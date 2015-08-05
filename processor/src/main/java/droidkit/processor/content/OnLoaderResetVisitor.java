package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.ExecutableElement;

import droidkit.annotation.OnResetLoader;

/**
 * @author Daniel Serdyukov
 */
class OnLoaderResetVisitor implements CallbackVisitor {

    @Override
    public int[] getLoaderIds(ExecutableElement method) {
        final OnResetLoader annotation = method.getAnnotation(OnResetLoader.class);
        if (annotation != null) {
            return annotation.value();
        }
        return new int[0];
    }

    @Override
    public void visit(LoaderCallbacksScanner scanner, ExecutableElement method, int loaderId) {
        for (final Signature signature : Signature.ON_RESET) {
            final CodeBlock codeBlock = signature.call(scanner.getEnv(), method);
            if (codeBlock != null) {
                final CodeBlock.Builder caseBlock = CodeBlock.builder();
                caseBlock.add("case $L:\n", loaderId).indent();
                caseBlock.add(codeBlock);
                caseBlock.addStatement("break").unindent();
                scanner.addOnLoaderResetCase(caseBlock.build());
                return;
            }
        }
        scanner.unexpectedMethodSignature(method);
    }

}
