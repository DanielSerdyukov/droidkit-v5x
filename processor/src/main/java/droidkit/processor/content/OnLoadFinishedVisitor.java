package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.ExecutableElement;

import droidkit.annotation.OnLoadFinished;

/**
 * @author Daniel Serdyukov
 */
class OnLoadFinishedVisitor implements CallbackVisitor {

    @Override
    public int[] getLoaderIds(ExecutableElement method) {
        final OnLoadFinished annotation = method.getAnnotation(OnLoadFinished.class);
        if (annotation != null) {
            return annotation.value();
        }
        return new int[0];
    }

    @Override
    public void visit(LoaderCallbacksScanner scanner, ExecutableElement method, int loaderId) {
        for (final Signature signature : Signature.ON_LOAD) {
            final CodeBlock codeBlock = signature.call(scanner.getEnv(), method);
            if (codeBlock != null) {
                final CodeBlock.Builder caseBlock = CodeBlock.builder();
                caseBlock.add("case $L:\n", loaderId).indent();
                caseBlock.add(codeBlock);
                caseBlock.addStatement("break").unindent();
                scanner.addOnLoadFinishedCase(caseBlock.build());
                return;
            }
        }
        scanner.unexpectedMethodSignature(method);
    }

}
