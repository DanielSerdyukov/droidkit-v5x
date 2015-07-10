package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * @author Daniel Serdyukov
 */
class FragmentVisitor extends LifecycleVisitor {

    private final CodeBlock.Builder mFindViews = CodeBlock.builder();

    private final List<MethodSpec> mOnClick = new ArrayList<>();

    private final List<MethodSpec> mOnActionClick = new ArrayList<>();

    FragmentVisitor(ProcessingEnvironment processingEnv, TypeElement element) {
        super(processingEnv, element);
        mFindViews.addStatement("final $1T origin = ($1T) this", ClassName.get(element));
    }

    @Override
    protected void injectView(VariableElement field, int viewId) {
        mFindViews.addStatement("origin.$L = $T.findById(view, $L)", field.getSimpleName(), DROIDKIT_VIEWS, viewId);
    }

    @Override
    protected void injectOnClick(ExecutableElement method, int[] viewIds) {
        for (final int viewId : viewIds) {
            mOnClick.add(MethodSpec.methodBuilder("setupOnClickBy" + viewId)
                    .addParameter(ANDROID_VIEW, "rootView")
                    .addStatement("final $T view = $T.findById(rootView, $L)", ANDROID_VIEW, DROIDKIT_VIEWS, viewId)
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
        return ImmutableList.<MethodSpec>builder().add(
                onViewCreated(),
                onActivityCreated(),
                onOptionsItemSelected(),
                onResume(Modifier.PUBLIC),
                onPause(Modifier.PUBLIC),
                onDestroy(Modifier.PUBLIC)
        ).addAll(mOnClick).addAll(mOnActionClick).build();
    }

    private MethodSpec onViewCreated() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onViewCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ANDROID_VIEW, "view")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onViewCreated(view, savedInstanceState)")
                .addCode(mFindViews.build());
        for (final MethodSpec setupOnClick : mOnClick) {
            builder.addStatement("$N(view)", setupOnClick);
        }
        return builder.build();
    }

    private MethodSpec onActivityCreated() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("onActivityCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onActivityCreated(savedInstanceState)");
        for (final MethodSpec setupOnActionClick : mOnActionClick) {
            builder.addStatement("$N()", setupOnActionClick);
        }
        return builder.build();
    }

}
