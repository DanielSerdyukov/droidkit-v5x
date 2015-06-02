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
import javax.tools.Diagnostic;
import java.util.ArrayList;

/**
 * @author Daniel Serdyukov
 */
class JCUtils {

    static JavacProcessingEnvironment ENV;

    static Trees TREES;

    static TreeMaker MAKER;

    static Names NAMES;

    static void init(ProcessingEnvironment env) {
        ENV = (JavacProcessingEnvironment) env;
        TREES = Trees.instance(ENV);
        MAKER = TreeMaker.instance(ENV.getContext());
        NAMES = Names.instance(ENV.getContext());
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

    static <T extends JCTree> List<T> nilList() {
        return List.nil();
    }

    static JCTree.JCBlock block(JCTree.JCStatement... statements) {
        return MAKER.Block(0, List.from(statements));
    }

    static JCTree.JCExpressionStatement invoke(JCTree.JCExpression method, JCTree.JCExpression... args) {
        return MAKER.Exec(MAKER.Apply(List.<JCTree.JCExpression>nil(), method, List.from(args)));
    }

    static void error(CharSequence msg, Element e) {
        ENV.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    static String normalize(String prefix, String field) {
        final String name = field.substring(prefix.length());
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
