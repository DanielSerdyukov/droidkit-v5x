package droidkit.annotation.internal;

import droidkit.annotation.InjectView;
import droidkit.annotation.SQLiteObject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
@SupportedAnnotationTypes({
        "droidkit.annotation.SQLiteObject",
        "droidkit.annotation.InjectView"
})
public class AnnotationProcessor extends AbstractProcessor {

    private final SQLiteGen mSQLiteGen = new SQLiteGen();

    private final Map<Element, IProcessor> mProcessors = new HashMap<>();

    private static void checkInNestedClass(TypeElement annotation, Element element) {
        if (ElementKind.PACKAGE != element.getEnclosingElement().getKind()) {
            JCUtils.error(annotation + " not supported for nested classes", element);
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JCUtils.init(processingEnv);
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
                getProcessor(annotation, element).process();
            }
        }
        boolean finished = false;
        for (final IProcessor processor : mProcessors.values()) {
            finished |= processor.finishProcessing();
        }
        try {
            mSQLiteGen.makeJavaFile();
        } catch (IOException e) {
            e.printStackTrace();
            finished = false;
        }
        return finished;
    }

    private IProcessor getProcessor(TypeElement annotation, Element element) {
        IProcessor processor = null;
        final String fqcn = annotation.getQualifiedName().toString();
        if (SQLiteObject.class.getName().equals(fqcn)) {
            checkInNestedClass(annotation, element);
            processor = mProcessors.get(element);
            if (processor == null) {
                processor = new SQLiteObjectProcessor(mSQLiteGen, (TypeElement) element);
                mProcessors.put(element, processor);
            }
        } else if (InjectView.class.getName().equals(fqcn)) {
            final Element enclosingElement = element.getEnclosingElement();
            processor = mProcessors.get(enclosingElement);
            if (processor == null) {
                if (JCUtils.isSubtype(enclosingElement, "android.app.Activity")) {
                    processor = new ActivityProxy((TypeElement) enclosingElement);
                    mProcessors.put(enclosingElement, processor);
                } else if (JCUtils.isSubtype(enclosingElement, "android.app.Fragment")
                        || JCUtils.isSubtype(enclosingElement, "android.support.v4.app.Fragment")) {
                    processor = new FragmentProxy((TypeElement) enclosingElement);
                    mProcessors.put(enclosingElement, processor);
                } else {
                    throw new IllegalArgumentException(annotation + " supported only for Activity and Fragments ");
                }
            }
        }
        if (processor == null) {
            throw new IllegalArgumentException("Unsupported annotation type " + annotation);
        }
        return processor;
    }

}
