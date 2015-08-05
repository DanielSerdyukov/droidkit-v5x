package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import droidkit.annotation.OnClick;
import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class OnClickVisitor implements MethodVisitor {

    @Override
    public Annotation getAnnotation(ProcessingEnv processingEnv, ExecutableElement field) {
        return field.getAnnotation(OnClick.class);
    }

    @Override
    public void visit(LifecycleScanner scanner, ExecutableElement method, Annotation annotation) {
        final int[] viewIds = ((OnClick) annotation).value();
        for (final int viewId : viewIds) {
            scanner.onClick().add(setupOnClick(scanner, method, viewId));
        }
    }

    private MethodSpec setupOnClick(LifecycleScanner scanner, ExecutableElement method, int viewId) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("mOnClick.put(view, new $T() {\n", ClassName.get("android.view", "View", "OnClickListener"))
                .indent()
                .add("@Override\n")
                .beginControlFlow("public void onClick($T clickedView)", ClassName.get("android.view", "View"))
                .add(originCall(scanner, method))
                .endControlFlow()
                .unindent()
                .add("});\n");
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("setupOnClick" + viewId)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.get("android.view", "View"), "rootView")
                .addStatement("final $1T origin = ($1T) this",
                        ClassName.get((TypeElement) method.getEnclosingElement()))
                .addCode(scanner.viewFinder().call(viewId))
                .beginControlFlow("if (view != null)")
                .addCode(codeBlock.build())
                .endControlFlow();
        return builder.build();
    }

    private CodeBlock originCall(LifecycleScanner scanner, ExecutableElement method) {
        for (final Signature signature : Signature.ON_CLICK) {
            final CodeBlock originCall = signature.call(scanner.getEnv(), method);
            if (originCall != null) {
                return originCall;
            }
        }
        scanner.getEnv().printMessage(Diagnostic.Kind.ERROR, method, "Unexpected method signature");
        return CodeBlock.builder().build();
    }

}
