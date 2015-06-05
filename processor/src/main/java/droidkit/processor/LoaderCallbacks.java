package droidkit.processor;

import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
class LoaderCallbacks implements JavaClassMaker {

    private final Map<Integer, CodeBlock> mOnCreate = new HashMap<>();

    private final Map<Integer, CodeBlock> mOnLoad = new HashMap<>();

    private final Map<Integer, CodeBlock> mOnReset = new HashMap<>();

    private final TypeElement mOriginElement;

    private boolean mSupport;

    public LoaderCallbacks(TypeElement originElement) {
        mOriginElement = originElement;
    }

    void tryInject(ExecutableElement element, OnCreateLoader loader) {
        if (loader != null) {
            JCUtils.<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            mSupport = loader.support();
            final List<? extends VariableElement> parameters = element.getParameters();
            for (final int loaderId : loader.value()) {
                if (parameters.isEmpty()) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return mDelegate.$L()", element.getSimpleName())
                            .build());
                    continue;
                }
                if (parameters.size() == 1 &&
                        JCUtils.isSubtype(parameters.get(0), "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return mDelegate.$L(args)", element.getSimpleName())
                            .build());
                    continue;
                }
                if (parameters.size() == 1 &&
                        TypeKind.INT == parameters.get(0).asType().getKind()) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return mDelegate.$L(id)", element.getSimpleName())
                            .build());
                    continue;
                }
                if (parameters.size() == 2 &&
                        TypeKind.INT == parameters.get(0).asType().getKind() &&
                        JCUtils.isSubtype(parameters.get(1), "android.os.Bundle")) {
                    mOnCreate.put(loaderId, CodeBlock.builder()
                            .addStatement("return mDelegate.$L(id, args)", element.getSimpleName())
                            .build());
                    continue;
                }
                JCUtils.error("Unexpected method signature. Expected () | (Bundle)", element);
            }
        }
    }

    void tryInject(ExecutableElement element, OnLoadFinished loader) {
        if (loader != null) {
            JCUtils.<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            mSupport = loader.support();
            final List<? extends VariableElement> parameters = element.getParameters();
            for (final int loaderId : loader.value()) {
                if (parameters.isEmpty()) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("mDelegate.$L()", element.getSimpleName())
                            .build());
                    continue;
                }
                if (parameters.size() == 1) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("mDelegate.$L(($L) data)",
                                    element.getSimpleName(),
                                    parameters.get(0).asType())
                            .build());
                    continue;
                }
                if (parameters.size() == 2) {
                    mOnLoad.put(loaderId, CodeBlock.builder()
                            .addStatement("mDelegate.$L(($L) loader, ($L) data)",
                                    element.getSimpleName(),
                                    parameters.get(0).asType(),
                                    parameters.get(1).asType())
                            .build());
                    continue;
                }
                JCUtils.error("Unexpected method signature. Expected () | (T) | (Loader<T>, T)", element);
            }
        }
    }

    void tryInject(ExecutableElement element, OnResetLoader loader) {
        if (loader != null) {
            JCUtils.<JCTree.JCMethodDecl>getTree(element).mods.flags &= ~Flags.PRIVATE;
            mSupport = loader.support();
            final List<? extends VariableElement> parameters = element.getParameters();
            for (final int loaderId : loader.value()) {
                if (parameters.isEmpty()) {
                    mOnReset.put(loaderId, CodeBlock.builder()
                            .addStatement("mDelegate.$L()", element.getSimpleName())
                            .build());
                    continue;
                }
                if (parameters.size() == 1) {
                    mOnReset.put(loaderId, CodeBlock.builder()
                            .addStatement("mDelegate.$L(($L) loader)",
                                    element.getSimpleName(),
                                    parameters.get(0).asType())
                            .build());
                    continue;
                }
                JCUtils.error("Unexpected method signature. Expected () | (Loader<T>)", element);
            }
        }
    }

    @Override
    public void makeJavaFile() throws IOException {
        for (final Map.Entry<Integer, CodeBlock> entry : mOnCreate.entrySet()) {
            final Integer loaderId = entry.getKey();
            final TypeSpec.Builder builder = TypeSpec
                    .classBuilder(mOriginElement.getSimpleName() + "$LC" + loaderId)
                    .addModifiers(Modifier.PUBLIC)
                    .addOriginatingElement(mOriginElement);
            if (mSupport) {
                builder.addSuperinterface(ClassName.get("android.support.v4.app", "LoaderManager", "LoaderCallbacks"));
            } else {
                builder.addSuperinterface(ClassName.get("android.app", "LoaderManager", "LoaderCallbacks"));
            }
            final TypeSpec spec = builder
                    .addField(makeDelegate())
                    .addMethod(makeInit())
                    .addMethod(makeOnCreateLoader(entry.getValue()))
                    .addMethod(makeOnLoadFinished(loaderId))
                    .addMethod(makeOnLoaderReset(loaderId))
                    .build();
            final JavaFile javaFile = JavaFile.builder(mOriginElement.getEnclosingElement().toString(), spec)
                    .addFileComment(AUTO_GENERATED)
                    .build();
            final JavaFileObject sourceFile = JCUtils.ENV.getFiler()
                    .createSourceFile(javaFile.packageName + "." + spec.name, mOriginElement);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        }
    }

    private FieldSpec makeDelegate() {
        return FieldSpec.builder(ClassName.get(mOriginElement), "mDelegate")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private MethodSpec makeInit() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(mOriginElement), "delegate")
                .addStatement("mDelegate = delegate")
                .build();
    }

    private MethodSpec makeOnCreateLoader(CodeBlock block) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateLoader")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);
        if (mSupport) {
            builder.returns(ClassName.get("android.support.v4.content", "Loader"));
        } else {
            builder.returns(ClassName.get("android.content", "Loader"));
        }
        return builder.addParameter(TypeName.INT, "id")
                .addParameter(ClassName.get("android.os", "Bundle"), "args")
                .addCode(block)
                .build();
    }

    private MethodSpec makeOnLoadFinished(int loaderId) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoadFinished")
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addModifiers(Modifier.PUBLIC);
        if (mSupport) {
            builder.addParameter(ClassName.get("android.support.v4.content", "Loader"), "loader");
        } else {
            builder.addParameter(ClassName.get("android.content", "Loader"), "loader");
        }
        builder.addParameter(ClassName.get(Object.class), "data");
        final CodeBlock codeBlock = mOnLoad.get(loaderId);
        if (codeBlock != null) {
            builder.addCode(codeBlock);
        }
        return builder.build();
    }

    private MethodSpec makeOnLoaderReset(int loaderId) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onLoaderReset")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build());
        if (mSupport) {
            builder.addParameter(ClassName.get("android.support.v4.content", "Loader"), "loader");
        } else {
            builder.addParameter(ClassName.get("android.content", "Loader"), "loader");
        }
        final CodeBlock codeBlock = mOnReset.get(loaderId);
        if (codeBlock != null) {
            builder.addCode(codeBlock);
        }
        return builder.build();
    }

}
