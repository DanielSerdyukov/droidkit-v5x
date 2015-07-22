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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner7;
import javax.tools.JavaFileObject;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;
import rx.functions.Action3;

/**
 * @author Daniel Serdyukov
 */
class LoaderCallbacksVisitor extends ElementScanner7<Void, Void> {

    private static final Map<Class<? extends Annotation>, MethodInjector> METHOD_INJECTORS = new HashMap<>();

    static {
        METHOD_INJECTORS.put(OnCreateLoader.class, new OnCreateLoaderInjector());
        METHOD_INJECTORS.put(OnLoadFinished.class, new OnLoadFinishedInjector());
        METHOD_INJECTORS.put(OnResetLoader.class, new OnResetLoaderInjector());
    }

    private final Map<Integer, CodeBlock> mOnCreate = new LinkedHashMap<>();

    private final Map<Integer, CodeBlock> mOnLoad = new LinkedHashMap<>();

    private final Map<Integer, CodeBlock> mOnReset = new LinkedHashMap<>();

    private final ProcessingEnvironment mProcessingEnv;

    private final TypeElement mElement;

    private final Trees mTrees;

    private final String mPackageName;

    LoaderCallbacksVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        mProcessingEnv = processingEnv;
        mElement = element;
        mTrees = Trees.instance(processingEnv);
        mPackageName = element.getEnclosingElement().toString();
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Void aVoid) {
        for (final Map.Entry<Class<? extends Annotation>, MethodInjector> entry : METHOD_INJECTORS.entrySet()) {
            final Annotation annotation = e.getAnnotation(entry.getKey());
            if (annotation != null) {
                ((JCTree.JCMethodDecl) mTrees.getTree(e)).mods.flags &= ~Flags.PRIVATE;
                entry.getValue().call(this, e, annotation);
            }
        }
        return super.visitExecutable(e, aVoid);
    }

    void brewJavaClass() {
        try {
            final TypeSpec typeSpec = TypeSpec.classBuilder(mElement.getSimpleName() + "$LC")
                    .addOriginatingElement(mElement)
                    .addSuperinterface(ClassName.get("android.app", "LoaderManager", "LoaderCallbacks"))
                    .addFields(fields())
                    .addMethods(methods())
                    .build();
            final JavaFile javaFile = JavaFile.builder(mPackageName, typeSpec)
                    .addFileComment(Utils.AUTO_GENERATED_FILE)
                    .build();
            final JavaFileObject sourceFile = mProcessingEnv.getFiler().createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, mElement);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Utils.error(mProcessingEnv, mElement, e.getMessage());
        }
    }

    protected List<FieldSpec> fields() {
        return Collections.singletonList(
                FieldSpec.builder(ParameterizedTypeName.get(
                                ClassName.get(Reference.class),
                                ClassName.get(mElement)),
                        "mDelegateRef", Modifier.PRIVATE, Modifier.FINAL)
                        .build()
        );
    }

    protected List<MethodSpec> methods() {
        return Arrays.asList(
                init(),
                onCreateLoader(),
                onLoadFinished(),
                onLoaderReset()
        );
    }

    private MethodSpec init() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(mElement), "delegate")
                .addStatement("mDelegateRef = new $T<>(delegate)", ClassName.get(WeakReference.class))
                .build();
    }

    private MethodSpec onCreateLoader() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("android.content", "Loader"))
                .addParameter(TypeName.INT, "id").addParameter(ClassName.get("android.os", "Bundle"), "args")
                .addStatement("final $T delegate = mDelegateRef.get()", ClassName.get(mElement))
                .beginControlFlow("if (delegate != null)");
        if (mOnCreate.size() == 1) {
            builder.addCode(mOnCreate.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (id)");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnCreate.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue())
                        .unindent().unindent()
                        .build());
            }
            builder.endControlFlow();
        }
        builder.endControlFlow();
        return builder.addStatement("return null").build();
    }

    private MethodSpec onLoadFinished() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoadFinished")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Loader"), "loader")
                .addParameter(ClassName.get(Object.class), "data")
                .addStatement("final $T delegate = mDelegateRef.get()", ClassName.get(mElement))
                .beginControlFlow("if (delegate != null)");
        if (mOnLoad.size() == 1) {
            builder.addCode(mOnLoad.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (loader.getId())");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnLoad.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue())
                        .addStatement("break")
                        .unindent().unindent()
                        .build());
            }
            builder.endControlFlow();
        }
        builder.endControlFlow();
        return builder.build();
    }

    private MethodSpec onLoaderReset() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoaderReset")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.content", "Loader"), "loader")
                .addStatement("final $T delegate = mDelegateRef.get()", ClassName.get(mElement))
                .beginControlFlow("if (delegate != null)");
        if (mOnReset.size() == 1) {
            builder.addCode(mOnReset.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (loader.getId())");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnReset.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue())
                        .addStatement("break")
                        .unindent().unindent()
                        .build());
            }
            builder.endControlFlow();
        }
        builder.endControlFlow();
        return builder.build();
    }

    private void injectOnCreateLoader(ExecutableElement method, int[] loaderIds) {
        for (final int loaderId : loaderIds) {
            final List<? extends VariableElement> parameters = method.getParameters();
            final int parametersCount = parameters.size();
            if (parameters.isEmpty()) {
                mOnCreate.put(loaderId, CodeBlock.builder()
                        .addStatement("return delegate.$L()", method.getSimpleName())
                        .build());
            } else if (parametersCount == 1) {
                final TypeMirror firstParam = parameters.get(0).asType();
                if (TypeKind.INT == firstParam.getKind()) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(id)", method.getSimpleName())
                            .build());
                } else if (Utils.isSubtype(mProcessingEnv, firstParam, "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(args)", method.getSimpleName())
                            .build());
                } else {
                    Utils.error(mProcessingEnv, method,
                            "Unexpected parameter type (expected=(int | Bundle), actual=%s)", firstParam);
                }
            } else if (parametersCount == 2) {
                final TypeMirror firstParam = parameters.get(0).asType();
                final TypeMirror secondParam = parameters.get(1).asType();
                if (TypeKind.INT == firstParam.getKind()
                        && Utils.isSubtype(mProcessingEnv, secondParam, "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(id, args)", method.getSimpleName())
                            .build());
                } else {
                    Utils.error(mProcessingEnv, method,
                            "Unexpected parameters type (expected=(int, Bundle), actual=(%s, %s))",
                            firstParam, secondParam);
                }
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected parameters count (expected=[0, 2], actual=%d)",
                        parametersCount);
            }
        }
    }

    private void injectOnLoadFinished(ExecutableElement method, int[] loaderIds) {
        for (final int loaderId : loaderIds) {
            final List<? extends VariableElement> parameters = method.getParameters();
            final int parametersCount = parameters.size();
            if (parameters.isEmpty()) {
                mOnLoad.put(loaderId, CodeBlock.builder()
                        .addStatement("delegate.$L()", method.getSimpleName())
                        .build());
            } else if (parametersCount == 1) {
                mOnLoad.put(loaderId, CodeBlock.builder()
                        .addStatement("delegate.$L(($L) data)",
                                method.getSimpleName(),
                                parameters.get(0).asType())
                        .build());
            } else if (parametersCount == 2) {
                mOnLoad.put(loaderId, CodeBlock.builder()
                        .addStatement("delegate.$L(($L) loader, ($L) data)",
                                method.getSimpleName(),
                                parameters.get(0).asType(),
                                parameters.get(1).asType())
                        .build());
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected parameters count (expected=[0, 2], actual=%d)",
                        parametersCount);
            }
        }
    }

    private void injectOnResetLoader(ExecutableElement method, int[] loaderIds) {
        for (final int loaderId : loaderIds) {
            final List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.isEmpty()) {
                mOnReset.put(loaderId, CodeBlock.builder()
                        .addStatement("delegate.$L()", method.getSimpleName())
                        .build());
            } else if (parameters.size() == 1) {
                mOnReset.put(loaderId, CodeBlock.builder()
                        .addStatement("delegate.$L(($L) loader)",
                                method.getSimpleName(),
                                parameters.get(0).asType())
                        .build());
            } else {
                Utils.error(mProcessingEnv, method, "Unexpected parameters count (expected=[0, 1], actual=%d)",
                        parameters.size());
            }
        }
    }

    //region Injectors
    private interface MethodInjector extends Action3<LoaderCallbacksVisitor, ExecutableElement, Annotation> {

    }

    private static class OnCreateLoaderInjector implements MethodInjector {

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Annotation annotation) {
            visitor.injectOnCreateLoader(method, ((OnCreateLoader) annotation).value());
        }
    }

    private static class OnLoadFinishedInjector implements MethodInjector {

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Annotation annotation) {
            visitor.injectOnLoadFinished(method, ((OnLoadFinished) annotation).value());
        }
    }

    private static class OnResetLoaderInjector implements MethodInjector {

        @Override
        public void call(LoaderCallbacksVisitor visitor, ExecutableElement method, Annotation annotation) {
            visitor.injectOnResetLoader(method, ((OnResetLoader) annotation).value());
        }
    }
    //endregion

}
