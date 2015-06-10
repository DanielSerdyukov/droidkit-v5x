package droidkit.apt;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;

/**
 * @author Daniel Serdyukov
 */
class JCTypeName {

    public static final JCTypeName BOOLEAN = new JCTypeName(TypeTag.BOOLEAN);

    public static final JCTypeName BYTE = new JCTypeName(TypeTag.BYTE);

    public static final JCTypeName SHORT = new JCTypeName(TypeTag.SHORT);

    public static final JCTypeName INT = new JCTypeName(TypeTag.INT);

    public static final JCTypeName LONG = new JCTypeName(TypeTag.LONG);

    public static final JCTypeName CHAR = new JCTypeName(TypeTag.CHAR);

    public static final JCTypeName FLOAT = new JCTypeName(TypeTag.FLOAT);

    public static final JCTypeName DOUBLE = new JCTypeName(TypeTag.DOUBLE);

    public static final JCTypeName VOID = new JCTypeName(TypeTag.VOID);

    private final TypeTag mTypeTag;

    JCTypeName(TypeTag typeTag) {
        mTypeTag = typeTag;
    }

    public static JCTypeName get(TypeKind kind) {
        switch (kind) {
            case BOOLEAN:
                return JCTypeName.BOOLEAN;
            case BYTE:
                return JCTypeName.BYTE;
            case SHORT:
                return JCTypeName.SHORT;
            case INT:
                return JCTypeName.INT;
            case LONG:
                return JCTypeName.LONG;
            case CHAR:
                return JCTypeName.CHAR;
            case FLOAT:
                return JCTypeName.FLOAT;
            case DOUBLE:
                return JCTypeName.DOUBLE;
            default:
                throw new AssertionError();
        }
    }

    public static JCTypeName get(TypeMirror mirror) {
        return mirror.accept(new SimpleTypeVisitor7<JCTypeName, Void>() {
            @Override
            public JCTypeName visitPrimitive(PrimitiveType t, Void p) {
                return JCTypeName.get(t.getKind());
            }
        }, null);
    }

    public JCTree.JCExpression getIdent() {
        return JavacEnv.get().maker().TypeIdent(mTypeTag);
    }

}
