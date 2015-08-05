package droidkit.processor.app;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.VariableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
interface FieldVisitor {

    List<FieldVisitor> SUPPORTED = Collections.<FieldVisitor>singletonList(
            new InjectViewVisitor()
    );

    Annotation getAnnotation(ProcessingEnv processingEnv, VariableElement field);

    void visit(LifecycleScanner scanner, VariableElement field, Annotation annotation);

}
