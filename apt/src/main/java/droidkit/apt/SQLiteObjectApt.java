package droidkit.apt;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.ListBuffer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectApt extends TreeTranslator implements Apt {

    private static final CodeBlock.Builder STATIC_BLOCK = CodeBlock.builder();

    private static final String[] CONFLICT_VALUES = new String[]{
            "",
            " ON CONFLICT ROLLBACK",
            " ON CONFLICT ABORT",
            " ON CONFLICT FAIL",
            " ON CONFLICT IGNORE",
            " ON CONFLICT REPLACE"
    };

    private static final ClassName DB_UTILS = ClassName.get("droidkit.database", "DatabaseUtils");

    private final Map<String, String> mFieldMap = new LinkedHashMap<>();

    private final Map<String, String> mSetterMap = new LinkedHashMap<>();

    private final List<String> mColumnsDef = new ArrayList<>();

    private final CodeBlock.Builder mInstantiateBlock = CodeBlock.builder();

    private final TypeElement mElement;

    private final String mPackageName;

    private String mPkFieldName;

    private String mTableName;

    public SQLiteObjectApt(TypeElement element) {
        mElement = element;
        mPackageName = element.getEnclosingElement().toString();
        mTableName = mElement.getAnnotation(SQLiteObject.class).value();
    }

    public static void brewSchemaClass() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder("SQLiteSchema")
                .addModifiers(Modifier.PUBLIC);
        final TypeSpec spec = builder.addStaticBlock(STATIC_BLOCK.build()).build();
        final JavaFile javaFile = JavaFile.builder("droidkit.sqlite", spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JavacEnv.createSourceFile(javaFile, spec);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
    }

    @Override
    public void process(RoundEnvironment roundEnv) {

    }

    @Override
    public void finishProcessing() throws IOException {
        JavacEnv.getTree(mElement).accept(this);
        STATIC_BLOCK.addStatement("SQLiteProvider.SCHEMA.putIfAbsent($S, \"($L)\")", mTableName,
                Joiner.on(", ").join(mColumnsDef));
        final TypeSpec.Builder builder = TypeSpec.classBuilder(mElement.getSimpleName() + "$Delegate")
                .addModifiers(Modifier.PUBLIC)
                .addOriginatingElement(mElement)
                .addSuperinterface(ParameterizedTypeName.get(
                        ClassName.get("droidkit.sqlite", "SQLiteDelegate"),
                        ClassName.get(mElement)));
        fields(builder);
        constructor(builder);
        overrideGetTableName(builder);
        overrideInstantiate(builder);
        overrideSave(builder);
        overrideUpdate(builder);
        final TypeSpec spec = builder.build();
        final JavaFile javaFile = JavaFile.builder(mPackageName, spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JavacEnv.createSourceFile(javaFile, spec);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (Objects.equal(jcClassDecl.getSimpleName(), mElement.getSimpleName())) {
            final TreeMaker maker = JavacEnv.sTreeMaker;
            final ListBuffer<JCTree> defs = new ListBuffer<>();
            defs.addAll(jcClassDecl.defs);
            defs.add(maker.VarDef(
                    maker.Modifiers(0),
                    JavacEnv.sNames.fromString("_delegate_"),
                    JavacEnv.select(mPackageName, mElement.getSimpleName() + "$Delegate"),
                    null
            ));
            jcClassDecl.defs = defs.toList();
            this.result = jcClassDecl;
        }
    }

    @Override
    public void visitVarDef(final JCTree.JCVariableDecl jcVariableDecl) {
        super.visitVarDef(jcVariableDecl);
        if ((jcVariableDecl.mods.flags & Flags.PARAMETER) == 0) {
            jcVariableDecl.accept(new TreeTranslator() {
                @Override
                public void visitAnnotation(JCTree.JCAnnotation jcAnnotation) {
                    super.visitAnnotation(jcAnnotation);
                    if ("droidkit.annotation.SQLitePk".equals(jcAnnotation.type.toString())) {
                        acceptPk(jcVariableDecl, jcAnnotation);
                    } else if ("droidkit.annotation.SQLiteColumn".equals(jcAnnotation.type.toString())) {
                        acceptColumn(jcVariableDecl, jcAnnotation);
                    }
                }
            });
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl jcMethodDecl) {
        super.visitMethodDef(jcMethodDecl);
        final String fieldName = mSetterMap.get(jcMethodDecl.name.toString());
        if (!Strings.isNullOrEmpty(fieldName)) {
            final TreeMaker maker = JavacEnv.sTreeMaker;
            jcMethodDecl.body.stats = com.sun.tools.javac.util.List.<JCTree.JCStatement>of(maker.Try(
                    maker.Block(0, jcMethodDecl.body.stats),
                    com.sun.tools.javac.util.List.<JCTree.JCCatch>nil(),
                    maker.Block(0, com.sun.tools.javac.util.List.<JCTree.JCStatement>of(maker.If(
                            JCBinaries.notNull(maker, JavacEnv.ident("_delegate_")),
                            maker.Block(0, com.sun.tools.javac.util.List.<JCTree.JCStatement>of(
                                    maker.Exec(maker.Apply(
                                            com.sun.tools.javac.util.List.<JCTree.JCExpression>nil(),
                                            JavacEnv.select("_delegate_", "update"),
                                            com.sun.tools.javac.util.List.of(
                                                    JCLiterals.valueOf(maker, mFieldMap.get(fieldName)),
                                                    JavacEnv.thisIdent(fieldName),
                                                    JavacEnv.thisIdent(mPkFieldName)
                                            )
                                    ))
                            )),
                            null
                    )))
            ));
            this.result = jcMethodDecl;
        }
    }

    private void fields(TypeSpec.Builder builder) {
        final Joiner joiner = Joiner.on(", ");
        final List<String> columns = Lists.newArrayList(mFieldMap.values());
        builder.addField(FieldSpec.builder(String.class, "INSERT",
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"INSERT INTO $L($L) VALUES($L)\"", mTableName,
                        joiner.join(columns),
                        joiner.join(Collections.nCopies(columns.size(), "?")))
                .build());
        builder.addField(FieldSpec.builder(String.class, "INSERT_WITHOUT_ID",
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("\"INSERT INTO $L($L) VALUES($L)\"", mTableName,
                        joiner.join(columns.subList(1, columns.size())),
                        joiner.join(Collections.nCopies(columns.size() - 1, "?")))
                .build());
        builder.addField(ClassName.get("droidkit.sqlite", "SQLiteClient"), "mClient",
                Modifier.PRIVATE, Modifier.FINAL);
    }

    private void constructor(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addStatement("mClient = client")
                .build());
    }

    private void overrideGetTableName(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("getTableName")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addStatement("return $S", mTableName)
                .build());
    }

    private void overrideInstantiate(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("instantiate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(mElement))
                .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
                .addStatement("final $T object = new $T()", mElement, mElement)
                .addCode(mInstantiateBlock.build())
                .addStatement("return object")
                .build());
    }

    private void overrideSave(TypeSpec.Builder builder) {
        final Joiner joiner = Joiner.on(", ");
        final List<String> fields = Lists.newArrayList(Iterables.transform(mFieldMap.keySet(),
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "object." + input;
                    }
                }));
        final MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("save")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.LONG)
                .addParameter(ClassName.get(mElement), "object")
                .beginControlFlow("if(object.$L > 0)", mPkFieldName);
        methodSpec.addStatement("mClient.executeInsert($L, $L)", "INSERT", joiner.join(fields));
        methodSpec.nextControlFlow("else");
        methodSpec.addStatement("object.$L = mClient.executeInsert($L, $L)", mPkFieldName, "INSERT_WITHOUT_ID",
                joiner.join(fields.subList(1, fields.size())));
        methodSpec.endControlFlow();
        methodSpec.addStatement("object._delegate_ = this");
        methodSpec.addStatement("return object.$L", mPkFieldName);
        builder.addMethod(methodSpec.build());
    }

    private void overrideUpdate(TypeSpec.Builder builder) {
        builder.addMethod(MethodSpec.methodBuilder("update")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addParameter(ClassName.get(String.class), "column")
                .addParameter(ClassName.get(Object.class), "value")
                .addParameter(TypeName.LONG, "id")
                .addStatement("return mClient.executeUpdateDelete(\"UPDATE $L SET\" + " +
                        "column + \" = ? WHERE _id=?\", value, id)", mTableName)
                .build());
    }

    private void acceptPk(JCTree.JCVariableDecl field, JCTree.JCAnnotation annotation) {
        field.mods.flags &= ~Flags.PRIVATE;
        final String fieldName = field.name.toString();
        if (TypeKind.LONG == field.vartype.type.getKind()) {
            int conflictClause = CONFLICT_VALUES.length - 1;
            String setterName = getCanonicalSetterName(fieldName);
            for (final JCTree.JCExpression arg : annotation.args) {
                if (arg instanceof JCTree.JCAssign) {
                    final JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                    final Object value = ((JCTree.JCLiteral) assign.rhs).value;
                    if ("value".equals(assign.lhs.toString())) {
                        conflictClause = Preconditions.checkElementIndex((int) value, CONFLICT_VALUES.length,
                                formatErrorMessage(field.name, "@SQLitePk(value)"));
                    } else if ("setter".equals(assign.lhs.toString())) {
                        setterName = (String) value;
                        Preconditions.checkArgument(!setterName.isEmpty(), formatErrorMessage(field.name,
                                "@SQLitePk(setterName) can't be empty"));
                    }
                }
            }
            mPkFieldName = fieldName;
            mFieldMap.put(fieldName, "_id");
            mSetterMap.put(setterName, fieldName);
            mColumnsDef.add("_id INTEGER PRIMARY KEY" + CONFLICT_VALUES[conflictClause]);
            mInstantiateBlock.add("object.$L = $T.getLong(cursor, $S);\n", fieldName, DB_UTILS, "_id");
        } else {
            JavacEnv.logE(mElement, "%s must be 'long'", field.name);
        }
    }

    private void acceptColumn(JCTree.JCVariableDecl field, JCTree.JCAnnotation annotation) {
        field.mods.flags &= ~Flags.PRIVATE;
        final Tree.Kind varTypeKind = field.vartype.getKind();
        if (Tree.Kind.PRIMITIVE_TYPE == varTypeKind) {
            acceptPrimitiveColumn(field, annotation);
        } else if (Tree.Kind.IDENTIFIER == varTypeKind) {
            acceptDeclaredColumn(field, annotation);
        } else if (Tree.Kind.ARRAY_TYPE == varTypeKind
                && Objects.equal(field.vartype.type.toString(), "byte[]")) {
            mColumnsDef.add(acceptSQLiteColumn(field, annotation) + " BLOB");
        } else {
            JavacEnv.logE(mElement, "Unsupported %s -> SQLite type conversion (%s)", field.vartype.type, field.name);
        }
    }

    private void acceptPrimitiveColumn(JCTree.JCVariableDecl field, JCTree.JCAnnotation annotation) {
        final String columnName = acceptSQLiteColumn(field, annotation);
        switch (field.vartype.type.getKind()) {
            case INT:
            case LONG:
            case SHORT:
            case BOOLEAN:
                mColumnsDef.add(columnName + " INTEGER");
                break;
            case DOUBLE:
            case FLOAT:
                mColumnsDef.add(columnName + " REAL");
                break;
            default:
                JavacEnv.logE(mElement, "Unsupported %s -> SQLite type conversion (%s)", field.vartype.type,
                        field.name);
                break;
        }
    }

    private void acceptDeclaredColumn(JCTree.JCVariableDecl field, JCTree.JCAnnotation annotation) {
        final String fieldName = field.name.toString();
        final String columnName = acceptSQLiteColumn(field, annotation);
        if (ElementKind.ENUM == field.vartype.type.asElement().getKind()) {
            mColumnsDef.add(columnName + " TEXT");
            mInstantiateBlock.add("object.$L = $T.getEnum(cursor, $S, $T.class);\n", fieldName, DB_UTILS,
                    columnName, field.vartype.type.asElement());
        } else if (JavacEnv.isSubtype(field.vartype.type, BigDecimal.class)) {
            mColumnsDef.add(columnName + " REAL");
            mInstantiateBlock.add("object.$L = $T.getBigDec(cursor, $S);\n", fieldName, DB_UTILS, columnName);
        } else if (JavacEnv.isSubtype(field.vartype.type, Number.class)) {
            mColumnsDef.add(columnName + " INTEGER");
            mInstantiateBlock.add("object.$L = $T.getBigInt(cursor, $S);\n", fieldName, DB_UTILS, columnName);
        } else if (JavacEnv.isSubtype(field.vartype.type, "org.joda.time.DateTime")) {
            mColumnsDef.add(columnName + " INTEGER");
            mInstantiateBlock.add("object.$L = $T.getDateTime(cursor, $S);\n", fieldName, DB_UTILS, columnName);
        } else if (JavacEnv.isSubtype(field.vartype.type, String.class)) {
            mColumnsDef.add(columnName + " TEXT");
            mInstantiateBlock.add("object.$L = $T.getString(cursor, $S);\n", fieldName, DB_UTILS, columnName);
        } else {
            JavacEnv.logE(mElement, "Unsupported %s -> SQLite type conversion (%s)", field.vartype.type,
                    field.name);
        }
    }

    private String acceptSQLiteColumn(JCTree.JCVariableDecl field, JCTree.JCAnnotation annotation) {
        final String fieldName = field.name.toString();
        String columnName = getCanonicalColumnName(fieldName);
        String setterName = getCanonicalSetterName(fieldName);
        for (final JCTree.JCExpression arg : annotation.args) {
            if (arg instanceof JCTree.JCAssign) {
                final JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                final Object value = ((JCTree.JCLiteral) assign.rhs).value;
                if ("value".equals(assign.lhs.toString())) {
                    columnName = (String) value;
                    Preconditions.checkArgument(!columnName.isEmpty(), formatErrorMessage(field.name,
                            "@SQLiteColumn(value) can't be empty"));
                } else if ("setter".equals(assign.lhs.toString())) {
                    setterName = (String) value;
                    Preconditions.checkArgument(!setterName.isEmpty(), formatErrorMessage(field.name,
                            "@SQLiteColumn(setterName) can't be empty"));
                }
            }
        }
        mFieldMap.put(fieldName, columnName);
        mSetterMap.put(setterName, fieldName);
        return columnName;
    }

    private String formatErrorMessage(Object element, String format, Object... args) {
        return mElement.getSimpleName() + "(" + element + ") " + String.format(format, args);
    }

    private String getCanonicalColumnName(String fieldName) {
        if (fieldName.startsWith("m")) {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName.substring(1));
        }
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

    private String getCanonicalSetterName(String fieldName) {
        if (fieldName.startsWith("m")) {
            return "set" + fieldName.substring(1);
        }
        return "set" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
    }

}
