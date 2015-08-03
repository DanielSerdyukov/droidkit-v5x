package droidkit.javac;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;
import rx.functions.Action3;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * @author Daniel Serdyukov
 */
class LoaderCallbacksVisitor extends ElementScanner {

    private static final List<MethodVisitor> VISITORS = Arrays.asList(
            new OnCreateLoaderVisitor(),
            new OnLoadFinishedVisitor(),
            new OnResetLoaderVisitor()
    );

    private static final Map<Class<? extends Annotation>, List<CodeSignature>> SIGNATURES = new HashMap<>();

    static {
        SIGNATURES.put(OnCreateLoader.class, Arrays.asList(
                new OnCreateLoader0(),
                new OnCreateLoader1(),
                new OnCreateLoader2()
        ));
        SIGNATURES.put(OnLoadFinished.class, Arrays.asList(
                new OnLoadFinished0(),
                new OnLoadFinished1(),
                new OnLoadFinished2()
        ));
        SIGNATURES.put(OnResetLoader.class, Arrays.asList(
                new OnLoaderReset0(),
                new OnLoaderReset1()
        ));
    }

    private final List<CodeBlock> mOnCreateLoader = new ArrayList<>();

    private final List<CodeBlock> mOnLoadFinished = new ArrayList<>();

    private final List<CodeBlock> mOnLoaderReset = new ArrayList<>();

    private final Trees mTrees;

    private TypeElement mOriginElement;

    public LoaderCallbacksVisitor(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        mTrees = Trees.instance(processingEnv);
    }

    @Override
    public Void visitType(TypeElement e, Void aVoid) {
        if (mOriginElement == null) {
            mOriginElement = e;
        }
        return super.visitType(e, aVoid);
    }

    @Override
    public Void visitExecutable(ExecutableElement method, Void aVoid) {
        for (final MethodVisitor visitor : VISITORS) {
            final int[] loaderIds = visitor.call(method);
            if (loaderIds.length > 0) {
                ((JCTree.JCMethodDecl) mTrees.getTree(method)).mods.flags &= ~Flags.PRIVATE;
            }
            for (final int loaderId : loaderIds) {
                visitor.call(this, method, loaderId);
            }
        }
        return super.visitExecutable(method, aVoid);
    }

    @Override
    void brewJava() {
        final Element packageElement = mOriginElement.getEnclosingElement();
        if (ElementKind.PACKAGE == packageElement.getKind()) {
            final TypeSpec typeSpec = TypeSpec.classBuilder(mOriginElement.getSimpleName() + "$LC")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                            .addMember("value", "$S", "unchecked")
                            .build())
                    .addSuperinterface(ClassName.get("android.app", "LoaderManager", "LoaderCallbacks"))
                    .addOriginatingElement(mOriginElement)
                    .addField(targetRef())
                    .addMethod(constructor())
                    .addMethod(onCreateLoader())
                    .addMethod(onLoadFinished())
                    .addMethod(onLoaderReset())
                    .build();
            final JavaFile javaFile = JavaFile.builder(packageElement.toString(), typeSpec)
                    .addFileComment(AUTO_GENERATED_FILE)
                    .build();
            try {
                final JavaFileObject sourceFile = createSourceFile(javaFile.packageName + "." + typeSpec.name,
                        mOriginElement);
                try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                    javaFile.writeTo(writer);
                }
            } catch (IOException e) {
                printMessage(Diagnostic.Kind.ERROR, mOriginElement, e.getMessage());
            }
        }
    }

    private void unexpectedMethodSignature(ExecutableElement method) {
        printMessage(Diagnostic.Kind.ERROR, method, "Unexpected method signature");
    }

    //region implementation
    private FieldSpec targetRef() {
        return FieldSpec.builder(ParameterizedTypeName.get(
                        ClassName.get(Reference.class),
                        ClassName.get(mOriginElement)),
                "mTargetRef", Modifier.PRIVATE, Modifier.FINAL
        ).build();
    }

    private MethodSpec constructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(mOriginElement), "referent")
                .addStatement("mTargetRef = new $T<>(referent)", ClassName.get(WeakReference.class))
                .build();
    }

    private MethodSpec onCreateLoader() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.content", "Loader"))
                .addParameter(TypeName.INT, "loaderId")
                .addParameter(ClassName.get("android.os", "Bundle"), "args")
                .addStatement("final $T referent = mTargetRef.get()", ClassName.get(mOriginElement))
                .beginControlFlow("if (referent != null)")
                .beginControlFlow("switch (loaderId)");
        for (final CodeBlock caseBlock : mOnCreateLoader) {
            builder.addCode(caseBlock);
        }
        return builder
                .addCode(CodeBlock.builder()
                        .add("default:\n").indent()
                        .addStatement("return null").unindent()
                        .build())
                .endControlFlow()
                .endControlFlow()
                .addStatement("throw new $T($S)", ClassName.get(IllegalStateException.class),
                        "It seems LoaderManager leaked")
                .build();
    }

    private MethodSpec onLoadFinished() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoadFinished")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Loader"), "loader")
                .addParameter(ClassName.get(Object.class), "result")
                .addStatement("final $T referent = mTargetRef.get()", ClassName.get(mOriginElement))
                .beginControlFlow("if (referent != null)")
                .beginControlFlow("switch (loader.getId())");
        for (final CodeBlock caseBlock : mOnLoadFinished) {
            builder.addCode(caseBlock);
        }
        return builder.endControlFlow()
                .endControlFlow()
                .build();
    }

    private MethodSpec onLoaderReset() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoaderReset")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Loader"), "loader")
                .addStatement("final $T referent = mTargetRef.get()", ClassName.get(mOriginElement))
                .beginControlFlow("if (referent != null)")
                .beginControlFlow("switch (loader.getId())");
        for (final CodeBlock caseBlock : mOnLoaderReset) {
            builder.addCode(caseBlock);
        }
        return builder.endControlFlow()
                .endControlFlow()
                .build();
    }
    //endregion

    private interface MethodVisitor extends Func1<ExecutableElement, int[]>,
            Action3<LoaderCallbacksVisitor, ExecutableElement, Integer> {

    }

    private interface CodeSignature extends Func2<LoaderCallbacksVisitor, ExecutableElement, CodeBlock> {
    }

    //region annotation visitors
    private static class OnCreateLoaderVisitor implements MethodVisitor {
        @Override
        public int[] call(ExecutableElement method) {
            final OnCreateLoader annotation = method.getAnnotation(OnCreateLoader.class);
            if (annotation != null) {
                return annotation.value();
            }
            return new int[0];
        }

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Integer loaderId) {
            final List<CodeSignature> signatures = SIGNATURES.get(OnCreateLoader.class);
            for (final CodeSignature signature : signatures) {
                final CodeBlock codeBlock = signature.call(visitor, method);
                if (codeBlock != null) {
                    final CodeBlock.Builder caseBlock = CodeBlock.builder();
                    caseBlock.add("case $L:\n", loaderId).indent();
                    caseBlock.add(codeBlock);
                    caseBlock.unindent();
                    visitor.mOnCreateLoader.add(caseBlock.build());
                    return;
                }
            }
            visitor.unexpectedMethodSignature(method);
        }
    }

    private static class OnLoadFinishedVisitor implements MethodVisitor {
        @Override
        public int[] call(ExecutableElement method) {
            final OnLoadFinished annotation = method.getAnnotation(OnLoadFinished.class);
            if (annotation != null) {
                return annotation.value();
            }
            return new int[0];
        }

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Integer loaderId) {
            final List<CodeSignature> signatures = SIGNATURES.get(OnLoadFinished.class);
            for (final CodeSignature signature : signatures) {
                final CodeBlock codeBlock = signature.call(visitor, method);
                if (codeBlock != null) {
                    final CodeBlock.Builder caseBlock = CodeBlock.builder();
                    caseBlock.add("case $L:\n", loaderId).indent();
                    caseBlock.add(codeBlock);
                    caseBlock.addStatement("break").unindent();
                    visitor.mOnLoadFinished.add(caseBlock.build());
                    return;
                }
            }
            visitor.unexpectedMethodSignature(method);
        }
    }

    private static class OnResetLoaderVisitor implements MethodVisitor {
        @Override
        public int[] call(ExecutableElement method) {
            final OnResetLoader annotation = method.getAnnotation(OnResetLoader.class);
            if (annotation != null) {
                return annotation.value();
            }
            return new int[0];
        }

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Integer loaderId) {
            final List<CodeSignature> signatures = SIGNATURES.get(OnResetLoader.class);
            for (final CodeSignature signature : signatures) {
                final CodeBlock codeBlock = signature.call(visitor, method);
                if (codeBlock != null) {
                    final CodeBlock.Builder caseBlock = CodeBlock.builder();
                    caseBlock.add("case $L:\n", loaderId).indent();
                    caseBlock.add(codeBlock);
                    caseBlock.addStatement("break").unindent();
                    visitor.mOnLoaderReset.add(caseBlock.build());
                    return;
                }
            }
            visitor.unexpectedMethodSignature(method);
        }
    }
    //endregion

    //region onCreateLoader
    private static class OnCreateLoader0 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.isEmpty()) {
                return CodeBlock.builder().addStatement("return referent.$L()", method.getSimpleName()).build();
            }
            return null;
        }

    }

    private static class OnCreateLoader1 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 1) {
                final TypeMirror paramType = parameters.get(0).asType();
                if (TypeKind.INT == paramType.getKind()) {
                    return CodeBlock.builder()
                            .addStatement("return referent.$L(loaderId)", method.getSimpleName())
                            .build();
                } else if (visitor.isSubtype(paramType, "android.os.Bundle")) {
                    return CodeBlock.builder()
                            .addStatement("return referent.$L(args)", method.getSimpleName())
                            .build();
                }
            }
            return null;
        }

    }

    private static class OnCreateLoader2 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 2) {
                final TypeMirror firstParamType = parameters.get(0).asType();
                final TypeMirror secondParamType = parameters.get(1).asType();
                if (TypeKind.INT == firstParamType.getKind()
                        && visitor.isSubtype(secondParamType, "android.os.Bundle")) {
                    return CodeBlock.builder()
                            .addStatement("return referent.$L(loaderId, args)", method.getSimpleName())
                            .build();
                } else if (visitor.isSubtype(firstParamType, "android.os.Bundle")
                        && TypeKind.INT == secondParamType.getKind()) {
                    return CodeBlock.builder()
                            .addStatement("return referent.$L(args, loaderId)", method.getSimpleName())
                            .build();
                }
            }
            return null;
        }

    }
    //endregion

    //region onLoadFinished
    private static class OnLoadFinished0 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.isEmpty()) {
                return CodeBlock.builder().addStatement("referent.$L()", method.getSimpleName()).build();
            }
            return null;
        }

    }

    private static class OnLoadFinished1 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 1) {
                return CodeBlock.builder()
                        .addStatement("referent.$L(($L) result)", method.getSimpleName(),
                                parameters.get(0).asType())
                        .build();
            }
            return null;
        }

    }

    private static class OnLoadFinished2 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 2) {
                return CodeBlock.builder()
                        .addStatement("referent.$L(($L) loader, ($L) result)",
                                method.getSimpleName(),
                                parameters.get(0).asType(),
                                parameters.get(1).asType())
                        .build();
            }
            return null;
        }

    }
    //endregion

    //region onResetLoader
    private static class OnLoaderReset0 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.isEmpty()) {
                return CodeBlock.builder()
                        .addStatement("referent.$L()", method.getSimpleName())
                        .build();
            }
            return null;
        }

    }

    private static class OnLoaderReset1 implements CodeSignature {

        @Override
        public CodeBlock call(LoaderCallbacksVisitor visitor, ExecutableElement method) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 1) {
                return CodeBlock.builder()
                        .addStatement("referent.$L(($L) loader)", method.getSimpleName(), parameters.get(0).asType())
                        .build();
            }
            return null;
        }

    }
    //endregion

}
