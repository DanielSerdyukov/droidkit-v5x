package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

/**
 * @author Daniel Serdyukov
 */
final class JCLiteral {

    public static final JCTree.JCExpression NULL = JavacEnv.get().maker().Literal(TypeTag.BOT, null);

    private JCLiteral() {
    }

    public static JCTree.JCExpression stringValue(String value) {
        return JavacEnv.get().maker().Literal(TypeTag.CLASS, value);
    }

    public static JCTree.JCExpression intValue(int value) {
        return JavacEnv.get().maker().Literal(TypeTag.INT, value);
    }

}
