package droidkit.javac;

import com.google.common.collect.Lists;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
class JCTypes {

    private final Names mNames;

    private final TreeMaker mMaker;

    private JCTypes(JavacProcessingEnvironment javacEnv) {
        mNames = Names.instance(javacEnv.getContext());
        mMaker = TreeMaker.instance(javacEnv.getContext());
    }

    static JCTypes instance(JavacProcessingEnvironment javacEnv) {
        return new JCTypes(javacEnv);
    }

    JCTree.JCExpression getClass(Class<?> type) {
        final List<String> names = Lists.newArrayList();
        for (Class<?> c = type; c != null; c = c.getEnclosingClass()) {
            names.add(c.getSimpleName());
        }
        if (type.getPackage() != null) {
            names.add(type.getPackage().getName());
        }
        Collections.reverse(names);
        return ident(names);
    }

    JCTree.JCExpression ident(Iterable<String> selectors) {
        final Iterator<String> iterator = selectors.iterator();
        JCTree.JCExpression selector = mMaker.Ident(mNames.fromString(iterator.next()));
        while (iterator.hasNext()) {
            selector = mMaker.Select(selector, mNames.fromString(iterator.next()));
        }
        return selector;
    }

    JCTree.JCExpression thisIdent(String name) {
        return mMaker.Select(mMaker.Ident(mNames._this), mNames.fromString(name));
    }

}
