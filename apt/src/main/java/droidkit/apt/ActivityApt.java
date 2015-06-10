package droidkit.apt;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class ActivityApt extends LifecycleApt {

    private final CodeBlock.Builder mInjectViews = CodeBlock.builder();

    private final List<MethodSpec> mOnClick = new ArrayList<>();

    public ActivityApt(TypeElement element) {
        super(element);
    }

    @Override
    protected void injectView(TypeElement clazz, Element field, int viewId) {
        mInjectViews.addStatement("(($T) this).$L = $T.findById(this, $L)",
                clazz, field.getSimpleName(), DK_VIEWS, viewId);
    }

    @Override
    protected void injectOnClick(TypeElement clazz, ExecutableElement method, int viewId) {
        mOnClick.add(MethodSpec.methodBuilder("setupOnClickListener" + viewId)
                .addModifiers(Modifier.PRIVATE)
                .addStatement("final View view = $T.findById(this, $L)", DK_VIEWS, viewId)
                .beginControlFlow("if (view != null)")
                .addCode(putNewOnClickListener(method))
                .endControlFlow()
                .build());
    }

    @Override
    protected Collection<MethodSpec> methods() {
        final List<MethodSpec> methods = new ArrayList<>();
        Collections.addAll(methods,
                setContentView1(),
                setContentView2(),
                onResume(Modifier.PROTECTED),
                onPause(Modifier.PROTECTED),
                onDestroy(Modifier.PROTECTED));
        methods.addAll(mOnClick);
        return methods;
    }

    private MethodSpec setContentView1() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)")
                .addCode(mInjectViews.build())
                .addCode(setupOnClickListeners())
                .build();
    }

    private MethodSpec setContentView2() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addStatement("super.setContentView(view)")
                .addCode(mInjectViews.build())
                .addCode(setupOnClickListeners())
                .build();
    }

    private CodeBlock setupOnClickListeners() {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (final MethodSpec method : mOnClick) {
            codeBlock.addStatement("$N()", method);
        }
        return codeBlock.build();
    }

}
