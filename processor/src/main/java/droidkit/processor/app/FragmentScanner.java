package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

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

    @Override
    protected List<MethodSpec> methods(TypeElement originType, ClassName viewInjector) {
        return Arrays.asList(
                onViewCreated(originType, viewInjector),
                onActivityCreated(),
                onOptionsItemSelected(Modifier.PUBLIC),
                onResume(Modifier.PUBLIC),
                onPause(Modifier.PUBLIC),
                onDestroy(Modifier.PUBLIC)
        );
    }

    private MethodSpec onViewCreated(TypeElement originType, ClassName viewInjector) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onViewCreated(view, savedInstanceState)")
                .beginControlFlow("if (view != null)")
                .addStatement("$T.inject(view, ($T) this)", viewInjector, ClassName.get(originType));
        for (final MethodSpec method : onClick()) {
            builder.addStatement("$N(view)", method);
        }
        return builder.endControlFlow().build();
    }

    private MethodSpec onActivityCreated() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onActivityCreated(savedInstanceState)");
        for (final MethodSpec method : onActionClick()) {
            builder.addStatement("$N()", method);
        }
        return builder.build();
    }

}
