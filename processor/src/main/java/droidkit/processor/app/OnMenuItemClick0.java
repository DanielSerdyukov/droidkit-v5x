package droidkit.processor.app;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnMenuItemClick0 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv processingEnv, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (TypeKind.VOID == method.getReturnType().getKind() && params.isEmpty()) {
            return CodeBlock.builder()
                    .addStatement("origin.$L()", method.getSimpleName())
                    .addStatement("return true")
                    .build();
        }
        return null;
    }

}
