package droidkit.annotation.internal;

/**
 * @author Daniel Serdyukov
 */
interface AnnotationProcessor {

    void process();

    boolean finishProcessing();

}
