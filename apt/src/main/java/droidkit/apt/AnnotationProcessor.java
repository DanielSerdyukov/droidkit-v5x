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
import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.SQLiteObject;

@SupportedAnnotationTypes({
        "droidkit.annotation.InjectView",
        "droidkit.annotation.OnActionClick",
        "droidkit.annotation.OnClick",
        "droidkit.annotation.SQLiteObject",
        "droidkit.annotation.OnCreateLoader"
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
                getOrCreateApt(annotation, element).process(roundEnv);
            }
        }
        try {
            for (final Apt apt : mApt.values()) {
                apt.finishProcessing();
            }
            SQLiteObjectApt.brewClass();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private Apt getOrCreateApt(TypeElement annotation, Element element) {
        final Element enclosingElement = element.getEnclosingElement();
        if (mApt.containsKey(element)) {
            return mApt.get(element);
        } else if (mApt.containsKey(enclosingElement)) {
            return mApt.get(enclosingElement);
        } else {
            final String fqcn = annotation.getQualifiedName().toString();
            if (InjectView.class.getName().equals(fqcn)
                    || OnClick.class.getName().equals(fqcn)
                    || OnActionClick.class.getName().equals(fqcn)) {
                if (Utils.isSubtype(enclosingElement, "android.app.Activity")) {
                    return putIfAbsent(enclosingElement, new ActivityApt((TypeElement) enclosingElement));
                } else if (Utils.isSubtype(enclosingElement, "android.app.Fragment")
                        || Utils.isSubtype(enclosingElement, "android.support.v4.app.Fragment")) {
                    return putIfAbsent(enclosingElement, new FragmentApt((TypeElement) enclosingElement));
                }
            } else if (SQLiteObject.class.getName().equals(fqcn)) {
                return putIfAbsent(element, new SQLiteObjectApt((TypeElement) element));
            } else if (OnCreateLoader.class.getName().equals(fqcn)) {
                return putIfAbsent(enclosingElement, new LoaderCallbacksApt((TypeElement) enclosingElement));
            }
        }
        JavacEnv.get().logE(element, "Unsupported annotation: %s", annotation);
        throw new IllegalArgumentException("Unsupported annotation: " + annotation);
    }

    private Apt putIfAbsent(Element element, Apt apt) {
        mApt.put(element, apt);
        return apt;
    }

}
