package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnCreateLoader1 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() == 1) {
            final TypeMirror paramType = parameters.get(0).asType();
            if (TypeKind.INT == paramType.getKind()) {
                return CodeBlock.builder()
                        .addStatement("return referent.$L(loaderId)", method.getSimpleName())
                        .build();
            } else if (env.isSubtype(paramType, "android.os.Bundle")) {
                return CodeBlock.builder()
                        .addStatement("return referent.$L(args)", method.getSimpleName())
                        .build();
            }
        }
        return null;
    }

}
