package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
class ActivityProxyMaker extends LifecycleProxyMaker {

    private final CodeBlock.Builder mFindViews = CodeBlock.builder();

    ActivityProxyMaker(ProcessingEnvironment processingEnv, TypeElement element) {
        super(processingEnv, element);
        mFindViews.addStatement("final $1T origin = ($1T) this", ClassName.get(element));
    }

    @Override
    void injectView(String fieldName, int viewId) {
        mFindViews.addStatement("origin.$L = $T.findById(this, $L)", fieldName, DROIDKIT_VIEWS, viewId);
    }

    @Override
    protected List<MethodSpec> methods() {
        return ImmutableList.of(
                setContentView1(),
                setContentView2(),
                setContentView3()
        );
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

}
