package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class FragmentProxyMaker extends LifecycleProxyMaker {

    private final CodeBlock.Builder mFindViews = CodeBlock.builder();

    FragmentProxyMaker(ProcessingEnvironment processingEnv, TypeElement element) {
        super(processingEnv, element);
        mFindViews.addStatement("final $1T origin = ($1T) this", ClassName.get(element));
    }

    @Override
    void injectView(String fieldName, int viewId) {
        mFindViews.addStatement("origin.$L = $T.findById(view, $L)", fieldName, DROIDKIT_VIEWS, viewId);
    }

    @Override
    protected List<MethodSpec> methods() {
        return ImmutableList.of(
                onViewCreated()
        );
    }

    private MethodSpec onViewCreated() {
        return MethodSpec.methodBuilder("onViewCreated")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ANDROID_VIEW, "view")
                .addParameter(ANDROID_BUNDLE, "savedInstanceState")
                .addStatement("super.onViewCreated(view, savedInstanceState)")
                .addCode(mFindViews.build())
                .build();
    }

}
