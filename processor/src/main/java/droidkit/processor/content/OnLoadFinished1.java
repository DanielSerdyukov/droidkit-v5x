package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnLoadFinished1 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() == 1) {
            return CodeBlock.builder()
                    .addStatement("referent.$L(($L) result)", method.getSimpleName(),
                            parameters.get(0).asType())
                    .build();
        }
        return null;
    }

}
