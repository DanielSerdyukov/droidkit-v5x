package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
final class JCLiteral {

    public static final JCTree.JCExpression NULL = JavacEnv.get().maker().Literal(TypeTag.BOT, null);

    private JCLiteral() {
    }

    public static JCTree.JCExpression stringValue(String format, Object... args) {
        return JavacEnv.get().maker().Literal(TypeTag.CLASS, String.format(Locale.US, format, args));
    }

    public static JCTree.JCExpression intValue(int value) {
        return JavacEnv.get().maker().Literal(TypeTag.INT, value);
    }

    public static JCTree.JCExpression clazz(JCTypeName type) {
        return JavacEnv.get().maker().Select(type.ident(), JavacEnv.get().names()._class);
    }

}
