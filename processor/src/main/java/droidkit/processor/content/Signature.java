package droidkit.processor.content;

import com.squareup.javapoet.CodeBlock;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import droidkit.processor.ProcessingEnv;
import rx.functions.Func2;

/**
 * @author Daniel Serdyukov
 */
interface Signature extends Func2<ProcessingEnv, ExecutableElement, CodeBlock> {

    List<Signature> ON_CREATE = Arrays.asList(
            new OnCreateLoader0(),
            new OnCreateLoader1(),
            new OnCreateLoader2()
    );

    List<Signature> ON_LOAD = Arrays.asList(
            new OnLoadFinished0(),
            new OnLoadFinished1(),
            new OnLoadFinished2()
    );

    List<Signature> ON_RESET = Arrays.asList(
            new OnLoaderReset0(),
            new OnLoaderReset1()
    );

}
