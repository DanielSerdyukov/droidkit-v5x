package droidkit.javac;

import com.google.common.collect.ImmutableList;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;

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

    private final Trees mTrees;

    LifecycleHandler(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
        mTrees = Trees.instance(processingEnv);
    }

    @Override
    public void handle(RoundEnvironment roundEnv, TypeElement annotation) {
        if (ANNOTATIONS.contains(annotation.getQualifiedName().toString())) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .filter(new NotInNestedClass())
                    .map(new GetEnclosingElement())
                    .filter(new DistinctTypes(mDistinctTypes))
                    .subscribe(new Action1<TypeElement>() {
                        @Override
                        public void call(TypeElement element) {
                            ((JCTree) mTrees.getTree(element)).accept(new LifecycleVisitor(mProcessingEnv, element));
                        }
                    });
        }
    }

    //region Reactive functions
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
