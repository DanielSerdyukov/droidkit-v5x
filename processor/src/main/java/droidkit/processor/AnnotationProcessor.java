package droidkit.processor;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import droidkit.processor.sqlite.SQLiteObjectScanner;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@SupportedAnnotationTypes({
        "droidkit.annotation.SQLiteObject",
        "droidkit.annotation.OnCreateLoader",
        "droidkit.annotation.InjectLayout",
        "droidkit.annotation.InjectView",
        "droidkit.annotation.OnClick",
        "droidkit.annotation.OnActionClick"
})
public class AnnotationProcessor extends AbstractProcessor {

    private final Set<Element> mSingleHit = new LinkedHashSet<>();

    private ScannerFactory mFactory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFactory = new ScannerFactory(processingEnv);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        for (final TypeElement annotation : annotations) {
            Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                    .map(new GetEnclosingClass())
                    .filter(new NotNestedClass())
                    .filter(new SingleHit(mSingleHit))
                    .subscribe(new Action1<TypeElement>() {
                        @Override
                        public void call(TypeElement element) {
                            final ElementScanner scanner = mFactory.getScanner(annotation);
                            scanner.visitStart();
                            element.accept(scanner, null);
                            scanner.visitEnd();
                        }
                    });
        }
        SQLiteObjectScanner.brewMetaClass(processingEnv);
        return true;
    }

    //region filters
    private static final class GetEnclosingClass implements Func1<Element, TypeElement> {
        @Override
        public TypeElement call(Element element) {
            while (ElementKind.CLASS != element.getKind()) {
                element = element.getEnclosingElement();
            }
            return (TypeElement) element;
        }
    }

    private static final class NotNestedClass implements Func1<Element, Boolean> {
        @Override
        public Boolean call(Element element) {
            return ElementKind.PACKAGE == element.getEnclosingElement().getKind();
        }
    }

    private static final class SingleHit implements Func1<Element, Boolean> {

        private final Set<Element> mSingleHit;

        private SingleHit(Set<Element> singleHit) {
            mSingleHit = singleHit;
        }

        @Override
        public Boolean call(Element element) {
            return mSingleHit.add(element);
        }

    }
    //endregion

}
