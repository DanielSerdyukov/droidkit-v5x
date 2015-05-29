package droidkit.annotation.internal;

import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import java.util.ArrayList;

/**
 * @author Daniel Serdyukov
 */
class JCUtils {

    static Trees TREES;

    static TreeMaker MAKER;

    static Names NAMES;

    static void init(ProcessingEnvironment env) {
        final JavacProcessingEnvironment jenv = (JavacProcessingEnvironment) env;
        TREES = Trees.instance(jenv);
        MAKER = TreeMaker.instance(jenv.getContext());
        NAMES = Names.instance(jenv.getContext());
    }

    static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String nonEmpty(String string, Object defaultValue) {
        if (isEmpty(string)) {
            return String.valueOf(defaultValue);
        }
        return string;
    }

    static JCTree.JCExpression ident(Name ident) {
        return MAKER.Ident(NAMES.fromString(ident.toString()));
    }

    static JCTree.JCExpression ident(Class<?> clazz) {
        return select(clazz.getPackage().getName(), clazz.getSimpleName());
    }

    static JCTree.JCExpression ident(String ident) {
        return MAKER.Ident(NAMES.fromString(ident));
    }

    static JCTree.JCExpression select(String... selectors) {
        JCTree.JCExpression expression = null;
        for (final String selector : selectors) {
            if (expression == null) {
                expression = MAKER.Ident(NAMES.fromString(selector));
            } else {
                expression = MAKER.Select(expression, NAMES.fromString(selector));
            }
        }
        return expression;
    }

    static JCTree.JCExpression notNull(JCTree.JCExpression lhs) {
        return MAKER.Binary(JCTree.Tag.NE, lhs, JCUtils.MAKER.Literal(TypeTag.BOT, "null"));
    }

    static JCTree.JCExpression enumValueOf(Element enumElement, JCTree.JCExpression value) {
        java.util.List<Element> elements = new ArrayList<>();
        Element enclosingElement = enumElement.getEnclosingElement();
        while (ElementKind.PACKAGE != enclosingElement.getKind()) {
            elements.add(enclosingElement);
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        JCTree.JCExpression expression = MAKER.Ident(NAMES.fromString(enclosingElement.toString()));
        for (final Element element : elements) {
            expression = MAKER.Select(expression, NAMES.fromString(element.getSimpleName().toString()));
        }
        expression = MAKER.Select(expression, NAMES.fromString(enumElement.getSimpleName().toString()));
        return JCUtils.MAKER.Exec(JCUtils.MAKER.Apply(
                List.<JCTree.JCExpression>nil(),
                MAKER.Select(expression, NAMES.fromString("valueOf")),
                List.of(value))).getExpression();
    }

    static JCTree getTree(Element element) {
        return (JCTree) TREES.getTree(element);
    }

}
