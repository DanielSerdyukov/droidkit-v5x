package droidkit.processor.app;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
interface MethodVisitor {

    List<MethodVisitor> SUPPORTED = Collections.<MethodVisitor>singletonList(
            new OnClickVisitor()
    );

    Annotation getAnnotation(ProcessingEnv processingEnv, ExecutableElement field);

    void visit(LifecycleScanner scanner, ExecutableElement method, Annotation annotation);

}
