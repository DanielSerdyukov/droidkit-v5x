package droidkit.annotation.internal;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import droidkit.annotation.SQLiteObject;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
class AnnotationProcessorFactory {

    private final Map<Element, AnnotationProcessor> mProcessors = new HashMap<>();

    private final JavacProcessingEnvironment mEnv;

    public AnnotationProcessorFactory(ProcessingEnvironment processingEnv) {
        mEnv = (JavacProcessingEnvironment) processingEnv;
    }

    private void checkInNestedClass(TypeElement annotation, Element element) {
        if (ElementKind.PACKAGE != element.getEnclosingElement().getKind()) {
            mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, annotation +
                    " not supported for nested classes", element);
        }
    }

    AnnotationProcessor getProcessor(TypeElement annotation, Element element) {
        AnnotationProcessor processor = mProcessors.get(element);
        if (processor == null) {
            processor = newProcessor(annotation, element);
            mProcessors.put(element, processor);
        }
        return processor;
    }

    boolean finishProcessing() {
        boolean finished = false;
        for (final AnnotationProcessor processor : mProcessors.values()) {
            finished |= processor.finishProcessing();
        }
        return finished;
    }

    private AnnotationProcessor newProcessor(TypeElement annotation, Element element) {
        if (SQLiteObject.class.getName().equals(annotation.getQualifiedName().toString())) {
            checkInNestedClass(annotation, element);
            return new SQLiteObjectProcessor((TypeElement) element);
        }
        throw new IllegalArgumentException("Unsupported annotation type " + annotation);
    }

}
