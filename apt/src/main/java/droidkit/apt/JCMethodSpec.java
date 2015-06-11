package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * @author Daniel Serdyukov
 */
class JCMethodSpec extends JCEmitter {

    private final JCTree.JCModifiers mModifiers;

    private final JCTree.JCExpression mReturnType;

    private final Name mName;

    private final Set<JCTree.JCVariableDecl> mParams;

    private final List<JCTree.JCStatement> mStatements;

    private JCMethodSpec(Builder builder) {
        mModifiers = builder.mModifiers;
        mReturnType = builder.mReturnType.ident();
        mName = builder.mName;
        mParams = Collections.unmodifiableSet(builder.mParams);
        mStatements = builder.mStatements.toList();
    }

    public static Builder builder(String name) {
        return new Builder(JavacEnv.get().names().fromString(name));
    }

    public static Builder constructorBuilder() {
        return new Builder(JavacEnv.get().names().init);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends JCTree> T tree() {
        final TreeMaker maker = JavacEnv.get().maker();
        return (T) maker.MethodDef(
                mModifiers, // modifiers
                mName, // name
                mReturnType, // return type
                List.<JCTree.JCTypeParameter>nil(), // generic types
                List.from(mParams), // params
                List.<JCTree.JCExpression>nil(), // throws
                maker.Block(0, mStatements), // code block
                null
        );
    }

    public static final class Builder {

        private final Name mName;

        private final Set<JCTree.JCVariableDecl> mParams;

        private final ListBuffer<JCTree.JCStatement> mStatements = new ListBuffer<>();

        private JCTree.JCModifiers mModifiers = JCModifier.get();

        private JCTypeName mReturnType;

        private Builder(Name name) {
            mName = name;
            mParams = new LinkedHashSet<>();
            mReturnType = JCTypeName.VOID;
        }

        Builder returnType(JCTypeName returnType) {
            mReturnType = returnType;
            return this;
        }

        Builder modifiers(Modifier... modifiers) {
            mModifiers = JCModifier.get(modifiers);
            return this;
        }

        Builder addParameter(JCVarSpec param) {
            mParams.add(param.<JCTree.JCVariableDecl>tree());
            return this;
        }

        Builder addParameter(JCTypeName typeName, String name, Modifier... modifiers) {
            return addParameter(JCVarSpec.parameterBuilder(typeName, name, modifiers).build());
        }

        Builder addStatement(JCTree.JCStatement statement) {
            mStatements.add(statement);
            return this;
        }

        Builder addStatements(Collection<? extends JCTree.JCStatement> statements) {
            mStatements.addAll(statements);
            return this;
        }

        Builder addReturnStatement(JCTree.JCExpression expression) {
            mStatements.add(JavacEnv.get().maker().Return(expression));
            return this;
        }

        JCMethodSpec build() {
            return new JCMethodSpec(this);
        }

    }

}
