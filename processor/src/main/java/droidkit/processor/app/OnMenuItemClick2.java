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
class OnMenuItemClick2 implements Signature {

    @Override
    public CodeBlock call(ProcessingEnv env, ExecutableElement method) {
        final List<? extends VariableElement> params = method.getParameters();
        if (TypeKind.VOID == method.getReturnType().getKind()
                && params.size() == 1
                && env.isSubtype(params.get(0).asType(), "android.view.MenuItem")) {
            return CodeBlock.builder()
                    .addStatement("origin.$L(clickedItem)", method.getSimpleName())
                    .addStatement("return true", method.getSimpleName())
                    .build();
        }
        return null;
    }

}
