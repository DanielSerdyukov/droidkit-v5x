package droidkit.annotation.internal;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import javax.lang.model.element.Element;

/**
 * @author Daniel Serdyukov
 */
class JCNewSpec {

    private JCTree.JCExpression mType;

    private ListBuffer<JCTree.JCExpression> mGenericTypes = new ListBuffer<>();

    private ListBuffer<JCTree.JCExpression> mArguments = new ListBuffer<>();

    JCNewSpec(JCTree.JCExpression type) {
        mType = type;
    }

    JCNewSpec(Element name) {
        mType = JCUtils.ident(name);
    }

    JCNewSpec(Class<?> clazz) {
        mType = JCUtils.ident(clazz);
    }

    JCNewSpec genericTypes(JCTree.JCExpression... types) {
        mGenericTypes.addAll(List.from(types));
        return this;
    }

    JCNewSpec args(JCTree.JCExpression... args) {
        mArguments.addAll(List.from(args));
        return this;
    }

    JCTree.JCNewClass build() {
        if (!mGenericTypes.isEmpty()) {
            mType = JCUtils.MAKER.TypeApply(mType, mGenericTypes.toList());
        }
        return JCUtils.MAKER.NewClass(null, List.<JCTree.JCExpression>nil(), mType, mArguments.toList(), null);
    }

}
