package droidkit.apt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;

@SupportedAnnotationTypes({
        "droidkit.annotation.InjectView",
        "droidkit.annotation.OnActionClick",
        "droidkit.annotation.OnClick"
})
public class AnnotationProcessor extends AbstractProcessor {

    private final Map<Element, Apt> mApt = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacEnv.init(processingEnv);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        for (final TypeElement annotation : annotations) {
            final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (final Element element : elements) {
                findApt(annotation, element).process(roundEnv);
            }
        }
        try {
            for (final Apt apt : mApt.values()) {
                apt.finishProcessing();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Apt findApt(TypeElement annotation, Element element) {
        final String fqcn = annotation.getQualifiedName().toString();
        if (InjectView.class.getName().equals(fqcn)
                || OnClick.class.getName().equals(fqcn)
                || OnActionClick.class.getName().equals(fqcn)) {
            return findLifecycleApt(annotation, element.getEnclosingElement());
        } else {
            JavacEnv.get().logE(element, "Unsupported annotation: %s", annotation);
        }
        throw new AssertionError();
    }

    private Apt findLifecycleApt(TypeElement annotation, Element element) {
        Apt apt = null;
        if (Utils.isSubtype(element, "android.app.Activity")) {
            apt = mApt.get(element);
            if (apt == null) {
                apt = new ActivityApt((TypeElement) element);
                mApt.put(element, apt);
            }
        } else if (Utils.isSubtype(element, "android.app.Fragment")
                || Utils.isSubtype(element, "android.support.v4.app.Fragment")) {
            apt = mApt.get(element);
            if (apt == null) {
                apt = new FragmentApt((TypeElement) element);
                mApt.put(element, apt);
            }
        } else {
            JavacEnv.get().logE(element, "%s supported only in Activity and Fragment", annotation);
        }
        return apt;
    }

}
