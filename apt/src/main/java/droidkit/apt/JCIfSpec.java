package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.ListBuffer;

import java.util.Collections;

/**
 * @author Daniel Serdyukov
 */
class JCIfSpec extends JCEmitter {

    private final JCTree.JCExpression mCondition;

    private final JCTree.JCBlock mThen;

    private final JCTree.JCBlock mElse;

    private JCIfSpec(Builder builder) {
        final TreeMaker maker = JavacEnv.get().maker();
        mCondition = builder.mCondition;
        mThen = maker.Block(0, builder.mThen.toList());
        if (!builder.mElse.isEmpty()) {
            mElse = maker.Block(0, builder.mElse.toList());
        } else {
            mElse = null;
        }
    }

    public static Builder builder(JCTree.JCExpression condition) {
        return new Builder(condition);
    }

    @Override
    public void emitTo(JCTree.JCMethodDecl methodDecl) {
        final ListBuffer<JCTree.JCStatement> stats = new ListBuffer<>();
        stats.addAll(methodDecl.body.stats);
        stats.add(this.<JCTree.JCTry>tree());
        methodDecl.body = JavacEnv.get().maker().Block(0, stats.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JCTree> T tree() {
        return (T) JavacEnv.get().maker().If(mCondition, mThen, mElse);
    }

    public static class Builder {

        private final JCTree.JCExpression mCondition;

        private final ListBuffer<JCTree.JCStatement> mThen = new ListBuffer<>();

        private final ListBuffer<JCTree.JCStatement> mElse = new ListBuffer<>();

        private Builder(JCTree.JCExpression condition) {
            mCondition = condition;
        }

        public Builder thenStatement(JCTree.JCStatement... statements) {
            Collections.addAll(mThen, statements);
            return this;
        }

        public Builder elseStatement(JCTree.JCStatement... statements) {
            Collections.addAll(mElse, statements);
            return this;
        }


        public JCIfSpec build() {
            return new JCIfSpec(this);
        }

    }

}
