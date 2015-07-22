package droidkit.javac;

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

    SQLiteObjectHandler(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    @Override
    public void handle(RoundEnvironment roundEnv, TypeElement annotation) {
        if (SQLiteObject.class.getName().equals(annotation.getQualifiedName().toString())) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .filter(new NotInNestedClass())
                    .map(new Func1<Element, TypeElement>() {
                        @Override
                        public TypeElement call(Element element) {
                            return (TypeElement) element;
                        }
                    })
                    .subscribe(new Action1<TypeElement>() {
                        @Override
                        public void call(TypeElement element) {
                            final SQLiteObjectVisitor visitor = new SQLiteObjectVisitor(mProcessingEnv, element);
                            element.accept(visitor, null);
                            visitor.brewJavaClass();
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
