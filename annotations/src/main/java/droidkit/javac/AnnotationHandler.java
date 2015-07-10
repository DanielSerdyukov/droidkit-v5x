package droidkit.javac;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
interface AnnotationHandler {

    void handle(RoundEnvironment roundEnv, TypeElement annotation);

}
