package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import droidkit.processor.ProcessingEnv;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class FragmentScanner extends LifecycleScanner {

    public FragmentScanner(ProcessingEnv env) {
        super(env);
    }

    @Override
    protected Func1<Integer, CodeBlock> viewFinder() {
        return new Func1<Integer, CodeBlock>() {
            @Override
            public CodeBlock call(Integer viewId) {
                return CodeBlock.builder()
                        .addStatement("final View view = $T.findById(rootView, $L)",
                                ClassName.get("droidkit.view", "Views"), viewId)
                        .build();
            }
        };
    }

}
