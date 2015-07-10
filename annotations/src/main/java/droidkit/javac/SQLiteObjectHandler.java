package droidkit.javac;

import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import droidkit.annotation.SQLiteObject;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
class SQLiteObjectHandler implements AnnotationHandler {

    private final ProcessingEnvironment mProcessingEnv;

    private final Trees mTrees;

    SQLiteObjectHandler(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
        mTrees = Trees.instance(processingEnv);
    }

    @Override
    public void handle(RoundEnvironment roundEnv, TypeElement annotation) {
        if (SQLiteObject.class.getName().equals(annotation.getQualifiedName().toString())) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .filter(new NotInNestedClass())
                    .subscribe(new Action1<Element>() {
                        @Override
                        public void call(Element element) {
                            ((JCTree) mTrees.getTree(element)).accept(new SQLiteObjectVisitor(
                                    mProcessingEnv, (TypeElement) element));
                        }
                    });
        }
    }

    private static final class NotInNestedClass implements Func1<Element, Boolean> {

        private NotInNestedClass() {
        }

        @Override
        public Boolean call(Element element) {
            return ElementKind.PACKAGE == element
                    .getEnclosingElement()
                    .getKind();
        }

    }

}
