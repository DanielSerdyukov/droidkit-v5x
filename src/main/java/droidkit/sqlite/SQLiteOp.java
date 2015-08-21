package droidkit.sqlite;

import android.provider.BaseColumns;

/**
 * @author Daniel Serdyukov
 */
interface SQLiteOp {

    String ID_EQ = BaseColumns._ID + " = ?";

    String EQ = " = ?";

    String NOT_EQ = " <> ?";

    String LT = " < ?";

    String LT_OR_EQ = " <= ?";

    String GT = " > ?";

    String GT_OR_EQ = " >= ?";

    String LIKE = " LIKE ?";

    String BETWEEN = " BETWEEN ? AND ?";

    int TRUE = 1;

    int FALSE = 0;

    String IS_NULL = " IS NULL";

    String NOT_NULL = " NOT NULL";

    String COMMA = ", ";

    String ASC = " ASC";

    String DESC = " DESC";

    String AND = " AND ";

    String OR = " OR ";

    String LEFT_PARENTHESIS = "(";

    String RIGHT_PARENTHESIS = ")";

    String WHERE = " WHERE ";

}
