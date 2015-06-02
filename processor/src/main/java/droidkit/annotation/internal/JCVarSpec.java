package droidkit.annotation.internal;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * @author Daniel Serdyukov
 */
class JCVarSpec {

    private final Name mName;

    private JCTree.JCModifiers mModifiers = JCUtils.MAKER.Modifiers(0);

    private List<JCTree.JCExpression> mGenericTypes;

    private JCTree.JCExpression mVarType;

    private JCTree.JCExpression mInit;

    JCVarSpec(String name) {
        mName = JCUtils.NAMES.fromString(name);
    }

    JCVarSpec modifiers(long flags) {
        mModifiers = JCUtils.MAKER.Modifiers(flags);
        return this;
    }

    JCVarSpec genericTypes(JCTree.JCExpression... types) {
        mGenericTypes = List.from(types);
        return this;
    }

    JCVarSpec varType(JCTree.JCExpression varType) {
        mVarType = varType;
        return this;
    }

    JCVarSpec init(JCTree.JCExpression init) {
        mInit = init;
        return this;
    }

    JCVarSpec initWithNew() {
        mInit = new JCNewSpec(mVarType).build();
        return this;
    }

    JCTree.JCVariableDecl build() {
        if (mGenericTypes != null) {
            return JCUtils.MAKER.VarDef(mModifiers, mName, JCUtils.MAKER.TypeApply(mVarType, mGenericTypes), mInit);
        }
        return JCUtils.MAKER.VarDef(mModifiers, mName, mVarType, mInit);
    }

}
