package droidkit.processor.sqlite;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Names;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import droidkit.annotation.SQLiteObject;
import droidkit.processor.ElementScanner;
import droidkit.processor.JCLiterals;
import droidkit.processor.ProcessingEnv;
import droidkit.processor.Strings;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteObjectScanner extends ElementScanner {

    private static final CodeBlock.Builder META_BLOCK = CodeBlock.builder();

    private final List<String> mColumnsDef = new ArrayList<>();

    private final Map<String, String> mFieldToColumn = new LinkedHashMap<>();

    private final Map<String, String> mSetterToField = new LinkedHashMap<>();

    private final CodeBlock.Builder mInstantiateBlock = CodeBlock.builder();

    private final List<ExecutableElement> mMethods = new ArrayList<>();

    private TypeElement mOriginType;

    private String mPrimaryKey;

    private String mTableName;

    private boolean mActiveRecord;

    public SQLiteObjectScanner(ProcessingEnv env) {
        super(env);
    }

    public static void brewMetaClass(ProcessingEnvironment processingEnv) {
        final TypeSpec typeSpec = TypeSpec.classBuilder("SQLiteMetaData")
                .addModifiers(Modifier.PUBLIC)
                .addStaticBlock(META_BLOCK.build())
                .build();
        final JavaFile javaFile = JavaFile.builder("droidkit.sqlite", typeSpec)
                .addFileComment(AUTO_GENERATED_FILE)
                .build();
        try {
            final JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(javaFile.packageName + "." + typeSpec.name);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Logger.getGlobal().throwing(SQLiteObjectScanner.class.getName(), "brewMetaClass", e);
        }
    }

    @Override
    public Void visitType(TypeElement type, Void aVoid) {
        final SQLiteObject annotation = type.getAnnotation(SQLiteObject.class);
        if (annotation != null) {
            mOriginType = type;
            mTableName = annotation.value();
            mActiveRecord = annotation.activeRecord();
        }
        return super.visitType(type, aVoid);
    }

    @Override
    public Void visitVariable(VariableElement field, Void aVoid) {
        for (final FieldVisitor visitor : FieldVisitor.SUPPORTED) {
            final Annotation annotation = visitor.getAnnotation(getEnv(), field);
            if (annotation != null) {
                getEnv().<JCTree.JCVariableDecl>getTree(field).mods.flags &= ~Flags.PRIVATE;
                visitor.visit(this, getEnv(), field, annotation);
            }
        }
        return super.visitVariable(field, aVoid);
    }

    @Override
    public Void visitExecutable(ExecutableElement method, Void aVoid) {
        if (mOriginType == method.getEnclosingElement()) {
            mMethods.add(method);
        }
        return super.visitExecutable(method, aVoid);
    }

    @Override
    public void visitEnd() {
        if (mOriginType != null) {
            final ClassName className = brewJava();
            if (mActiveRecord) {
                Observable.from(mMethods)
                        .filter(new SetterOnly(mSetterToField))
                        .subscribe(new Action1<ExecutableElement>() {
                            @Override
                            public void call(ExecutableElement method) {
                                final String fieldName = mSetterToField.get(method.getSimpleName().toString());
                                getEnv().getTree(method).accept(new SetterTranslator(
                                        getEnv().getJavacEnv(),
                                        className,
                                        fieldName,
                                        mFieldToColumn.get(fieldName),
                                        mPrimaryKey
                                ));
                            }
                        });
            }
        }
    }

    void setPrimaryKey(String primaryKey) {
        mPrimaryKey = primaryKey;
    }

    void addColumnDef(String def) {
        mColumnsDef.add(def);
    }

    void putFieldToColumn(String fieldName, String columnName) {
        mFieldToColumn.put(fieldName, columnName);
    }

    void putFieldToSetter(String fieldName, String setterName) {
        if (setterName.isEmpty()) {
            if ('m' == fieldName.charAt(0)
                    && Character.isUpperCase(fieldName.charAt(1))) {
                mSetterToField.put("set" + Strings.capitalize(fieldName.substring(1)), fieldName);
            } else {
                mSetterToField.put("set" + Strings.capitalize(fieldName), fieldName);
            }
        } else {
            mSetterToField.put(setterName, fieldName);
        }
    }

    void addInstantiateStatement(CodeBlock codeBlock) {
        mInstantiateBlock.add(codeBlock);
    }

    //region implementation
    private ClassName brewJava() {
        final TypeSpec typeSpec = TypeSpec.classBuilder(mOriginType.getSimpleName() + "$SQLiteHelper")
                .addModifiers(Modifier.PUBLIC)
                .addField(clientRef())
                .addMethod(attachInfo())
                .addMethod(instantiate())
                .addMethod(insert())
                .addMethod(updateWithClient())
                .addMethod(updateIfActive())
                .addOriginatingElement(mOriginType)
                .build();
        final JavaFile javaFile = JavaFile.builder(mOriginType.getEnclosingElement().toString(), typeSpec)
                .addFileComment(AUTO_GENERATED_FILE)
                .build();
        try {
            final JavaFileObject sourceFile = getEnv().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, mOriginType);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Logger.getGlobal().throwing(SQLiteObjectScanner.class.getName(), "brewJava", e);
        }
        META_BLOCK.addStatement("$T.attachTableInfo($T.class, $S, $S)",
                ClassName.get("droidkit.sqlite", "SQLiteSchema"),
                ClassName.get(mOriginType), mTableName,
                Strings.join(", ", mColumnsDef));
        if (mActiveRecord) {
            META_BLOCK.addStatement("$T.attachHelper($T.class)",
                    ClassName.get("droidkit.sqlite", "SQLiteProvider"),
                    ClassName.get(javaFile.packageName, typeSpec.name));
        }
        return ClassName.get(javaFile.packageName, typeSpec.name);
    }

    private FieldSpec clientRef() {
        return FieldSpec.builder(ParameterizedTypeName.get(
                ClassName.get(Reference.class),
                ClassName.get("droidkit.sqlite", "SQLiteClient")
        ), "sClientRef", Modifier.PRIVATE, Modifier.STATIC).build();
    }

    private MethodSpec attachInfo() {
        return MethodSpec.methodBuilder("attachInfo")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addStatement("sClientRef = new $T<>(client)", ClassName.get(WeakReference.class))
                .build();
    }

    private MethodSpec instantiate() {
        return MethodSpec.methodBuilder("instantiate")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.database", "Cursor"), "cursor")
                .returns(ClassName.get(mOriginType))
                .addStatement("final $1T object = new $1T()", ClassName.get(mOriginType))
                .addCode(mInstantiateBlock.build())
                .addStatement("return object")
                .build();
    }

    private MethodSpec insert() {
        return MethodSpec.methodBuilder("insert")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addParameter(ClassName.get(mOriginType), "object")
                .returns(TypeName.LONG)
                .beginControlFlow("if (object.$L > 0)", mPrimaryKey)
                .addStatement("object.$L = client.executeInsert($S, $L)", mPrimaryKey,
                        String.format(Locale.US, "INSERT INTO %s(%s) VALUES(%s);", mTableName,
                                Strings.join(", ", mFieldToColumn.values()),
                                Strings.join(", ", Collections.nCopies(mFieldToColumn.size(), "?"))),
                        Strings.transformAndJoin(", ", mFieldToColumn.keySet(), new ObjectField()))
                .nextControlFlow("else")
                .addStatement("object.$L = client.executeInsert($S, $L)", mPrimaryKey,
                        String.format(Locale.US, "INSERT INTO %s(%s) VALUES(%s);", mTableName,
                                Strings.join(", ", mFieldToColumn.values(), 1),
                                Strings.join(", ", Collections.nCopies(mFieldToColumn.size() - 1, "?"))),
                        Strings.transformAndJoin(", ", mFieldToColumn.keySet(), new ObjectField(), 1))
                .endControlFlow()
                .addStatement("$T.notifyChange($T.class)",
                        ClassName.get("droidkit.sqlite", "SQLiteSchema"),
                        ClassName.get(mOriginType))
                .addStatement("return object.$L", mPrimaryKey)
                .build();
    }

    private MethodSpec updateWithClient() {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("droidkit.sqlite", "SQLiteClient"), "client")
                .addParameter(ClassName.get(mOriginType), "object")
                .returns(TypeName.INT)
                .addStatement("$T affectedRows = client.executeUpdateDelete($S, $L, $L)",
                        TypeName.INT, "UPDATE " + mTableName + " SET " +
                                Strings.transformAndJoin(", ", mFieldToColumn.values(), new ColumnBinder(), 1) +
                                " WHERE _id = ?;",
                        Strings.transformAndJoin(", ", mFieldToColumn.keySet(), new ObjectField(), 1),
                        "object." + mPrimaryKey)
                .beginControlFlow("if (affectedRows > 0)")
                .addStatement("$T.notifyChange($T.class)",
                        ClassName.get("droidkit.sqlite", "SQLiteSchema"),
                        ClassName.get(mOriginType))
                .endControlFlow()
                .addStatement("return affectedRows")
                .build();
    }

    private MethodSpec updateIfActive() {
        return MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.STATIC)
                .addParameter(String.class, "column")
                .addParameter(Object.class, "value")
                .addParameter(TypeName.LONG, "rowId")
                .returns(TypeName.INT)
                .addStatement("$T affectedRows = 0", TypeName.INT)
                .beginControlFlow("if (rowId > 0 && sClientRef != null)")
                .addStatement("final $T client = sClientRef.get()", ClassName.get("droidkit.sqlite", "SQLiteClient"))
                .beginControlFlow("if (client != null)")
                .addStatement("affectedRows = client.executeUpdateDelete(\"UPDATE $L SET \" + column + \" = ?" +
                        " WHERE _id = ?;\", value, rowId)", mTableName)
                .beginControlFlow("if (affectedRows > 0)")
                .addStatement("$T.notifyChange($T.class)",
                        ClassName.get("droidkit.sqlite", "SQLiteSchema"),
                        ClassName.get(mOriginType))
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return affectedRows")
                .build();
    }
    //endregion

    //region reactive functions
    private static class ColumnBinder implements Func1<String, String> {

        @Override
        public String call(String column) {
            return column + " = ?";
        }

    }

    private static class ObjectField implements Func1<String, String> {

        @Override
        public String call(String field) {
            return "object." + field;
        }

    }

    private static class SetterOnly implements Func1<ExecutableElement, Boolean> {

        private final Map<String, String> mFieldToSetter;

        private SetterOnly(Map<String, String> fieldToSetter) {
            mFieldToSetter = fieldToSetter;
        }

        @Override
        public Boolean call(ExecutableElement method) {
            return mFieldToSetter.containsKey(method.getSimpleName().toString());
        }

    }
    //endregion

    //region javac magic
    private static class SetterTranslator extends TreeTranslator {

        private final TreeMaker mTreeMaker;

        private final Names mNames;

        private final String mPackageName;

        private final String mClassName;

        private final String mFieldName;

        private final String mColumnName;

        private final String mPrimaryKey;

        public SetterTranslator(JavacProcessingEnvironment env, ClassName className, String fieldName,
                                String columnName, String primaryKey) {
            mTreeMaker = TreeMaker.instance(env.getContext());
            mNames = Names.instance(env.getContext());
            mPackageName = className.packageName();
            mClassName = className.simpleName();
            mFieldName = fieldName;
            mColumnName = columnName;
            mPrimaryKey = primaryKey;
        }

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl methodDecl) {
            super.visitMethodDef(methodDecl);
            methodDecl.body.stats = com.sun.tools.javac.util.List.<JCTree.JCStatement>of(
                    mTreeMaker.Try(
                            mTreeMaker.Block(0, methodDecl.body.stats),
                            com.sun.tools.javac.util.List.<JCTree.JCCatch>nil(),
                            mTreeMaker.Block(0, com.sun.tools.javac.util.List.<JCTree.JCStatement>of(
                                    mTreeMaker.Exec(mTreeMaker.Apply(
                                            com.sun.tools.javac.util.List.<JCTree.JCExpression>nil(),
                                            ident(mPackageName, mClassName, "update"),
                                            com.sun.tools.javac.util.List.of(
                                                    JCLiterals.stringValue(mTreeMaker, mColumnName),
                                                    thisIdent(mFieldName), thisIdent(mPrimaryKey)
                                            )
                                    ))
                            ))
                    )
            );
            this.result = methodDecl;
        }

        private JCTree.JCExpression ident(String... selectors) {
            final Iterator<String> iterator = Arrays.asList(selectors).iterator();
            JCTree.JCExpression selector = mTreeMaker.Ident(mNames.fromString(iterator.next()));
            while (iterator.hasNext()) {
                selector = mTreeMaker.Select(selector, mNames.fromString(iterator.next()));
            }
            return selector;
        }

        private JCTree.JCExpression thisIdent(String name) {
            return mTreeMaker.Select(mTreeMaker.Ident(mNames._this), mNames.fromString(name));
        }

    }
    //endregion

}
