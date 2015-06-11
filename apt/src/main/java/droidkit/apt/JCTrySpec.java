package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Daniel Serdyukov
 */
class JCTrySpec extends JCEmitter {

    private final JCTree.JCBlock mTry;

    private final JCTree.JCBlock mFinally;

    private JCTrySpec(Builder builder) {
        final TreeMaker maker = JavacEnv.get().maker();
        mTry = maker.Block(0, builder.mTry.toList());
        mFinally = maker.Block(0, builder.mFinally.toList());
    }

    public static Builder builder(JCTree.JCBlock block) {
        return new Builder(block.stats);
    }

    public static Builder builder(JCTree.JCStatement... statements) {
        return new Builder(Arrays.asList(statements));
    }

    public static Builder builder(Collection<JCTree.JCStatement> statements) {
        return new Builder(statements);
    }

    @Override
    public void emitTo(JCTree.JCMethodDecl methodDecl) {
        methodDecl.body = JavacEnv.get().maker().Block(0, List.<JCTree.JCStatement>of(this.<JCTree.JCTry>tree()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JCTree> T tree() {
        return (T) JavacEnv.get().maker().Try(mTry, List.<JCTree.JCCatch>nil(), mFinally);
    }

    public static class Builder {

        private final ListBuffer<JCTree.JCStatement> mTry = new ListBuffer<>();

        private final ListBuffer<JCTree.JCStatement> mFinally = new ListBuffer<>();

        private Builder(Collection<JCTree.JCStatement> statements) {
            mTry.addAll(statements);
        }

        public Builder finalize(JCTree.JCStatement... statements) {
            Collections.addAll(mFinally, statements);
            return this;
        }

        public Builder finalize(Collection<JCTree.JCStatement> statements) {
            mFinally.addAll(statements);
            return this;
        }

        public JCTrySpec build() {
            return new JCTrySpec(this);
        }

    }

}
