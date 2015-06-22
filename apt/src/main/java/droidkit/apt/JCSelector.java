package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.lang.model.element.VariableElement;

/**
 * @author Daniel Serdyukov
 */
final class JCSelector {

    private final JCTree.JCExpression mSelector;

    private JCSelector(JCTree.JCExpression selector) {
        mSelector = selector;
    }

    public static JCSelector getField(JCTree.JCExpression object, VariableElement field, String... selectors) {
        final TreeMaker maker = JavacEnv.get().maker();
        final Names names = JavacEnv.get().names();
        JCTree.JCExpression selector = object == null ? maker.Ident(names._this) : object;
        selector = maker.Select(selector, names.fromString(field.getSimpleName().toString()));
        return get(selector, selectors);
    }

    public static JCSelector get(JCTree.JCExpressionStatement selector, Collection<String> selectors) {
        return get(selector.getExpression(), selectors);
    }

    public static JCSelector get(JCTree.JCExpressionStatement selector, String... selectors) {
        return get(selector.getExpression(), Arrays.asList(selectors));
    }

    public static JCSelector get(JCTree.JCExpression selector, Collection<String> selectors) {
        final TreeMaker maker = JavacEnv.get().maker();
        final Names names = JavacEnv.get().names();
        for (final String sel : selectors) {
            selector = maker.Select(selector, names.fromString(sel));
        }
        return new JCSelector(selector);
    }

    public static JCSelector get(JCTree.JCExpression selector, String... selectors) {
        return get(selector, Arrays.asList(selectors));
    }

    public static JCSelector get(String... selectors) {
        return get(Arrays.asList(selectors));
    }

    public static JCSelector get(Collection<String> selectors) {
        final Iterator<String> iterator = selectors.iterator();
        final TreeMaker maker = JavacEnv.get().maker();
        final Names names = JavacEnv.get().names();
        JCTree.JCExpression selector = maker.Ident(names.fromString(iterator.next()));
        while (iterator.hasNext()) {
            selector = maker.Select(selector, names.fromString(iterator.next()));
        }
        return new JCSelector(selector);
    }

    public JCTree.JCExpression ident() {
        return mSelector;
    }

    public JCTree.JCExpressionStatement assign(String value) {
        final TreeMaker maker = JavacEnv.get().maker();
        return maker.Exec(maker.Assign(ident(), JCSelector.get(value).ident()));
    }

    public JCTree.JCExpressionStatement assign(JCTree.JCExpression expression) {
        final TreeMaker maker = JavacEnv.get().maker();
        return maker.Exec(maker.Assign(ident(), expression));
    }

    public JCTree.JCExpressionStatement invoke(JCTree.JCExpression... args) {
        final TreeMaker maker = JavacEnv.get().maker();
        return maker.Exec(maker.Apply(List.<JCTree.JCExpression>nil(), ident(), List.from(args)));
    }

    public JCTree.JCExpressionStatement invoke(JCTree.JCExpression first, java.util.List<JCTree.JCExpression> args,
                                               int offset, int limit) {
        final Collection<JCTree.JCExpression> combinedArgs = new ArrayList<>();
        combinedArgs.add(first);
        for (int i = offset; i < Math.min(limit, args.size()); ++i) {
            combinedArgs.add(args.get(i));
        }
        return invoke(combinedArgs);
    }

    public JCTree.JCExpressionStatement invoke(Collection<JCTree.JCExpression> args) {
        final TreeMaker maker = JavacEnv.get().maker();
        return maker.Exec(maker.Apply(List.<JCTree.JCExpression>nil(), ident(), List.from(args)));
    }

}
