package droidkit.processor.app;

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

    List<Signature> ON_CLICK = Arrays.asList(
            new OnClick0(),
            new OnClick1()
    );

    List<Signature> ON_ACTION_CLICK = Arrays.asList(
            new OnMenuItemClick0(),
            new OnMenuItemClick1(),
            new OnMenuItemClick2(),
            new OnMenuItemClick3()
    );

}
