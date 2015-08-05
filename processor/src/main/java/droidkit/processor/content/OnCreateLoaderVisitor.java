package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.ExecutableElement;

import droidkit.annotation.OnCreateLoader;

/**
 * @author Daniel Serdyukov
 */
class OnCreateLoaderVisitor implements CallbackVisitor {

    @Override
    public int[] getLoaderIds(ExecutableElement method) {
        final OnCreateLoader annotation = method.getAnnotation(OnCreateLoader.class);
        if (annotation != null) {
            return annotation.value();
        }
        return new int[0];
    }

    @Override
    public void visit(LoaderCallbacksScanner scanner, ExecutableElement method, int loaderId) {
        for (final Signature signature : Signature.ON_CREATE) {
            final CodeBlock codeBlock = signature.call(scanner.getEnv(), method);
            if (codeBlock != null) {
                final CodeBlock.Builder caseBlock = CodeBlock.builder();
                caseBlock.add("case $L:\n", loaderId).indent();
                caseBlock.add(codeBlock);
                caseBlock.unindent();
                scanner.addOnCreateLoaderCase(caseBlock.build());
                return;
            }
        }
        scanner.unexpectedMethodSignature(method);
    }

}
