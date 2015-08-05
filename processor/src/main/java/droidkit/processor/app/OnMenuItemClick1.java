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
class OnMenuItemClick1 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv processingEnv, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (TypeKind.BOOLEAN == method.getReturnType().getKind() && params.isEmpty()) {
            return CodeBlock.builder()
                    .addStatement("return origin.$L()", method.getSimpleName())
                    .build();
        }
        return null;
    }

}
