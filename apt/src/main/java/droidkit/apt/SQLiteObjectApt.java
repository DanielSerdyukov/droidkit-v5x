package droidkit.apt;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectApt extends TreeTranslator implements Apt {

    private static final Map<TypeKind, String> JAVA_TO_SQLITE_TYPE = new HashMap<>();

    private static final Map<Enum<?>, String> CURSOR_TO_JAVA_TYPE = new HashMap<>();

    private static final String[] CONFLICT_VALUES = new String[]{
            "",
            " ON CONFLICT ROLLBACK",
            " ON CONFLICT ABORT",
            " ON CONFLICT FAIL",
            " ON CONFLICT IGNORE",
            " ON CONFLICT REPLACE"
    };

    private static final List<CodeBlock> SQLITE_CREATE = new ArrayList<>();

    private static final JCClassName SQLITE_CLIENT = JCClassName.get("droidkit.sqlite", "SQLiteClient");

    private static final JCClassName CURSOR = JCClassName.get("android.database", "Cursor");

    private static final JCGenericType SQLITE_CLIENT_REF = JCGenericType.get(
            JCClassName.get(WeakReference.class), SQLITE_CLIENT);

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

    private final Map<String, String> mSetterToColumn = new HashMap<>();

    private final Map<String, JCTree.JCExpression> mSetterToField = new HashMap<>();

    private final Map<String, String> mSQLiteColumns = new LinkedHashMap<>();

    private final Map<String, String> mFieldToColumn = new LinkedHashMap<>();

    private final List<JCTree.JCExpressionStatement> mCursorToField = new ArrayList<>();

    private final TypeElement mElement;

    private final SQLiteObject mMetaObject;

    private String mPkField;

    public SQLiteObjectApt(TypeElement element) {
        mElement = element;
        mMetaObject = element.getAnnotation(SQLiteObject.class);
    }

    public static void brewClass() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("SQLiteSchema")
                .addModifiers(Modifier.PUBLIC);
        final CodeBlock.Builder staticBlock = CodeBlock.builder();
        for (final CodeBlock codeBlock : SQLITE_CREATE) {
            staticBlock.add(codeBlock);
        }
        final TypeSpec spec = builder.addStaticBlock(staticBlock.build()).build();
        final JavaFile javaFile = JavaFile.builder("droidkit.sqlite", spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JavacEnv.get().createSourceFile(javaFile, spec);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
    }

    private static List<JCTree.JCExpression> insertArgs(JCTree.JCExpression arg1, String object,
                                                        Collection<String> fields, String... initialFields) {
        final List<JCTree.JCExpression> list = new ArrayList<>(fields.size() + initialFields.length + 1);
        list.add(arg1);
        for (final String field : initialFields) {
            list.add(JCSelector.get(object, field).ident());
        }
        for (final String field : fields) {
            list.add(JCSelector.get(object, field).ident());
        }
        return list;
    }

    @Override
    public void process(RoundEnvironment roundEnv) {
        final List<? extends Element> elements = mElement.getEnclosedElements();
        for (final Element element : elements) {
            if (ElementKind.FIELD == element.getKind()) {
                final VariableElement field = (VariableElement) element;
                final SQLitePk pk = field.getAnnotation(SQLitePk.class);
                if (pk != null) {
                    JavacEnv.get().<JCTree.JCVariableDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
                    Utils.checkArgument(field, TypeKind.LONG == field.asType().getKind(),
                            "@SQLitePk field must be 'long'");
                    mPkField = field.getSimpleName().toString();
                    final String setterName = getSetterName(mPkField, pk.setter());
                    mSQLiteColumns.put("_id", "INTEGER PRIMARY KEY" + getConflictValue(pk.value()));
                    mSetterToColumn.put(setterName, getColumnName(mPkField, "_id"));
                    mSetterToField.put(setterName, JCSelector.get(mPkField).ident());
                    mCursorToField.add(getCursorToFieldAssign(field, "_id"));
                    continue;
                }
                final SQLiteColumn column = field.getAnnotation(SQLiteColumn.class);
                if (column != null) {
                    JavacEnv.get().<JCTree.JCVariableDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
                    final String fieldName = field.getSimpleName().toString();
                    final String columnName = getColumnName(fieldName, column.value());
                    final String setterName = getSetterName(fieldName, column.setter());
                    mSQLiteColumns.put(columnName, getSQLiteType(field));
                    mSetterToColumn.put(setterName, columnName);
                    if (Utils.isEnum(field.asType())) {
                        mSetterToField.put(setterName, JCSelector.get(fieldName, "name")
                                .invoke().getExpression());
                    } else {
                        mSetterToField.put(setterName, JCSelector.get(fieldName).ident());
                    }
                    mFieldToColumn.put(fieldName, columnName);
                    mCursorToField.add(getCursorToFieldAssign(field, columnName));
                }
            }
        }
        if (mPkField == null) {
            JavacEnv.get().logE(mElement, "No such field annotated with @SQLitePk");
        }
    }

    @Override
    public void finishProcessing() throws IOException {
        final StringBuilder query = new StringBuilder("(");
        final Iterator<Map.Entry<String, String>> iterator = mSQLiteColumns.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, String> entry = iterator.next();
            query.append(entry.getKey()).append(" ").append(entry.getValue());
            if (iterator.hasNext()) {
                query.append(", ");
            }
        }
        query.append(");");
        SQLITE_CREATE.add(CodeBlock.builder()
                .addStatement("SQLiteProvider.SCHEMA.put($S, $S)", mMetaObject.value(), query.toString())
                .build());
        JavacEnv.get().getTree(mElement).accept(this);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equals(jcClassDecl.getSimpleName(), mElement.getSimpleName())) {
            JCVarSpec.builder(JCClassName.get(String.class), "_TABLE_",
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .init(JCLiteral.stringValue(mMetaObject.value()))
                    .build().emitTo(jcClassDecl);
            JCVarSpec.builder(SQLITE_CLIENT_REF, "mClientRef", Modifier.PRIVATE)
                    .build()
                    .emitTo(jcClassDecl);
            staticCreate1().emitTo(jcClassDecl);
            save().emitTo(jcClassDecl);
            this.result = jcClassDecl;
            JavacEnv.get().logI(null, "%s", jcClassDecl);
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        final String methodName = jcMethodDecl.name.toString();
        final String columnName = mSetterToColumn.get(methodName);
        if (columnName != null) {
            final JCTree.JCExpression clientRef = JCSelector.get("mClientRef").ident();
            JCTrySpec.builder(jcMethodDecl.body)
                    .finalize(JCIfSpec.builder(JCBinary
                                    .notEqualTo(clientRef, JCLiteral.NULL))
                                    .thenStatement(JCVarSpec.builder(SQLITE_CLIENT, "client", Modifier.FINAL)
                                            .init(JCSelector.get(clientRef, "get").invoke().getExpression())
                                            .build().<JCTree.JCStatement>tree())
                                    .thenStatement(JCIfSpec.builder(JCBinary
                                            .notEqualTo(JCSelector.get("client").ident(), JCLiteral.NULL))
                                            .thenStatement(JCSelector.get("client", "executeUpdateDelete").invoke(
                                                    JCLiteral.stringValue("UPDATE " + mMetaObject.value()
                                                            + " SET " + columnName + " = ? WHERE _id = ?;"),
                                                    JCSelector.get(mSetterToField.get(methodName)).ident(),
                                                    JCSelector.get(mPkField).ident()
                                            ))
                                            .build().<JCTree.JCStatement>tree())
                                    .build().<JCTree.JCStatement>tree()
                    ).build().emitTo(jcMethodDecl);
            this.result = jcMethodDecl;
        }
    }

    private String getColumnName(String fieldName, String columnName) {
        if (columnName != null && columnName.length() > 0) {
            return columnName;
        }
        return fieldName.substring(mMetaObject.fieldPrefix().length()).toLowerCase();
    }

    private String getSetterName(String fieldName, String setterName) {
        if (setterName != null && setterName.length() > 0) {
            return setterName;
        }
        final String name = fieldName.substring(mMetaObject.fieldPrefix().length());
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String getConflictValue(int index) {
        if (0 < index && index >= CONFLICT_VALUES.length) {
            return CONFLICT_VALUES[0];
        }
        return CONFLICT_VALUES[index];
    }

    private String getSQLiteType(VariableElement element) {
        final TypeMirror type = element.asType();
        final TypeKind typeKind = type.getKind();
        if (JAVA_TO_SQLITE_TYPE.containsKey(typeKind)) {
            return JAVA_TO_SQLITE_TYPE.get(typeKind);
        }
        if (Objects.equals(String.class.getName(), type.toString())) {
            return "TEXT";
        }
        if (Objects.equals("byte[]", type.toString())) {
            return "BLOB";
        }
        if (Utils.isEnum(type)) {
            return "TEXT";
        }
        JavacEnv.get().logE(element, "Unsupported column type %s", type);
        throw new IllegalArgumentException("Unsupported column type " + type);
    }

    private JCTree.JCExpressionStatement getCursorToFieldAssign(VariableElement element, String columnName) {
        final TypeMirror type = element.asType();
        final TypeKind typeKind = type.getKind();
        final JCTree.JCExpression dbUtils = JCSelector.get("droidkit.database", "DatabaseUtils").ident();
        final JCTree.JCExpression cursor = JCSelector.get("cursor").ident();
        final JCTree.JCExpression column = JCLiteral.stringValue(columnName);
        if (CURSOR_TO_JAVA_TYPE.containsKey(typeKind)) {
            return JCSelector.get("object", element.getSimpleName().toString())
                    .assign(JCSelector.get(dbUtils, CURSOR_TO_JAVA_TYPE.get(typeKind))
                            .invoke(cursor, column).getExpression());
        }
        if (Objects.equals(String.class.getName(), type.toString())) {
            return JCSelector.get("object", element.getSimpleName().toString())
                    .assign(JCSelector.get(dbUtils, "getString")
                            .invoke(cursor, column).getExpression());
        }
        if (Objects.equals("byte[]", type.toString())) {
            return JCSelector.get("object", element.getSimpleName().toString())
                    .assign(JCSelector.get(dbUtils, "getBlob")
                            .invoke(cursor, column).getExpression());
        }
        if (Utils.isEnum(type)) {
            final TypeElement declared = JavacEnv.get().getElement(type);
            return JCSelector.get("object", element.getSimpleName().toString())
                    .assign(JCSelector.get(dbUtils, "getEnum")
                            .invoke(cursor, column, JCLiteral.clazz(JCClassName.get(declared)))
                            .getExpression());
        }
        JavacEnv.get().logE(element, "Unsupported field type %s", type);
        throw new IllegalArgumentException("Unsupported field type " + type);
    }

    private JCMethodSpec staticCreate1() {
        final JCClassName className = JCClassName.get(mElement);
        final JCVarSpec object = JCVarSpec.builder(className, "object", Modifier.FINAL)
                .init(className.newInstance())
                .build();
        return JCMethodSpec.builder("_create")
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returnType(className)
                .addParameter(SQLITE_CLIENT, "client")
                .addParameter(CURSOR, "cursor")
                .addStatement(object.<JCTree.JCStatement>tree())
                .addStatement(JCSelector.get("object", "mClientRef")
                        .assign(SQLITE_CLIENT_REF.newInstance(JCSelector.get("client").ident())))
                .addStatements(mCursorToField)
                .addStatement(JCSelector.get("droidkit.sqlite", "SQLiteCache", "put").invoke(
                        JCLiteral.clazz(className),
                        JCSelector.get(object.ident(), mPkField).ident(),
                        object.ident()
                ))
                .addReturnStatement(object.ident())
                .build();
    }

    private JCMethodSpec save() {
        final JCClassName className = JCClassName.get(mElement);
        final Set<String> fields = mFieldToColumn.keySet();
        final Collection<String> columns = mFieldToColumn.values();
        final List<String> binders = new ArrayList<>(columns);
        Collections.fill(binders, "?");
        return JCMethodSpec.builder("_save")
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returnType(className)
                .addParameter(SQLITE_CLIENT, "client")
                .addParameter(JCClassName.get(mElement), "object")
                .addStatement(JCIfSpec.builder(JCBinary
                        .greaterThan(JCSelector.get("object", mPkField).ident(), JCLiteral.intValue(0)))
                        .thenStatement(JCSelector.get("client", "executeInsert").invoke(
                                insertArgs(JCLiteral.stringValue("INSERT INTO " + mMetaObject.value() +
                                                "(_id, " + Utils.join(", ", columns) + ")" +
                                                " VALUES (?, " + Utils.join(", ", binders) + ");"),
                                        "object", fields, mPkField)
                        ))
                        .elseStatement(JCSelector.get("object", mPkField)
                                .assign(JCSelector.get("client", "executeInsert").invoke(
                                        insertArgs(JCLiteral.stringValue("INSERT INTO " + mMetaObject.value() +
                                                        "(" + Utils.join(", ", columns) + ")" +
                                                        " VALUES (" + Utils.join(", ", binders) + ");"),
                                                "object", fields)
                                ).getExpression()))
                        .build().<JCTree.JCStatement>tree())
                .addStatement(JCSelector.get("object", "mClientRef").assign(SQLITE_CLIENT_REF
                        .newInstance(JCSelector.get("client").ident())))
                .addStatement(JCSelector.get("droidkit.sqlite", "SQLiteCache", "put").invoke(
                        JCLiteral.clazz(className),
                        JCSelector.get("object", mPkField).ident(),
                        JCSelector.get("object").ident()
                ))
                .addReturnStatement(JCSelector.get("object").ident())
                .build();
    }

}
