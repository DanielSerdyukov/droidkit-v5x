package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
abstract class LifecycleProxyMaker {

    protected static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");

    protected static final ClassName ANDROID_BUNDLE = ClassName.get("android.os", "Bundle");

    protected static final ClassName DROIDKIT_VIEWS = ClassName.get("droidkit.view", "Views");

    protected final ProcessingEnvironment mProcessingEnv;

    protected final TypeElement mElement;

    private final String mPackageName;

    public LifecycleProxyMaker(ProcessingEnvironment processingEnv, TypeElement element) {
        mProcessingEnv = processingEnv;
        mElement = element;
        mPackageName = element.getEnclosingElement().toString();
    }

    abstract void injectView(String fieldName, int viewId);

    void brewJavaClass() {
        try {
            final TypeSpec typeSpec = TypeSpec.classBuilder(getClassName())
                    .addModifiers(Modifier.ABSTRACT)
                    .superclass(ClassName.get(mElement.getSuperclass()))
                    .addOriginatingElement(mElement)
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

    String getPackageName() {
        return mPackageName;
    }

    String getClassName() {
        return mElement.getSimpleName() + "$Proxy";
    }

    protected List<FieldSpec> fields() {
        return ImmutableList.of();
    }

    protected List<MethodSpec> methods() {
        return ImmutableList.of();
    }

}
