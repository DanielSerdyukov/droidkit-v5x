package droidkit.processor.app;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnClick0 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (params.isEmpty()) {
            return CodeBlock.builder()
                    .addStatement("origin.$L()", method.getSimpleName())
                    .build();
        }
        return null;
    }

}
