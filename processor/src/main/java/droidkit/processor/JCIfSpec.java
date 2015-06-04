package droidkit.processor;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

/**
 * @author Daniel Serdyukov
 */
class JCIfSpec {

    private final JCTree.JCExpression mCondition;

    private ListBuffer<JCTree.JCStatement> mThenBlock = new ListBuffer<>();

    private ListBuffer<JCTree.JCStatement> mElseBlock = new ListBuffer<>();

    public JCIfSpec(JCTree.JCExpression cond) {
        mCondition = cond;
    }

    JCIfSpec thenBlock(ListBuffer<JCTree.JCStatement> thenBlock) {
        mThenBlock.addAll(thenBlock);
        return this;
    }

    JCIfSpec thenBlock(JCTree.JCStatement... thenBlock) {
        mThenBlock.addAll(List.from(thenBlock));
        return this;
    }

    JCIfSpec elseBlock(JCTree.JCStatement... elseBlock) {
        mElseBlock.addAll(List.from(elseBlock));
        return this;
    }

    JCTree.JCIf build() {
        return JCUtils.MAKER.If(
                mCondition,
                JCUtils.MAKER.Block(0, mThenBlock.toList()),
                mElseBlock.isEmpty() ? null : JCUtils.MAKER.Block(0, mElseBlock.toList()));
    }

}
