package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;

/**
 * @author Daniel Serdyukov
 */
class JCBinary {

    private JCBinary() {
    }

    public static JCTree.JCExpression equalTo(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return JavacEnv.get().maker().Binary(JCTree.Tag.EQ, lhs, rhs);
    }

    public static JCTree.JCExpression notEqualTo(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return JavacEnv.get().maker().Binary(JCTree.Tag.NE, lhs, rhs);
    }

    public static JCTree.JCExpression lessThan(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return JavacEnv.get().maker().Binary(JCTree.Tag.LT, lhs, rhs);
    }

    public static JCTree.JCExpression greaterThan(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return JavacEnv.get().maker().Binary(JCTree.Tag.GT, lhs, rhs);
    }

}
