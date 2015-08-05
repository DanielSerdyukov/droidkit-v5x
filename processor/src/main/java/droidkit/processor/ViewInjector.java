package droidkit.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
public class ViewInjector {

    private final CodeBlock.Builder mViewById = CodeBlock.builder();

    public void findById(String format, Object... args) {
        mViewById.addStatement(format, args);
    }

    public ClassName brewJava(ProcessingEnv env, TypeElement originType) {
        final TypeSpec typeSpec = TypeSpec.classBuilder(originType.getSimpleName() + "$ViewInjector")
                .addModifiers(Modifier.PUBLIC)
                .addOriginatingElement(originType)
                .addMethod(activityInject(originType))
                .addMethod(dialogInject(originType))
                .addMethod(viewInject(originType))
                .build();
        final JavaFile javaFile = JavaFile.builder(originType.getEnclosingElement().toString(), typeSpec)
                .addFileComment(ElementScanner.AUTO_GENERATED_FILE)
                .build();
        try {
            final JavaFileObject sourceFile = env.createSourceFile(
                    javaFile.packageName + "." + typeSpec.name, originType);
            try (final Writer writer = new BufferedWriter(sourceFile.openWriter())) {
                javaFile.writeTo(writer);
            }
        } catch (IOException e) {
            Logger.getGlobal().throwing(ViewInjector.class.getName(), "brewJava", e);
        }
        return ClassName.get(javaFile.packageName, typeSpec.name);
    }

    private MethodSpec activityInject(TypeElement element) {
        return MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.app", "Activity"), "root")
                .addParameter(ClassName.get(element), "target")
                .addCode(mViewById.build())
                .build();
    }

    private MethodSpec dialogInject(TypeElement element) {
        return MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.app", "Dialog"), "root")
                .addParameter(ClassName.get(element), "target")
                .addCode(mViewById.build())
                .build();
    }

    private MethodSpec viewInject(TypeElement element) {
        return MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.view", "View"), "root")
                .addParameter(ClassName.get(element), "target")
                .addCode(mViewById.build())
                .build();
    }

}
