package droidkit.processor;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
public class ProcessingEnv {

    private final JavacProcessingEnvironment mJavacEnv;

    private final Messager mLogger;

    private final Elements mElements;

    private final Types mTypes;

    private final Trees mTrees;

    private final Filer mFiler;

    public ProcessingEnv(ProcessingEnvironment processingEnv) {
        mJavacEnv = (JavacProcessingEnvironment) processingEnv;
        mLogger = processingEnv.getMessager();
        mElements = processingEnv.getElementUtils();
        mTypes = processingEnv.getTypeUtils();
        mTrees = Trees.instance(processingEnv);
        mFiler = processingEnv.getFiler();
    }

    public JavacProcessingEnvironment getJavacEnv() {
        return mJavacEnv;
    }

    @SuppressWarnings("unchecked")
    public <T extends JCTree> T getTree(Element element) {
        return (T) mTrees.getTree(element);
    }

    public void printMessage(Diagnostic.Kind kind, String format, Object... args) {
        mLogger.printMessage(kind, String.format(Locale.US, format, args));
    }

    public void printMessage(Diagnostic.Kind kind, Element e, String format, Object... args) {
        mLogger.printMessage(kind, String.format(Locale.US, format, args), e);
    }

    public boolean isSubtype(TypeMirror t1, String t2) {
        final TypeElement type = mElements.getTypeElement(t2);
        return type != null && mTypes.isSubtype(t1, type.asType());
    }

    public boolean isSubtype(TypeMirror t1, Class<?> t2) {
        return isSubtype(t1, t2.getName());
    }

    public boolean isTypeOfKind(ElementKind kind, TypeMirror t1) {
        return TypeKind.DECLARED == t1.getKind()
                && kind == mTypes.asElement(t1).getKind();
    }

    public Element asElement(TypeMirror t1) {
        return mTypes.asElement(t1);
    }

    public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements) throws IOException {
        return mFiler.createSourceFile(name, originatingElements);
    }

}
