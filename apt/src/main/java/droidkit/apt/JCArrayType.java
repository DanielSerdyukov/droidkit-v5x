package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.ListBuffer;

/**
 * @author Daniel Serdyukov
 */
class JCArrayType extends JCTypeName {

    private final JCTypeName mTypeName;

    private JCArrayType(TypeTag typeTag, JCTypeName typeName) {
        super(typeTag);
        mTypeName = typeName;
    }

    public static JCArrayType get(Class<?> clazz) {
        return new JCArrayType(TypeTag.ARRAY, JCClassName.get(clazz));
    }

    public static JCArrayType get(JCTypeName type) {
        return new JCArrayType(TypeTag.ARRAY, type);
    }

    @Override
    public JCTree.JCExpression ident() {
        return JavacEnv.get().maker().TypeArray(mTypeName.ident());
    }

    public JCTree.JCExpression newArray(int... sizes) {
        final ListBuffer<JCTree.JCExpression> dimensions = new ListBuffer<>();
        for (final int size : sizes) {
            dimensions.add(JCLiteral.intValue(size));
        }
        return JavacEnv.get().maker().NewArray(mTypeName.ident(), dimensions.toList(), null);
    }

}
