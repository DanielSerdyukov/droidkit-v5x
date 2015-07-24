package droidkit.javac;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
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
@SupportedAnnotationTypes({
        "droidkit.annotation.InjectView",
        "droidkit.annotation.InstanceState",
        "droidkit.annotation.OnClick",
        "droidkit.annotation.OnActionClick",
        "droidkit.annotation.OnCreateLoader",
        "droidkit.annotation.SQLiteObject"
})
public class AnnotationProcessor extends AbstractProcessor {

    private final Map<String, ElementScanner> mScanners = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mScanners.put(SQLiteObject.class.getName(), new SQLiteObjectVisitor(processingEnv));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        for (final TypeElement annotation : annotations) {
            final ElementScanner scanner = mScanners.get(annotation.getQualifiedName().toString());
            if (scanner != null) {
                Observable.from(roundEnv.getElementsAnnotatedWith(annotation))
                        .map(new GetEnclosingClass())
                        .filter(new NotNestedClass())
                        .filter(scanner.singleHit())
                        .subscribe(new Action1<TypeElement>() {
                            @Override
                            public void call(TypeElement element) {
                                element.accept(scanner, null);
                                scanner.brewJava();
                            }
                        });
            }
        }
        SQLiteObjectVisitor.brewSchema(processingEnv);
        return true;
    }

    private static final class GetEnclosingClass implements Func1<Element, TypeElement> {
        @Override
        public TypeElement call(Element element) {
            while (ElementKind.CLASS != element.getKind()) {
                element = element.getEnclosingElement();
            }
            System.out.println(element);
            return (TypeElement) element;
        }
    }

    private static final class NotNestedClass implements Func1<Element, Boolean> {
        @Override
        public Boolean call(Element element) {
            return ElementKind.PACKAGE == element.getEnclosingElement().getKind();
        }
    }

}
