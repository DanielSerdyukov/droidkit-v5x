package droidkit.processor.content;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Daniel Serdyukov
 */
interface CallbackVisitor {

    List<CallbackVisitor> SUPPORTED = Arrays.asList(
            new OnCreateLoaderVisitor(),
            new OnLoadFinishedVisitor(),
            new OnLoaderResetVisitor()
    );

    int[] getLoaderIds(ExecutableElement method);

    void visit(LoaderCallbacksScanner scanner, ExecutableElement method, int loaderId);

}
