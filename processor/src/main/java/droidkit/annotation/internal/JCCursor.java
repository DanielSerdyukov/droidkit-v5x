package droidkit.annotation.internal;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

/**
 * @author Daniel Serdyukov
 */
class JCCursor {

    static JCTree.JCExpression getValue(String cursorVar, String methodName, String columnName) {
        return JCUtils.invoke(JCUtils.select("droidkit.database", "CursorUtils", methodName),
                JCUtils.MAKER.Ident(JCUtils.NAMES.fromString(cursorVar)),
                JCUtils.MAKER.Literal(TypeTag.CLASS, columnName)).getExpression();
    }

}
