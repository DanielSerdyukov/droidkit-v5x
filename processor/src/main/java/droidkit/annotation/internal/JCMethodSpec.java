package droidkit.annotation.internal;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;

/**
 * @author Daniel Serdyukov
 */
class JCMethodSpec {

    private final Name mName;

    private JCTree.JCExpression mReturnType = JCUtils.MAKER.TypeIdent(TypeTag.VOID);

    private JCTree.JCModifiers mModifiers = JCUtils.MAKER.Modifiers(Flags.PRIVATE);

    private List<JCTree.JCTypeParameter> mGenericTypes = List.nil();

    private ListBuffer<JCTree.JCVariableDecl> mParams = new ListBuffer<>();

    private ListBuffer<JCTree.JCStatement> mStatements = new ListBuffer<>();

    JCMethodSpec(String name) {
        mName = JCUtils.NAMES.fromString(name);
    }

    JCMethodSpec returnType(JCTree.JCExpression returnType) {
        mReturnType = returnType;
        return this;
    }

    JCMethodSpec modifiers(long flags) {
        mModifiers = JCUtils.MAKER.Modifiers(flags);
        return this;
    }

    JCMethodSpec genericTypes(JCTree.JCTypeParameter... types) {
        mGenericTypes = List.from(types);
        return this;
    }

    JCMethodSpec params(JCTree.JCVariableDecl... params) {
        mParams.addAll(List.from(params));
        return this;
    }

    JCMethodSpec statements(JCTree.JCStatement... statements) {
        mStatements.addAll(List.from(statements));
        return this;
    }

    JCMethodSpec statements(List<JCTree.JCStatement> statements) {
        mStatements.addAll(statements);
        return this;
    }

    JCTree.JCMethodDecl build() {
        return JCUtils.MAKER.MethodDef(
                mModifiers,
                mName,
                mReturnType,
                mGenericTypes,
                mParams.toList(),
                List.<JCTree.JCExpression>nil(), // throws
                JCUtils.MAKER.Block(0, mStatements.toList()),
                null);
    }

}
