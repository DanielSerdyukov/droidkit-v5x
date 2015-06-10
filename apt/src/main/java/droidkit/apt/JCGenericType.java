package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Daniel Serdyukov
 */
class JCGenericType extends JCTypeName {

    private final JCTypeName mTypeName;

    private final java.util.List<JCTypeName> mGenericTypes;

    private JCGenericType(TypeTag typeTag, JCTypeName typeName, java.util.List<JCTypeName> types) {
        super(typeTag);
        mTypeName = typeName;
        mGenericTypes = Collections.unmodifiableList(types);
    }

    public static JCGenericType get(Class<?> clazz, Class<?>... types) {
        final java.util.List<JCTypeName> genericTypes = new ArrayList<>(types.length);
        for (final Class<?> type : types) {
            genericTypes.add(JCClassName.get(type));
        }
        return new JCGenericType(TypeTag.UNKNOWN, JCClassName.get(clazz), genericTypes);
    }

    public static JCGenericType get(JCTypeName type, JCTypeName... types) {
        return new JCGenericType(TypeTag.UNKNOWN, type, Arrays.asList(types));
    }

    @Override
    public JCTree.JCExpression getIdent() {
        return JavacEnv.get().maker().TypeApply(mTypeName.getIdent(), getGenericTypes());
    }

    public JCTree.JCExpression newInstance(JCTree.JCExpression... args) {
        return JavacEnv.get().maker().NewClass(
                null, // enclosing
                getGenericTypes(), // generic types
                getIdent(), // type
                List.from(args), // args
                null
        );
    }

    private List<JCTree.JCExpression> getGenericTypes() {
        final ListBuffer<JCTree.JCExpression> genericTypes = new ListBuffer<>();
        for (final JCTypeName genericType : mGenericTypes) {
            genericTypes.add(genericType.getIdent());
        }
        return genericTypes.toList();
    }

}
