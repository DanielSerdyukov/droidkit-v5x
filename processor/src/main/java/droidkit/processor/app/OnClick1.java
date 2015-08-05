package droidkit.processor.app;

import com.squareup.javapoet.CodeBlock;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnClick1 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (params.size() == 1 && env.isSubtype(params.get(0).asType(), "android.view.View")) {
            return CodeBlock.builder()
                    .addStatement("origin.$L(clickedView)", method.getSimpleName())
                    .build();
        }
        return null;
    }

}
