package droidkit.apt;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import java.io.IOException;
import java.util.Locale;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * @author Daniel Serdyukov
 */
class JavacEnv {

    static volatile JavacProcessingEnvironment sEnv;

    static volatile TreeMaker sTreeMaker;

    static volatile Names sNames;

    static volatile Trees sTrees;

    static void init(ProcessingEnvironment env) {
        sEnv = (JavacProcessingEnvironment) env;
        sTreeMaker = TreeMaker.instance(sEnv.getContext());
        sNames = Names.instance(sEnv.getContext());
        sTrees = Trees.instance(sEnv);
    }

    static void logE(Element element, String format, Object... args) {
        if (element == null) {
            sEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(Locale.US, format, args));
        } else {
            sEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(Locale.US, format, args), element);
        }
    }

    static boolean isSubtype(Element element, String fqcn) {
        return sEnv.getTypeUtils().isSubtype(element.asType(), sEnv.getElementUtils().getTypeElement(fqcn).asType());
    }

    static boolean isSubtype(TypeMirror element, String fqcn) {
        return sEnv.getTypeUtils().isSubtype(element, sEnv.getElementUtils().getTypeElement(fqcn).asType());
    }

    static boolean isSubtype(TypeMirror element, Class<?> type) {
        return sEnv.getTypeUtils().isSubtype(element, sEnv.getElementUtils().getTypeElement(type.getName()).asType());
    }

    @SuppressWarnings("unchecked")
    static <T extends JCTree> T getTree(Element element) {
        return (T) sTrees.getTree(element);
    }

    static JavaFileObject createSourceFile(JavaFile javaFile, TypeSpec typeSpec, Element... ordinatingElements)
            throws IOException {
        return sEnv.getFiler().createSourceFile(javaFile.packageName + "." + typeSpec.name, ordinatingElements);
    }

    static JCTree.JCExpression thisIdent(String selector) {
        return sTreeMaker.Select(sTreeMaker.Ident(sNames._this), sNames.fromString(selector));
    }

    static JCTree.JCExpression select(String... selectors) {
        final UnmodifiableIterator<String> iterator = Iterators.forArray(selectors);
        JCTree.JCExpression selector = sTreeMaker.Ident(sNames.fromString(iterator.next()));
        while (iterator.hasNext()) {
            selector = sTreeMaker.Select(selector, sNames.fromString(iterator.next()));
        }
        return selector;
    }

    static JCTree.JCExpression ident(String selector) {
        return sTreeMaker.Ident(sNames.fromString(selector));
    }

}
