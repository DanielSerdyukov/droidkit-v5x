package droidkit.processor;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

/**
 * @author Daniel Serdyukov
 */
class JCCursor {

    static JCTree.JCExpressionStatement getValue(String cursorVar, String methodName, String columnName) {
        return JCUtils.invoke(JCUtils.select("droidkit.database", "DatabaseUtils", methodName),
                JCUtils.MAKER.Ident(JCUtils.NAMES.fromString(cursorVar)),
                JCUtils.MAKER.Literal(TypeTag.CLASS, columnName));
    }

    static JCTree.JCExpressionStatement getEnumValue(JCTree.JCExpression enumClass, String cursorVar,
                                                     String columnName) {
        return JCUtils.invoke(JCUtils.select("droidkit.database", "DatabaseUtils", "getEnum"),
                JCUtils.MAKER.Ident(JCUtils.NAMES.fromString(cursorVar)),
                JCUtils.MAKER.Literal(TypeTag.CLASS, columnName), enumClass);
    }

}
