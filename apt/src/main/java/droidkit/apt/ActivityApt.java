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
                onCreate(),
                setContentView1(),
                setContentView2(),
                setContentView3(),
                onPostCreate(),
                onOptionsItemSelected(),
                onResume(Modifier.PROTECTED),
                onPause(Modifier.PROTECTED),
                onSaveInstanceState(Modifier.PROTECTED),
                onDestroy(Modifier.PROTECTED));
        methods.addAll(mOnClick);
        methods.addAll(setupOnActionClickMethods());
        return methods;
    }

    private MethodSpec onCreate() {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onCreate(savedInstanceState)")
                .addCode(restoreInstanceState())
                .build();
    }

    private MethodSpec setContentView1() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)")
                .addCode(mInjectViews.build())
                .addCode(callSetupOnClickMethods())
                .build();
    }

    private MethodSpec setContentView2() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addStatement("super.setContentView(view)")
                .addCode(mInjectViews.build())
                .addCode(callSetupOnClickMethods())
                .build();
    }

    private MethodSpec setContentView3() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addParameter(ClassName.get("android.view", "ViewGroup", "LayoutParams"), "params")
                .addStatement("super.setContentView(view, params)")
                .addCode(mInjectViews.build())
                .addCode(callSetupOnClickMethods())
                .build();
    }

    private MethodSpec onPostCreate() {
        return MethodSpec.methodBuilder("onPostCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onPostCreate(savedInstanceState)")
                .addCode(callSetupOnActionClickMethods())
                .build();
    }

    private CodeBlock callSetupOnClickMethods() {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (final MethodSpec method : mOnClick) {
            codeBlock.addStatement("$N()", method);
        }
        return codeBlock.build();
    }

}
