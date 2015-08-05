package droidkit.processor.app;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
interface MethodVisitor {

    List<MethodVisitor> SUPPORTED = Arrays.asList(
            new OnClickVisitor(),
            new OnActionClickVisitor()
    );

    Annotation getAnnotation(ProcessingEnv processingEnv, ExecutableElement method);

    void visit(LifecycleScanner scanner, ExecutableElement method, Annotation annotation);

}
