package droidkit.javac;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner7;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
class ElementScanner extends ElementScanner7<Void, Void> {

    protected static final String AUTO_GENERATED_FILE = "AUTO-GENERATED FILE. DO NOT MODIFY.";

    private final Set<TypeElement> mSingleHit = new HashSet<>();

    private final Types mTypes;

    private final Elements mElements;

    private final Messager mLogger;

    private final Filer mFiler;

    public ElementScanner(ProcessingEnvironment processingEnv) {
        super();
        mTypes = processingEnv.getTypeUtils();
        mElements = processingEnv.getElementUtils();
        mLogger = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
    }

    Func1<TypeElement, Boolean> singleHit() {
        return new Func1<TypeElement, Boolean>() {
            @Override
            public Boolean call(TypeElement element) {
                return mSingleHit.add(element);
            }
        };
    }

    void brewJava() {

    }

    void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        mLogger.printMessage(kind, String.format(Locale.US, format, args));
    }

    void printMessage(Diagnostic.Kind kind, Element e, String format, Object... args) {
        mLogger.printMessage(kind, String.format(Locale.US, format, args), e);
    }

    boolean isSubtype(TypeMirror t1, String t2) {
        final TypeElement type = mElements.getTypeElement(t2);
        return type != null && mTypes.isSubtype(t1, type.asType());
    }

    boolean isSubtype(TypeMirror t1, Class<?> t2) {
        return isSubtype(t1, t2.getName());
    }

    boolean isTypeOfKind(ElementKind kind, TypeMirror t1) {
        return TypeKind.DECLARED == t1.getKind()
                && kind == mTypes.asElement(t1).getKind();
    }

    JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
        return mFiler.createSourceFile(name, originatingElements);
    }

}
