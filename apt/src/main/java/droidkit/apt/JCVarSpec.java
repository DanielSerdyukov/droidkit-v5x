package droidkit.apt;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Name;

import javax.lang.model.element.Modifier;

/**
 * @author Daniel Serdyukov
 */
final class JCVarSpec extends JCEmitter {

    private final JCTree.JCExpression mType;

    private final Name mName;

    private final JCTree.JCModifiers mModifiers;

    private final JCTree.JCExpression mInit;

    JCVarSpec(Builder builder) {
        mType = builder.mType.ident();
        mName = JavacEnv.get().names().fromString(builder.mName);
        mModifiers = builder.mModifiers;
        mInit = builder.mInit;
    }

    public static Builder builder(JCTypeName type, String name, Modifier... modifiers) {
        return new Builder(type, name, JCModifier.get(modifiers));
    }

    public static Builder parameterBuilder(JCTypeName type, String name, Modifier... modifiers) {
        return new Builder(type, name, JCModifier.get(Flags.PARAMETER, modifiers));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JCTree> T tree() {
        final TreeMaker maker = JavacEnv.get().maker();
        return (T) maker.VarDef(
                mModifiers, // modifiers
                mName, // name
                mType, // type
                mInit // init
        );
    }

    public JCTree.JCExpression ident() {
        return JavacEnv.get().maker().Ident(mName);
    }

    public JCTree.JCExpression assign(JCVarSpec var) {
        final TreeMaker maker = JavacEnv.get().maker();
        return maker.Assign(maker.Ident(mName), maker.Ident(var.mName));
    }

    public JCTree.JCExpressionStatement execAssign(JCVarSpec var) {
        return JavacEnv.get().maker().Exec(assign(var));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JCVarSpec jcVarSpec = (JCVarSpec) o;
        return !(mName != null ? !mName.equals(jcVarSpec.mName) : jcVarSpec.mName != null);
    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    public static final class Builder {

        private final JCTypeName mType;

        private final String mName;

        private final JCTree.JCModifiers mModifiers;

        private JCTree.JCExpression mInit;

        private Builder(JCTypeName type, String name, JCTree.JCModifiers modifiers) {
            mType = type;
            mName = name;
            mModifiers = modifiers;
        }

        public Builder init(JCTree.JCExpression expression) {
            mInit = expression;
            return this;
        }

        public Builder init(JCSelector selector) {
            mInit = selector.ident();
            return this;
        }

        public Builder init(String... selectors) {
            mInit = JCSelector.get(selectors).ident();
            return this;
        }

        public JCVarSpec build() {
            return new JCVarSpec(this);
        }

    }

}
