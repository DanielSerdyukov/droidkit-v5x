package droidkit.apt;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

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
class FragmentApt extends LifecycleApt {

    private final CodeBlock.Builder mInjectViews = CodeBlock.builder();

    private final List<MethodSpec> mOnClick = new ArrayList<>();

    public FragmentApt(TypeElement element) {
        super(element);
    }

    @Override
    protected void injectView(TypeElement clazz, Element field, int viewId) {
        mInjectViews.addStatement("(($T) this).$L = $T.findById(view, $L)",
                clazz, field.getSimpleName(), ClassName.get("droidkit.view", "Views"), viewId);
    }

    @Override
    protected void injectOnClick(TypeElement clazz, ExecutableElement method, int viewId) {
        mOnClick.add(MethodSpec.methodBuilder("setupOnClickListener" + viewId)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(VIEW, "rootView")
                .addStatement("final View view = $T.findById(rootView, $L)", DK_VIEWS, viewId)
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
                onViewCreated(),
                onActivityCreated(),
                onOptionsItemSelected(),
                onResume(Modifier.PUBLIC),
                onPause(Modifier.PUBLIC),
                onSaveInstanceState(Modifier.PUBLIC),
                onDestroy(Modifier.PUBLIC));
        methods.addAll(mOnClick);
        methods.addAll(setupOnActionClickMethods());
        return methods;
    }

    private MethodSpec onCreate() {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onCreate(savedInstanceState)")
                .addCode(restoreInstanceState())
                .build();
    }

    private MethodSpec onViewCreated() {
        return MethodSpec.methodBuilder("onViewCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.view", "View"), "view")
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onViewCreated(view, savedInstanceState)")
                .addCode(mInjectViews.build())
                .addCode(setupOnClickListeners())
                .build();
    }

    private MethodSpec onActivityCreated() {
        return MethodSpec.methodBuilder("onActivityCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addStatement("super.onActivityCreated(savedInstanceState)")
                .addCode(callSetupOnActionClickMethods())
                .build();
    }

    private CodeBlock setupOnClickListeners() {
        final CodeBlock.Builder codeBlock = CodeBlock.builder();
        for (final MethodSpec method : mOnClick) {
            codeBlock.addStatement("$N(view)", method);
        }
        return codeBlock.build();
    }

}
