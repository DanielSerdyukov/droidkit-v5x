package droidkit.annotation.internal;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.ListBuffer;
import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLitePk;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectProcessor1 extends TreeTranslator implements AnnotationProcessor {

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

    private final List<String> mColumnsDef = new ArrayList<>();

    private final ListBuffer<JCTree.JCStatement> mInstantiateBlock = new ListBuffer<>();

    private final JavacProcessingEnvironment mEnv;

    private final TypeElement mOriginElement;

    private String mPkName;

    SQLiteObjectProcessor1(TypeElement element, JavacProcessingEnvironment env) {
        mEnv = env;
        mOriginElement = element;
    }

    @Override
    public void process() {
        final List<? extends Element> elements = mOriginElement.getEnclosedElements();
        for (final Element element : elements) {
            if (ElementKind.FIELD == element.getKind()) {
                final SQLitePk pk = element.getAnnotation(SQLitePk.class);
                if (pk != null) {
                    processPk((VariableElement) element, pk);
                    mPkName = element.getSimpleName().toString();
                    continue;
                }
                final SQLiteColumn column = element.getAnnotation(SQLiteColumn.class);
                if (column != null) {
                    processColumn((VariableElement) element, column);
                }
            }
        }
        if (JCUtils.isEmpty(mPkName)) {
            mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "No such field annotated with @SQLitePk", mOriginElement);
        } else {
            JCUtils.getTree(mOriginElement).accept(this);
            System.out.println(JCUtils.getTree(mOriginElement));
        }
    }

    @Override
    public boolean finishProcessing() {
        for (final String columnDef : mColumnsDef) {
            System.out.println(columnDef);
        }
        return false;
    }

    /*@Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (jcClassDecl.getSimpleName().equals(mOriginElement.getSimpleName())) {
            final ListBuffer<JCTree> buffer = new ListBuffer<>();
            buffer.addAll(jcClassDecl.defs);
            buffer.add(makeSQLiteClientRef());
            buffer.add(makeCreate1Method());
            buffer.add(makeCreate2Method());
            buffer.add(makeSaveToSQLiteMethod());
            jcClassDecl.defs = buffer.toList();
            this.result = jcClassDecl;
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        if (jcMethodDecl.getName().toString().startsWith("set")) {
            jcMethodDecl.body = JCUtils.block(JCUtils.MAKER.Try(jcMethodDecl.body,
                    JCUtils.<JCTree.JCCatch>nilList(),
                    JCUtils.block(new JCIfSpec(JCUtils.notNull(JCUtils.ident("mClientRef")))
                            .thenBlock(JCUtils.invoke(
                                    JCUtils.ident("saveToSQLite"),
                                    JCUtils.invoke(JCUtils.select("mClientRef", "get")).getExpression(),
                                    JCUtils.ident(JCUtils.NAMES._this)
                            ))
                            .build())));
            this.result = jcMethodDecl;
        }
    }*/

    private void processPk(VariableElement element, SQLitePk pk) {
        if (TypeKind.LONG != element.asType().getKind()) {
            mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@SQLitePk must be long", element);
        }
        mColumnsDef.add("_id INTEGER PRIMARY KEY" + CONFLICT_VALUES[pk.value()]);
        mInstantiateBlock.add(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                JCUtils.select("object", element.getSimpleName().toString()),
                JCCursor.getValue("cursor", "getLong", "_id")
        )));
    }

    private void processColumn(VariableElement element, SQLiteColumn column) {
        final TypeMirror type = element.asType();
        final TypeKind typeKind = type.getKind();
        String sqliteType = JAVA_TO_SQLITE_TYPE.get(typeKind);
        final String columnName = JCUtils.nonEmpty(column.value(), element.getSimpleName());
        if (!JCUtils.isEmpty(sqliteType)) {
            mColumnsDef.add(columnName + " " + sqliteType);
            mInstantiateBlock.add(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                    JCUtils.select("object", element.getSimpleName().toString()),
                    JCCursor.getValue("cursor", CURSOR_TO_JAVA_TYPE.get(typeKind), columnName)
            )));
            return;
        }
        if (Objects.equals(String.class.getName(), type.toString())) {
            mColumnsDef.add(columnName + " TEXT");
            mInstantiateBlock.add(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                    JCUtils.select("object", element.getSimpleName().toString()),
                    JCCursor.getValue("cursor", "getString", columnName)
            )));
            return;
        }
        if (Objects.equals("byte[]", type.toString())) {
            mColumnsDef.add(columnName + " BLOB");
            mInstantiateBlock.add(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                    JCUtils.select("object", element.getSimpleName().toString()),
                    JCCursor.getValue("cursor", "getBlob", columnName)
            )));
            return;
        }
        if (TypeKind.DECLARED == typeKind) {
            final Element typeElement = mEnv.getTypeUtils().asElement(element.asType());
            if (ElementKind.ENUM == typeElement.getKind()) {
                mColumnsDef.add(columnName + " TEXT");
                mInstantiateBlock.add(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                        JCUtils.select("object", element.getSimpleName().toString()),
                        JCUtils.enumValueOf(typeElement, JCCursor.getValue("cursor", "getString", columnName))
                )));
                return;
            }
        }
        mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unsupported column type " + type, element);
    }

    private JCTree.JCVariableDecl makeSQLiteClientRef() {
        return new JCVarSpec("mClientRef")
                .modifiers(Flags.PRIVATE)
                .varType(JCUtils.ident(WeakReference.class))
                .genericTypes(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                .build();
    }

    private JCTree.JCMethodDecl makeCreate1Method() {
        return new JCMethodSpec("create")
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .returnType(JCUtils.ident(mOriginElement.getSimpleName()))
                .params(new JCVarSpec("client")
                        .modifiers(Flags.PARAMETER)
                        .varType(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                        .build())
                .statements(new JCVarSpec("object")
                        .modifiers(Flags.FINAL)
                        .varType(JCUtils.ident(mOriginElement.getSimpleName()))
                        .init(new JCNewSpec(mOriginElement.getSimpleName()).build())
                        .build())
                .statements(JCUtils.MAKER.Return(JCUtils.ident("object")))
                .build();
    }

    private JCTree.JCMethodDecl makeCreate2Method() {
        return new JCMethodSpec("create")
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .returnType(JCUtils.ident(mOriginElement.getSimpleName()))
                .params(new JCVarSpec("client")
                        .modifiers(Flags.PARAMETER)
                        .varType(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                        .build())
                .params(new JCVarSpec("cursor")
                        .modifiers(Flags.PARAMETER)
                        .varType(JCUtils.select("android.database", "Cursor"))
                        .build())
                .statements(new JCVarSpec("object")
                        .modifiers(Flags.FINAL)
                        .varType(JCUtils.ident(mOriginElement.getSimpleName()))
                        .init(new JCNewSpec(mOriginElement.getSimpleName()).build())
                        .build())
                .statements(JCUtils.MAKER.Exec(JCUtils.MAKER.Assign(
                        JCUtils.select("object", "mClientRef"),
                        new JCNewSpec(WeakReference.class)
                                .genericTypes(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                                .args(JCUtils.ident("client"))
                                .build()
                )))
                .statements(mInstantiateBlock.toList())
                .statements(JCUtils.MAKER.Return(JCUtils.ident("object")))
                .build();
    }

    private JCTree.JCMethodDecl makeSaveToSQLiteMethod() {
        return new JCMethodSpec("saveToSQLite")
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .params(new JCVarSpec("client")
                        .modifiers(Flags.PARAMETER)
                        .varType(JCUtils.select("droidkit.sqlite", "SQLiteClient"))
                        .build())
                .params(new JCVarSpec("object")
                        .modifiers(Flags.PARAMETER)
                        .varType(JCUtils.ident(mOriginElement.getSimpleName()))
                        .build())
                .build();
    }

}
