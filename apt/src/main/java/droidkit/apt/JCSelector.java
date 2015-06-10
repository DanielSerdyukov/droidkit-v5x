package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
final class JCSelector {

    private final java.util.List<String> mNames;

    private JCSelector(java.util.List<String> names) {
        mNames = names;
    }

    public static JCSelector get(String... selectors) {
        return new JCSelector(Arrays.asList(selectors));
    }

    public static JCSelector get(Iterable<String> selectors) {
        final ArrayList<String> list = new ArrayList<>();
        for (final String selector : selectors) {
            list.add(selector);
        }
        return new JCSelector(list);
    }

    public JCTree.JCExpression getIdent() {
        Utils.checkArgument(!mNames.isEmpty(), "Selectors is empty");
        final TreeMaker maker = JavacEnv.get().maker();
        final Names names = JavacEnv.get().names();
        JCTree.JCExpression selector = maker.Ident(names.fromString(mNames.get(0)));
        for (int i = 1; i < mNames.size(); ++i) {
            selector = maker.Select(selector, names.fromString(mNames.get(i)));
        }
        return selector;
    }

    public JCTree.JCExpression assign(JCTree.JCExpression expression) {
        return JavacEnv.get().maker().Assign(getIdent(), expression);
    }

    public JCTree.JCExpressionStatement execAssign(JCTree.JCExpression expression) {
        return JavacEnv.get().maker().Exec(assign(expression));
    }

    public JCTree.JCMethodInvocation apply(JCTree.JCExpression... args) {
        return JavacEnv.get().maker().Apply(List.<JCTree.JCExpression>nil(), getIdent(), List.from(args));
    }

    public JCTree.JCExpressionStatement execApply(JCTree.JCExpression... args) {
        return JavacEnv.get().maker().Exec(apply(args));
    }

}
