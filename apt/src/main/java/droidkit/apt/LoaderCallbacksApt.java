package droidkit.apt;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;

/**
 * @author Daniel Serdyukov
 */
class LoaderCallbacksApt implements Apt {

    private final AtomicBoolean mProcessSingle = new AtomicBoolean();

    private final Map<Integer, CodeBlock> mOnCreate = new HashMap<>();

    private final Map<Integer, CodeBlock> mOnLoad = new HashMap<>();

    private final Map<Integer, CodeBlock> mOnReset = new HashMap<>();

    private final TypeElement mElement;

    private final ClassName mClassName;

    private boolean mSupportImpl;

    public LoaderCallbacksApt(TypeElement element) {
        mElement = element;
        mClassName = ClassName.get(element);
    }

    @Override
    public void process(RoundEnvironment roundEnv) {
        if (mProcessSingle.compareAndSet(false, true)) {
            final List<? extends Element> elements = mElement.getEnclosedElements();
            for (final Element element : elements) {
                if (ElementKind.METHOD == element.getKind()) {
                    tryInjectOnCreateLoader((ExecutableElement) element, element.getAnnotation(OnCreateLoader.class));
                    tryInjectOnLoadFinished((ExecutableElement) element, element.getAnnotation(OnLoadFinished.class));
                    tryInjectOnLoaderReset((ExecutableElement) element, element.getAnnotation(OnResetLoader.class));
                }
            }
        }
    }

    @Override
    public void finishProcessing() throws IOException {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(mElement.getSimpleName() + "$LC");
        if (mSupportImpl) {
            builder.addSuperinterface(ClassName.get("android.support.v4.app", "LoaderManager", "LoaderCallbacks"));
        } else {
            builder.addSuperinterface(ClassName.get("android.app", "LoaderManager", "LoaderCallbacks"));
        }
        builder.addField(FieldSpec
                .builder(ParameterizedTypeName.get(ClassName.get(Reference.class), mClassName),
                        "mDelegateRef", Modifier.PRIVATE, Modifier.FINAL)
                .build());
        builder.addMethod(init());
        builder.addMethod(onCreateLoader());
        builder.addMethod(onLoadFinished());
        builder.addMethod(onLoaderReset());
        final TypeSpec spec = builder.build();
        final JavaFile javaFile = JavaFile.builder(mElement.getEnclosingElement().toString(), spec)
                .addFileComment(AUTO_GENERATED)
                .build();
        final JavaFileObject sourceFile = JavacEnv.createSourceFile(javaFile, spec, mElement);
        try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
            javaFile.writeTo(writer);
        }
    }

    private MethodSpec init() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mClassName, "delegate")
                .addStatement("mDelegateRef = new $T<>(delegate)", ClassName.get(WeakReference.class))
                .build();
    }

    private MethodSpec onCreateLoader() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);
        if (mSupportImpl) {
            builder.returns(ClassName.get("android.support.v4.content", "Loader"));
        } else {
            builder.returns(ClassName.get("android.content", "Loader"));
        }
        builder.addParameter(TypeName.INT, "id").addParameter(ClassName.get("android.os", "Bundle"), "args");
        builder.addStatement("final $T delegate = mDelegateRef.get()", mClassName);
        builder.beginControlFlow("if (delegate != null)");
        if (mOnCreate.size() == 1) {
            builder.addCode(mOnCreate.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (id)");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnCreate.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue()).unindent().unindent()
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
                .addModifiers(Modifier.PUBLIC);
        if (mSupportImpl) {
            builder.addParameter(ClassName.get("android.support.v4.content", "Loader"), "loader");
        } else {
            builder.addParameter(ClassName.get("android.content", "Loader"), "loader");
        }
        builder.addParameter(ClassName.get(Object.class), "data");
        builder.addStatement("final $T delegate = mDelegateRef.get()", mClassName);
        builder.beginControlFlow("if (delegate != null)");
        if (mOnLoad.size() == 1) {
            builder.addCode(mOnLoad.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (loader.getId())");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnLoad.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue()).unindent().unindent()
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
                .addModifiers(Modifier.PUBLIC);
        if (mSupportImpl) {
            builder.addParameter(ClassName.get("android.support.v4.content", "Loader"), "loader");
        } else {
            builder.addParameter(ClassName.get("android.content", "Loader"), "loader");
        }
        builder.addStatement("final $T delegate = mDelegateRef.get()", mClassName);
        builder.beginControlFlow("if (delegate != null)");
        if (mOnReset.size() == 1) {
            builder.addCode(mOnReset.values().iterator().next());
        } else {
            builder.beginControlFlow("switch (loader.getId())");
            for (final Map.Entry<Integer, CodeBlock> entry : mOnReset.entrySet()) {
                builder.addCode(CodeBlock.builder()
                        .indent().add("case $L:\n", entry.getKey())
                        .indent().add(entry.getValue()).unindent().unindent()
                        .build());
            }
            builder.endControlFlow();
        }
        builder.endControlFlow();
        return builder.build();
    }

    private void tryInjectOnCreateLoader(ExecutableElement method, OnCreateLoader annotation) {
        if (annotation != null) {
            mSupportImpl = annotation.support();
            JavacEnv.<JCTree.JCMethodDecl>getTree(method).mods.flags &= ~Flags.PRIVATE;
            for (final int loaderId : annotation.value()) {
                final List<? extends VariableElement> parameters = method.getParameters();
                if (parameters.isEmpty()) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L()", method.getSimpleName())
                            .build());
                } else if (parameters.size() == 1 &&
                        TypeKind.INT == parameters.get(0).asType().getKind()) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(id)", method.getSimpleName())
                            .build());
                } else if (parameters.size() == 1 &&
                        JavacEnv.isSubtype(parameters.get(0), "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(args)", method.getSimpleName())
                            .build());
                } else if (parameters.size() == 2 &&
                        TypeKind.INT == parameters.get(0).asType().getKind() &&
                        JavacEnv.isSubtype(parameters.get(1), "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return delegate.$L(id, args)", method.getSimpleName())
                            .build());
                } else {
                    JavacEnv.logE(method, "Unexpected method signature.");
                }
            }
        }
    }

    private void tryInjectOnLoadFinished(ExecutableElement method, OnLoadFinished annotation) {
        if (annotation != null) {
            JavacEnv.<JCTree.JCMethodDecl>getTree(method).mods.flags &= ~Flags.PRIVATE;
            for (final int loaderId : annotation.value()) {
                final List<? extends VariableElement> parameters = method.getParameters();
                if (parameters.isEmpty()) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("delegate.$L()", method.getSimpleName())
                            .build());
                } else if (parameters.size() == 1) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("delegate.$L(($L) data)",
                                    method.getSimpleName(),
                                    parameters.get(0).asType())
                            .build());
                } else if (parameters.size() == 2) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("delegate.$L(($L) loader, ($L) data)",
                                    method.getSimpleName(),
                                    parameters.get(0).asType(),
                                    parameters.get(1).asType())
                            .build());
                } else {
                    JavacEnv.logE(method, "Unexpected method signature.");
                }
            }
        }
    }

    private void tryInjectOnLoaderReset(ExecutableElement method, OnResetLoader annotation) {
        if (annotation != null) {
            JavacEnv.<JCTree.JCMethodDecl>getTree(method).mods.flags &= ~Flags.PRIVATE;
            for (final int loaderId : annotation.value()) {
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
                    JavacEnv.logE(method, "Unexpected method signature.");
                }
            }
        }
    }

}
