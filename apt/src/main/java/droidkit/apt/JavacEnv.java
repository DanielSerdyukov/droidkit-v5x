package droidkit.apt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
final class JavacEnv {

    private static JavacEnv sInstance;

    private final JavacProcessingEnvironment mEnv;

    private final TreeMaker mTreeMaker;

    private final Names mNames;

    private final Trees mTrees;

    private JavacEnv(ProcessingEnvironment env) {
        mEnv = (JavacProcessingEnvironment) env;
        mTreeMaker = TreeMaker.instance(mEnv.getContext());
        mNames = Names.instance(mEnv.getContext());
        mTrees = Trees.instance(mEnv);
    }

    public static JavacEnv init(ProcessingEnvironment env) {
        JavacEnv instance = sInstance;
        if (instance == null) {
            synchronized (JavacEnv.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new JavacEnv(env);
                }
            }
        }
        return instance;
    }

    public static JavacEnv get() {
        return Objects.requireNonNull(sInstance, "Add JavacEnv.init(env) in AbstractProcessor.init(env)");
    }

    public TreeMaker maker() {
        return mTreeMaker;
    }

    public Names names() {
        return mNames;
    }

    @SuppressWarnings("unchecked")
    public <T extends JCTree> T getTree(Element element) {
        return (T) mTrees.getTree(element);
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> T getElement(TypeMirror mirror) {
        return (T) mEnv.getTypeUtils().asElement(mirror);
    }

    @SuppressWarnings("unchecked")
    public <T extends Element> T getElement(String element) {
        return (T) mEnv.getElementUtils().getTypeElement(element);
    }

    public boolean isSubtype(Element element1, Element element2) {
        return mEnv.getTypeUtils().isSubtype(element1.asType(), element2.asType());
    }

    public JavaFileObject createSourceFile(JavaFile javaFile, TypeSpec typeSpec, Element... ordinatingElements)
            throws IOException {
        return mEnv.getFiler().createSourceFile(javaFile.packageName + "." + typeSpec.name, ordinatingElements);
    }

    public void logI(Element element, String format, Object... args) {
        if (element == null) {
            mEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(Locale.US, format, args));
        } else {
            mEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(Locale.US, format, args), element);
        }
    }

    public void logE(Element element, String format, Object... args) {
        if (element == null) {
            mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(Locale.US, format, args));
        } else {
            mEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(Locale.US, format, args), element);
        }
    }

}
