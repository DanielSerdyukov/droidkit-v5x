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
class OnCreateLoader2 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() == 2) {
            final TypeMirror firstParamType = parameters.get(0).asType();
            final TypeMirror secondParamType = parameters.get(1).asType();
            if (TypeKind.INT == firstParamType.getKind()
                    && env.isSubtype(secondParamType, "android.os.Bundle")) {
                return CodeBlock.builder()
                        .addStatement("return referent.$L(loaderId, args)", method.getSimpleName())
                        .build();
            } else if (env.isSubtype(firstParamType, "android.os.Bundle")
                    && TypeKind.INT == secondParamType.getKind()) {
                return CodeBlock.builder()
                        .addStatement("return referent.$L(args, loaderId)", method.getSimpleName())
                        .build();
            }
        }
        return null;
    }

}
