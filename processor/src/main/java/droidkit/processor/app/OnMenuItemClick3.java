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
class OnMenuItemClick3 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (TypeKind.BOOLEAN == method.getReturnType().getKind()
                && params.size() == 1
                && env.isSubtype(params.get(0).asType(), "android.view.MenuItem")) {
            return CodeBlock.builder()
                    .addStatement("return origin.$L(clickedItem)", method.getSimpleName())
                    .build();
        }
        return null;
    }

}
