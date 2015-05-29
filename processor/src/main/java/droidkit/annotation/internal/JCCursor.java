package droidkit.annotation.internal;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

/**
 * @author Daniel Serdyukov
 */
class JCCursor {

    static JCTree.JCExpression getValue(String cursorVar, String methodName, String columnName) {
        return JCUtils.MAKER.Exec(JCUtils.MAKER.Apply(
                List.<JCTree.JCExpression>nil(),
                JCUtils.select("droidkit.database", "CursorUtils", methodName),
                List.of(
                        JCUtils.MAKER.Ident(JCUtils.NAMES.fromString(cursorVar)),
                        JCUtils.MAKER.Literal(TypeTag.CLASS, columnName)
                ))).getExpression();
    }

}
