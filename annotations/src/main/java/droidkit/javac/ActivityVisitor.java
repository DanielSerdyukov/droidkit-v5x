package droidkit.javac;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * @author Daniel Serdyukov
 */
class ActivityVisitor extends LifecycleVisitor {

    private final CodeBlock.Builder mFindViews = CodeBlock.builder();

    private final List<MethodSpec> mOnClick = new ArrayList<>();

    private final List<MethodSpec> mOnActionClick = new ArrayList<>();

    ActivityVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        super(processingEnv, element);
        mFindViews.addStatement("final $1T origin = ($1T) this", ClassName.get(element));
    }

    @Override
    protected void injectView(VariableElement field, int viewId) {
        mFindViews.addStatement("origin.$L = $T.findById(this, $L)", field.getSimpleName(), DROIDKIT_VIEWS, viewId);
    }

    @Override
    protected void injectOnClick(ExecutableElement method, int[] viewIds) {
        for (final int viewId : viewIds) {
            mOnClick.add(MethodSpec.methodBuilder("setupOnClickBy" + viewId)
                    .addStatement("final $T view = $T.findById(this, $L)", ANDROID_VIEW, DROIDKIT_VIEWS, viewId)
                    .addCode(putOnClickListener(method))
                    .build());
        }
    }

    @Override
    protected List<MethodSpec> injectOnActionClick(ExecutableElement method, int[] viewIds) {
        final List<MethodSpec> methods = super.injectOnActionClick(method, viewIds);
        mOnActionClick.addAll(methods);
        return methods;
    }

    @Override
    protected List<MethodSpec> methods() {
        final List<MethodSpec> methods = new ArrayList<>();
        Collections.addAll(methods,
                setContentView1(),
                setContentView2(),
                setContentView3(),
                onPostCreate(),
                onOptionsItemSelected(),
                onResume(Modifier.PROTECTED),
                onPause(Modifier.PROTECTED),
                onDestroy(Modifier.PROTECTED));
        methods.addAll(mOnClick);
        methods.addAll(mOnActionClick);
        return methods;
    }

    private MethodSpec setContentView1() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "layoutResId")
                .addStatement("super.setContentView(layoutResId)")
                .addCode(mFindViews.build())
                .build();
    }

    private MethodSpec setContentView2() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ANDROID_VIEW, "view")
                .addStatement("super.setContentView(view)")
                .addCode(mFindViews.build())
                .build();
    }

    private MethodSpec setContentView3() {
        return MethodSpec.methodBuilder("setContentView")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ANDROID_VIEW, "view")
                .addParameter(ClassName.get("android.view", "ViewGroup", "LayoutParams"), "params")
                .addStatement("super.setContentView(view, params)")
                .addCode(mFindViews.build())
                .build();
    }

    private MethodSpec onPostCreate() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onPostCreate")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onPostCreate(savedInstanceState)");
        for (final MethodSpec setupOnClick : mOnClick) {
            builder.addStatement("$N()", setupOnClick);
        }
        for (final MethodSpec setupOnActionClick : mOnActionClick) {
            builder.addStatement("$N()", setupOnActionClick);
        }
        return builder.build();
    }

}
