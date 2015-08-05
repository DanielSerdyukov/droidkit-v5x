package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import droidkit.annotation.OnActionClick;
import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnActionClickVisitor implements MethodVisitor {

    @Override
    public Annotation getAnnotation(ProcessingEnv processingEnv, ExecutableElement method) {
        return method.getAnnotation(OnActionClick.class);
    }

    @Override
    public void visit(LifecycleScanner scanner, ExecutableElement method, Annotation annotation) {
        final int[] itemIds = ((OnActionClick) annotation).value();
        for (final int itemId : itemIds) {
            scanner.onActionClick().add(setupOnActionClick(scanner, method, itemId));
        }
    }

    private MethodSpec setupOnActionClick(LifecycleScanner scanner, ExecutableElement method, int itemId) {
        return MethodSpec.methodBuilder("setupActionOnClick" + itemId)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("final $1T origin = ($1T) this",
                        ClassName.get((TypeElement) method.getEnclosingElement()))
                .addCode(CodeBlock.builder()
                        .add("mOnActionClick.put($L, new $T() {\n", itemId,
                                ClassName.get("android.view", "MenuItem", "OnMenuItemClickListener"))
                        .indent()
                        .add("@Override\n")
                        .beginControlFlow("public boolean onMenuItemClick($T clickedItem)",
                                ClassName.get("android.view", "MenuItem"))
                        .add(originCall(scanner, method))
                        .endControlFlow()
                        .unindent()
                        .add("});\n")
                        .build())
                .build();
    }

    private CodeBlock originCall(LifecycleScanner scanner, ExecutableElement method) {
        for (final Signature signature : Signature.ON_ACTION_CLICK) {
            final CodeBlock originCall = signature.call(scanner.getEnv(), method);
            if (originCall != null) {
                return originCall;
            }
        }
        scanner.getEnv().printMessage(Diagnostic.Kind.ERROR, method, "Unexpected method signature");
        return CodeBlock.builder().addStatement("return false").build();
    }

}
