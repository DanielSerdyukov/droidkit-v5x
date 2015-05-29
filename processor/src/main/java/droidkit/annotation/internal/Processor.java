package droidkit.annotation.internal;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
@SupportedAnnotationTypes({
        "droidkit.annotation.SQLiteObject",
})
public class Processor extends AbstractProcessor {

    private AnnotationProcessorFactory mFactory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JCUtils.init(processingEnv);
        mFactory = new AnnotationProcessorFactory(processingEnv);
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
                mFactory.getProcessor(annotation, element).process();
            }
        }
        return mFactory.finishProcessing();
    }

}
