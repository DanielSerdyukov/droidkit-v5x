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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectApt extends TreeTranslator implements Apt {

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

    private final List<String> mColumnsDef = new ArrayList<>();

    private final Map<String, VariableElement> mSetterMap = new LinkedHashMap<>();

    private final Map<VariableElement, String> mColumnMap = new LinkedHashMap<>();

    private final SQLiteHelper mHelper;

    private final TypeElement mElement;

    private VariableElement mPkField;

    public SQLiteObjectApt(TypeElement element) {
        mElement = element;
        mHelper = new SQLiteHelper(element.getAnnotation(SQLiteObject.class));
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
                    mPkField = field;
                    final String setterName = mHelper.getSetterName(mPkField, pk.setter());
                    mColumnsDef.add("_id INTEGER PRIMARY KEY" + getConflictValue(pk.value()));
                    mSetterMap.put(setterName, field);
                    mColumnMap.put(field, "_id");
                    continue;
                }
                final SQLiteColumn column = field.getAnnotation(SQLiteColumn.class);
                if (column != null) {
                    JavacEnv.get().<JCTree.JCVariableDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
                    final String columnName = mHelper.getColumnName(field, column.value());
                    final String setterName = mHelper.getSetterName(field, column.setter());
                    mColumnsDef.add(columnName + " " + mHelper.getSQLiteType(field.asType()));
                    mSetterMap.put(setterName, field);
                    mColumnMap.put(field, columnName);
                }
            }
        }
        if (mPkField == null) {
            JavacEnv.get().logE(mElement, "No such field annotated with @SQLitePk");
        }
    }

    @Override
    public void finishProcessing() throws IOException {
        SQLITE_CREATE.add(CodeBlock.builder()
                .addStatement("SQLiteProvider.SCHEMA.put($S, $S)", mHelper.getTableName(),
                        "(" + Utils.join(", ", mColumnsDef) + ")")
                .build());
        JavacEnv.get().getTree(mElement).accept(this);
    }

    private String getConflictValue(int index) {
        if (0 < index && index >= CONFLICT_VALUES.length) {
            return CONFLICT_VALUES[0];
        }
        return CONFLICT_VALUES[index];
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equals(jcClassDecl.getSimpleName(), mElement.getSimpleName())) {
            JCVarSpec.builder(JCClassName.get(String.class), "_TABLE_",
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .init(JCLiteral.stringValue(mHelper.getTableName()))
                    .build().emitTo(jcClassDecl);
            JCVarSpec.builder(SQLITE_CLIENT_REF, "mClientRef", Modifier.PRIVATE)
                    .build()
                    .emitTo(jcClassDecl);
            staticCreate().emitTo(jcClassDecl);
            staticSave().emitTo(jcClassDecl);
            this.result = jcClassDecl;
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        final String methodName = jcMethodDecl.name.toString();
        final VariableElement field = mSetterMap.get(methodName);
        if (field != null) {
            final String columnName = mColumnMap.get(field);
            final JCTree.JCExpression clientRef = JCSelector.get("mClientRef").ident();
            JCTrySpec.builder(jcMethodDecl.body)
                    .finalize(JCIfSpec.builder(JCBinary.notEqualTo(clientRef, JCLiteral.NULL))
                            .thenStatement(JCVarSpec.builder(SQLITE_CLIENT, "client", Modifier.FINAL)
                                    .init(JCSelector.get(clientRef, "get").invoke().getExpression())
                                    .build().<JCTree.JCStatement>tree())
                            .thenStatement(JCIfSpec.builder(JCBinary
                                    .notEqualTo(JCSelector.get("client").ident(), JCLiteral.NULL))
                                    .thenStatement(JCSelector.get("client", "executeUpdateDelete").invoke(
                                            mHelper.updateColumnStatement(columnName),
                                            mHelper.getSQLiteFieldValue(null, field),
                                            JCSelector.getField(null, mPkField).ident()
                                    ))
                                    .build().<JCTree.JCStatement>tree())
                            .build().<JCTree.JCStatement>tree())
                    .build().overrideMethod(jcMethodDecl);
            this.result = jcMethodDecl;
        }
    }

    private JCMethodSpec staticCreate() {
        final JCClassName className = JCClassName.get(mElement);
        final JCVarSpec object = JCVarSpec.builder(className, "object", Modifier.FINAL)
                .init(className.newInstance())
                .build();
        final JCMethodSpec.Builder builder = JCMethodSpec.builder("_create")
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returnType(className)
                .addParameter(SQLITE_CLIENT, "client")
                .addParameter(CURSOR, "cursor")
                .addStatement(object.<JCTree.JCStatement>tree())
                .addStatement(JCSelector.get(object.ident(), "mClientRef")
                        .assign(SQLITE_CLIENT_REF.newInstance(JCSelector.get("client").ident())));
        for (final Map.Entry<VariableElement, String> entry : mColumnMap.entrySet()) {
            builder.addStatement(JCSelector.getField(object.ident(), entry.getKey())
                    .assign(mHelper.getCursorFieldValue(entry.getKey().asType(), "cursor", entry.getValue())));
        }
        return builder.addReturnStatement(object.ident()).build();
    }

    private JCMethodSpec staticSave() {
        final JCClassName className = JCClassName.get(mElement);
        final JCTree.JCExpression object = JCSelector.get("object").ident();
        return JCMethodSpec.builder("_save")
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returnType(className)
                .addParameter(SQLITE_CLIENT, "client")
                .addParameter(JCClassName.get(mElement), "object")
                .addStatement(JCIfSpec.builder(JCBinary
                        .greaterThan(JCSelector.getField(object, mPkField).ident(), JCLiteral.intValue(0)))
                        .thenStatement(JCSelector.get("client", "executeInsert").invoke(
                                mHelper.insertStatement(mColumnMap.values(), false),
                                mHelper.getSQLiteFieldValues(object, mColumnMap.keySet()),
                                0, mColumnMap.size()
                        ))
                        .elseStatement(JCSelector.getField(object, mPkField)
                                .assign(JCSelector.get("client", "executeInsert").invoke(
                                        mHelper.insertStatement(mColumnMap.values(), true),
                                        mHelper.getSQLiteFieldValues(object, mColumnMap.keySet()),
                                        1, mColumnMap.size()
                                ).getExpression()))
                        .build().<JCTree.JCStatement>tree())
                .addStatement(JCSelector.get("object", "mClientRef")
                        .assign(SQLITE_CLIENT_REF.newInstance(JCSelector.get("client").ident())))
                .addReturnStatement(object).build();
    }

}
