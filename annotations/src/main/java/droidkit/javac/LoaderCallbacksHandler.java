package droidkit.javac;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.OnCreateLoader;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
class LoaderCallbacksHandler implements AnnotationHandler {

    private final Set<TypeElement> mDistinctTypes = new LinkedHashSet<>();

    private final ProcessingEnvironment mProcessingEnv;

    LoaderCallbacksHandler(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    @Override
    public void handle(RoundEnvironment roundEnv, TypeElement annotation) {
        if (OnCreateLoader.class.getName().equals(annotation.getQualifiedName().toString())) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .map(new GetEnclosingElement())
                    .filter(new DistinctTypes(mDistinctTypes))
                    .subscribe(new Action1<TypeElement>() {
                        @Override
                        public void call(TypeElement element) {
                            final LoaderCallbacksVisitor visitor = new LoaderCallbacksVisitor(mProcessingEnv, element);
                            element.accept(visitor, null);
                            visitor.brewJavaClass();
                        }
                    });
        }
    }

    //region Filters
    private static final class GetEnclosingElement implements Func1<Element, TypeElement> {

        private GetEnclosingElement() {
        }

        @Override
        public TypeElement call(Element element) {
            return (TypeElement) element.getEnclosingElement();
        }

    }

    private static final class DistinctTypes implements Func1<TypeElement, Boolean> {

        private final Set<TypeElement> mDistinctTypes;

        private DistinctTypes(Set<TypeElement> distinctTypes) {
            mDistinctTypes = distinctTypes;
        }

        @Override
        public Boolean call(TypeElement element) {
            if (!mDistinctTypes.contains(element)) {
                mDistinctTypes.add(element);
                return true;
            }
            return false;
        }

    }
    //endregion

}
