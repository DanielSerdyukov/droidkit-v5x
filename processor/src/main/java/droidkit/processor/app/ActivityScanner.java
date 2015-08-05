package droidkit.processor.app;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import droidkit.processor.ProcessingEnv;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class ActivityScanner extends LifecycleScanner {

    public ActivityScanner(ProcessingEnv env) {
        super(env);
    }

    @Override
    protected Func1<Integer, CodeBlock> viewFinder() {
        return new Func1<Integer, CodeBlock>() {
            @Override
            public CodeBlock call(Integer viewId) {
                return CodeBlock.builder()
                        .addStatement("final View view = $T.findById(this, $L)",
                                ClassName.get("droidkit.view", "Views"), viewId)
                        .build();
            }
        };
    }

    @Override
    protected List<MethodSpec> methods(TypeElement originType, ClassName viewInjector) {
        return Arrays.asList(
                setContentView1(originType, viewInjector),
                setContentView2(originType, viewInjector),
                setContentView3(originType, viewInjector),
                onPostCreate(),
                onOptionsItemSelected(Modifier.PUBLIC),
                onResume(Modifier.PROTECTED),
                onPause(Modifier.PROTECTED),
                onDestroy(Modifier.PROTECTED)
        );
    }

    private MethodSpec setContentView1(TypeElement element, ClassName viewInjector) {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addStatement("super.setContentView(view)")
                .addStatement("$T.inject(view, ($T) this)", viewInjector, ClassName.get(element))
                .build();
    }

    private MethodSpec setContentView2(TypeElement element, ClassName viewInjector) {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addParameter(ClassName.get("android.view", "ViewGroup", "LayoutParams"), "params")
                .addStatement("super.setContentView(view, params)")
                .addStatement("$T.inject(view, ($T) this)", viewInjector, ClassName.get(element))
                .build();
    }

    private MethodSpec setContentView3(TypeElement element, ClassName viewInjector) {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)")
                .addStatement("$T.inject(this, ($T) this)", viewInjector, ClassName.get(element))
                .build();
    }

    private MethodSpec onPostCreate() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onPostCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onPostCreate(savedInstanceState)");
        for (final MethodSpec method : onClick()) {
            builder.addStatement("$N(null)", method);
        }
        for (final MethodSpec method : onActionClick()) {
            builder.addStatement("$N()", method);
        }
        return builder.build();
    }


}
