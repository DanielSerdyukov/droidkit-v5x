package droidkit.annotation.internal;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.ListBuffer;
import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteObjectProcessor extends TreeTranslator implements AnnotationProcessor {

    private static final Map<TypeKind, String> JAVA_TO_SQLITE_TYPE = new HashMap<>();

    private static final Map<TypeKind, String> CURSOR_TO_JAVA_TYPE = new HashMap<>();

    private static final String[] CONFLICT_VALUES = new String[]{
            "",
            " ON CONFLICT ROLLBACK",
            " ON CONFLICT ABORT",
            " ON CONFLICT FAIL",
            " ON CONFLICT IGNORE",
            " ON CONFLICT REPLACE"
    };

    static {
        JAVA_TO_SQLITE_TYPE.put(TypeKind.LONG, "INTEGER");
        JAVA_TO_SQLITE_TYPE.put(TypeKind.INT, "INTEGER");
        JAVA_TO_SQLITE_TYPE.put(TypeKind.DOUBLE, "REAL");
        JAVA_TO_SQLITE_TYPE.put(TypeKind.FLOAT, "REAL");
        JAVA_TO_SQLITE_TYPE.put(TypeKind.BOOLEAN, "INTEGER");
        CURSOR_TO_JAVA_TYPE.put(TypeKind.LONG, "getLong");
        CURSOR_TO_JAVA_TYPE.put(TypeKind.INT, "getInt");
        CURSOR_TO_JAVA_TYPE.put(TypeKind.DOUBLE, "getDouble");
        CURSOR_TO_JAVA_TYPE.put(TypeKind.FLOAT, "getFloat");
        CURSOR_TO_JAVA_TYPE.put(TypeKind.BOOLEAN, "getBoolean");
    }

    private final Map<String, String> mFields = new HashMap<>();

    private final Map<String, String> mSetters = new HashMap<>();

    private final List<String> mColumnsDef = new ArrayList<>();

    private final List<String> mEnumColumns = new ArrayList<>();

    private final TypeElement mOriginElement;

    private final String mTableName;

    private final String mFieldPrefix;

    private String mPkField;

    SQLiteObjectProcessor(TypeElement element) {
        mOriginElement = element;
        final SQLiteObject annotation = element.getAnnotation(SQLiteObject.class);
        mTableName = annotation.value();
        mFieldPrefix = annotation.fieldPrefix();
    }

    @Override
    public void process() {
        final List<? extends Element> elements = mOriginElement.getEnclosedElements();
        for (final Element element : elements) {
            if (ElementKind.FIELD == element.getKind()) {
                final SQLitePk pk = element.getAnnotation(SQLitePk.class);
                if (pk != null) {
                    mPkField = processPrimaryKey((VariableElement) element, pk);
                    continue;
                }
                final SQLiteColumn column = element.getAnnotation(SQLiteColumn.class);
                if (column != null) {
                    processColumn((VariableElement) element, column);
                }
            }
        }
        if (JCUtils.isEmpty(mPkField)) {
            JCUtils.error("No such field annotated with @SQLitePk", mOriginElement);
        } else {
            JCUtils.getTree(mOriginElement).accept(this);
            System.out.println(JCUtils.getTree(mOriginElement));
        }
    }

    @Override
    public boolean finishProcessing() {
        return false;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equals(jcClassDecl.getSimpleName(), mOriginElement.getSimpleName())) {
            final ListBuffer<JCTree> buffer = new ListBuffer<>();
            buffer.addAll(jcClassDecl.defs);
            buffer.add(makeUpdateIfActiveObject());
            //buffer.add(makeCreate1Method());
            //buffer.add(makeCreate2Method());
            //buffer.add(makeSaveToSQLiteMethod());
            jcClassDecl.defs = buffer.toList();
            this.result = jcClassDecl;
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        final String columnName = mSetters.get(jcMethodDecl.name.toString());
        if (!JCUtils.isEmpty(columnName)) {
            JCTree.JCExpression value = JCUtils.ident(mFields.get(columnName));
            if (mEnumColumns.contains(columnName)) {
                value = JCUtils.invoke(JCUtils.MAKER.Select(value, JCUtils.NAMES.fromString("name"))).getExpression();
            }
            jcMethodDecl.body = JCUtils.block(JCUtils.MAKER.Try(jcMethodDecl.body,
                    JCUtils.<JCTree.JCCatch>nilList(),
                    JCUtils.block(new JCIfSpec(JCUtils.notNull(JCUtils.ident("mClientRef")))
                            .thenBlock(JCUtils.invoke(
                                    JCUtils.ident("updateIfActiveObject"),
                                    JCUtils.MAKER.Literal(TypeTag.CLASS, columnName),
                                    value
                            ))
                            .build())));
            this.result = jcMethodDecl;
        }
    }

    private String processPrimaryKey(VariableElement field, SQLitePk pk) {
        if (TypeKind.LONG != field.asType().getKind()) {
            JCUtils.error("@SQLitePk must be long", field);
        }
        final String fieldName = field.getSimpleName().toString();
        mFields.put("_id", fieldName);
        mColumnsDef.add("_id INTEGER PRIMARY KEY" + CONFLICT_VALUES[pk.value()]);
        processGetterSetter(fieldName, "_id", pk.setter());
        return fieldName;
    }

    private void processColumn(VariableElement field, SQLiteColumn column) {
        final TypeMirror type = field.asType();
        final TypeKind typeKind = type.getKind();
        final String sqliteType = JAVA_TO_SQLITE_TYPE.get(typeKind);
        final String fieldName = field.getSimpleName().toString();
        final String columnName = JCUtils.nonEmpty(column.value(), fieldName);
        if (!JCUtils.isEmpty(sqliteType)) {
            mFields.put(columnName, fieldName);
            mColumnsDef.add(columnName + " " + sqliteType);
            processGetterSetter(fieldName, columnName, column.setter());
            return;
        }
        if (Objects.equals(String.class.getName(), type.toString())) {
            mFields.put(columnName, fieldName);
            mColumnsDef.add(columnName + " TEXT");
            processGetterSetter(fieldName, columnName, column.setter());
            return;
        }
        if (Objects.equals("byte[]", type.toString())) {
            mFields.put(columnName, fieldName);
            mColumnsDef.add(columnName + " BLOB");
            processGetterSetter(fieldName, columnName, column.setter());
            return;
        }
        if (TypeKind.DECLARED == typeKind) {
            final Element typeElement = JCUtils.ENV.getTypeUtils().asElement(field.asType());
            if (ElementKind.ENUM == typeElement.getKind()) {
                mFields.put(columnName, fieldName);
                mColumnsDef.add(columnName + " TEXT");
                mEnumColumns.add(columnName);
                processGetterSetter(fieldName, columnName, column.setter());
                return;
            }
        }
        JCUtils.error("Unsupported column type " + type, field);
    }

    private void processGetterSetter(String fieldName, String columnName, String setter) {
        final String normalizedName = JCUtils.normalize(mFieldPrefix, fieldName);
        if (JCUtils.isEmpty(setter)) {
            setter = "set" + normalizedName;
        }
        mSetters.put(setter, columnName);
    }

    private JCTree.JCMethodDecl makeUpdateIfActiveObject() {
        return new JCMethodSpec("updateIfActiveObject")
                .modifiers(Flags.PRIVATE)
                .params(new JCVarSpec("column")
                        .varType(JCUtils.ident(String.class))
                        .modifiers(Flags.PARAMETER)
                        .build())
                .params(new JCVarSpec("value")
                        .varType(JCUtils.ident(Object.class))
                        .modifiers(Flags.PARAMETER)
                        .build())
                .statements(new JCIfSpec(JCUtils.notNull(JCUtils.ident("mClientRef")))
                        .thenBlock(
                                new JCVarSpec("client")
                                        .varType(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                                        .modifiers(Flags.FINAL)
                                        .init(JCUtils.invoke(JCUtils.select("mClientRef", "get")).expr)
                                        .build(),
                                new JCIfSpec(JCUtils.notNull(JCUtils.ident("client")))
                                        .thenBlock(JCUtils.invoke(
                                                JCUtils.select("droidkit.sqlite", "SQLiteClientUtils", "updateColumn"),
                                                JCUtils.ident("client"),
                                                JCUtils.ident("_TABLE_"),
                                                JCUtils.ident(mPkField),
                                                JCUtils.ident("column"),
                                                JCUtils.ident("value")
                                        ))
                                        .build()
                        )
                        .build())
                .build();
    }

    /*private void updateIfActiveObject(String column, Object value) {
        if (mClientRef != null) {
            final SQLiteClient client = mClientRef.get();
            if (client != null) {
                client.executeUpdateDelete("UPDATE " + _TABLE_ + " SET " + column + " = ? WHERE _id = ?;", value, mId);
            }
        }
    }*/

}
