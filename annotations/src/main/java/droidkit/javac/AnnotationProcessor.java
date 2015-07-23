package droidkit.javac;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * @author Daniel Serdyukov
 */
@SupportedAnnotationTypes({
        "droidkit.annotation.InjectView",
        "droidkit.annotation.InstanceState",
        "droidkit.annotation.OnClick",
        "droidkit.annotation.OnActionClick",
        "droidkit.annotation.OnCreateLoader",
        "droidkit.annotation.SQLiteObject"
})
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /*private final List<AnnotationHandler> mHandlers = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mHandlers.add(new LifecycleHandler(processingEnv));
        mHandlers.add(new SQLiteObjectHandler(processingEnv));
        mHandlers.add(new LoaderCallbacksHandler(processingEnv));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        Observable.from(annotations).forEach(new Action1<TypeElement>() {
            @Override
            public void call(final TypeElement annotation) {
                Observable.from(mHandlers).forEach(new Action1<AnnotationHandler>() {
                    @Override
                    public void call(AnnotationHandler handler) {
                        handler.handle(roundEnv, annotation);
                    }
                });
            }
        });
        SQLiteObjectMaker.brewSchemaClass(processingEnv);
        return true;
    }*/

}
