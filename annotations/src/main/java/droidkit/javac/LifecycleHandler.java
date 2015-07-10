package droidkit.javac;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;
import droidkit.annotation.OnCreateLoader;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
class LifecycleHandler implements AnnotationHandler {

    private static final List<String> ANNOTATIONS = ImmutableList.of(
            InjectView.class.getName(),
            OnClick.class.getName(),
            OnActionClick.class.getName(),
            OnCreateLoader.class.getName()
    );

    private final Set<TypeElement> mDistinctTypes = new LinkedHashSet<>();

    private final ProcessingEnvironment mProcessingEnv;

    LifecycleHandler(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    @Override
    public void handle(RoundEnvironment roundEnv, TypeElement annotation) {
        if (ANNOTATIONS.contains(annotation.getQualifiedName().toString())) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .filter(new NotInNestedClass())
                    .map(new GetEnclosingElement())
                    .filter(new ActivityOrFragment(mProcessingEnv))
                    .filter(new DistinctTypes(mDistinctTypes))
                    .subscribe(new Action1<TypeElement>() {
                        @Override
                        public void call(TypeElement element) {
                            final LifecycleVisitor visitor;
                            if (Utils.isSubtype(mProcessingEnv, element, "android.app.Activity")) {
                                visitor = new ActivityVisitor(mProcessingEnv, element);
                            } else {
                                visitor = new FragmentVisitor(mProcessingEnv, element);
                            }
                            element.accept(visitor, null);
                            visitor.brewJavaClass();
                        }
                    });
        }
    }

    //region Filters
    private static final class NotInNestedClass implements Func1<Element, Boolean> {

        private NotInNestedClass() {
        }

        @Override
        public Boolean call(Element element) {
            return ElementKind.PACKAGE == element
                    .getEnclosingElement()
                    .getEnclosingElement()
                    .getKind();
        }

    }

    private static final class GetEnclosingElement implements Func1<Element, TypeElement> {

        private GetEnclosingElement() {
        }

        @Override
        public TypeElement call(Element element) {
            return (TypeElement) element.getEnclosingElement();
        }

    }

    private static final class ActivityOrFragment implements Func1<TypeElement, Boolean> {

        private final ProcessingEnvironment mProcessingEnv;

        private ActivityOrFragment(ProcessingEnvironment processingEnv) {
            mProcessingEnv = processingEnv;
        }

        @Override
        public Boolean call(TypeElement element) {
            return Utils.isSubtype(mProcessingEnv, element, "android.app.Activity")
                    || Utils.isSubtype(mProcessingEnv, element, "android.app.Fragment")
                    || Utils.isSubtype(mProcessingEnv, element, "android.support.v4.app.Fragment");

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
