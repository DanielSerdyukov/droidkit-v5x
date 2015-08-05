package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnCreateLoader0 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return CodeBlock.builder().addStatement("return referent.$L()", method.getSimpleName()).build();
        }
        return null;
    }

}
