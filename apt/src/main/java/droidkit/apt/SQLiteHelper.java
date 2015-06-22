package droidkit.apt;

import com.sun.tools.javac.tree.JCTree;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import droidkit.annotation.SQLiteObject;

/**
 * @author Daniel Serdyukov
 */
final class SQLiteHelper {

    private final String mTableName;

    private final String mFieldPrefix;

    SQLiteHelper(SQLiteObject meta) {
        mTableName = meta.value();
        mFieldPrefix = meta.fieldPrefix();
    }

    String getTableName() {
        return mTableName;
    }

    String getSQLiteType(TypeMirror type) {
        switch (type.getKind()) {
            case INT:
            case LONG:
            case BOOLEAN:
                return "INTEGER";
            case FLOAT:
            case DOUBLE:
                return "REAL";
            case DECLARED:
                return getDeclaredSQLiteType(type);
            default:
                throw new IllegalArgumentException("Can't convert to sqlite type: " + type);
        }
    }

    String getColumnName(VariableElement field, String columnName) {
        if (columnName != null && columnName.length() > 0) {
            return columnName;
        }
        final String fieldName = field.getSimpleName().toString();
        return fieldName.substring(mFieldPrefix.length()).toLowerCase();
    }

    String getSetterName(VariableElement field, String setterName) {
        if (setterName != null && setterName.length() > 0) {
            return setterName;
        }
        final String fieldName = field.getSimpleName().toString();
        final String name = fieldName.substring(mFieldPrefix.length());
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    JCTree.JCExpression updateColumnStatement(String column) {
        return JCLiteral.stringValue("UPDATE " + mTableName + " SET " + column + " = ? WHERE _id = ?;");
    }

    JCTree.JCExpression insertStatement(Collection<String> columns, boolean nullRowid) {
        final String[] bindings = new String[columns.size()];
        Arrays.fill(bindings, "?");
        if (nullRowid) {
            bindings[0] = "NULL";
        }
        return JCLiteral.stringValue("INSERT INTO " + mTableName + "(" + Utils.join(", ", columns) + ")" +
                " VALUES (" + Utils.join(", ", Arrays.asList(bindings)) + ");");
    }

    List<JCTree.JCExpression> getSQLiteFieldValues(JCTree.JCExpression object, Collection<VariableElement> fields) {
        final List<JCTree.JCExpression> list = new ArrayList<>();
        for (final VariableElement field : fields) {
            list.add(getSQLiteFieldValue(object, field));
        }
        return list;
    }

    JCTree.JCExpression getSQLiteFieldValue(JCTree.JCExpression object, VariableElement field) {
        final TypeMirror type = field.asType();
        switch (type.getKind()) {
            case INT:
            case LONG:
            case BOOLEAN:
            case FLOAT:
            case DOUBLE:
                return JCSelector.getField(object, field).ident();
            case DECLARED:
                return getSQLiteDeclaredFieldValue(object, field);
            default:
                throw new IllegalArgumentException("Can't convert to sqlite value: " + type);
        }
    }

    JCTree.JCExpression getCursorFieldValue(TypeMirror type, String cursor, String column) {
        final JCTree.JCExpression dbUtils = JCSelector.get("droidkit.database", "DatabaseUtils").ident();
        final JCTree.JCExpression cursorVar = JCSelector.get(cursor).ident();
        final JCTree.JCExpression columnVar = JCLiteral.stringValue(column);
        switch (type.getKind()) {
            case INT:
                return JCSelector.get(dbUtils, "getInt").invoke(cursorVar, columnVar).getExpression();
            case LONG:
                return JCSelector.get(dbUtils, "getLong").invoke(cursorVar, columnVar).getExpression();
            case BOOLEAN:
                return JCSelector.get(dbUtils, "getBoolean").invoke(cursorVar, columnVar).getExpression();
            case FLOAT:
                return JCSelector.get(dbUtils, "getFloat").invoke(cursorVar, columnVar).getExpression();
            case DOUBLE:
                return JCSelector.get(dbUtils, "getDouble").invoke(cursorVar, columnVar).getExpression();
            case DECLARED:
                return getCursorDeclaredFieldValue(type, dbUtils, cursorVar, columnVar);
            default:
                throw new IllegalArgumentException("Can't convert cursor value to field value (" + type + ")");
        }
    }

    private String getDeclaredSQLiteType(TypeMirror type) {
        final String typeFqcn = type.toString();
        if (String.class.getName().equals(typeFqcn) || Utils.isEnum(type)) {
            return "TEXT";
        }
        if ("byte[]".equals(typeFqcn)) {
            return "BLOB";
        }
        if (BigInteger.class.getName().equals(typeFqcn)) {
            return "INTEGER";
        }
        if (BigDecimal.class.getName().equals(typeFqcn)) {
            return "REAL";
        }
        throw new IllegalArgumentException("Can't convert to sqlite type: " + type);
    }

    private JCTree.JCExpression getSQLiteDeclaredFieldValue(JCTree.JCExpression object, VariableElement field) {
        final TypeMirror type = field.asType();
        final String typeFqcn = type.toString();
        final JCSelector requireNonNull = JCSelector.get("droidkit.util", "Objects", "requireNonNull");
        final JCTree.JCExpression throwMessage = JCLiteral.stringValue("Declared field is null: " + field);
        if (String.class.getName().equals(typeFqcn) || "byte[]".equals(typeFqcn)) {
            return JCSelector.getField(object, field).ident();
        }
        if (Utils.isEnum(type)) {
            return JCSelector.get(requireNonNull.invoke(JCSelector.getField(object, field).ident(), throwMessage)
                    .getExpression(), "name").invoke().getExpression();
        }
        if (BigInteger.class.getName().equals(typeFqcn)) {
            return JCSelector.get(requireNonNull.invoke(JCSelector.getField(object, field).ident(), throwMessage)
                    .getExpression(), "longValue").invoke().getExpression();
        }
        if (BigDecimal.class.getName().equals(typeFqcn)) {
            return JCSelector.get(requireNonNull.invoke(JCSelector.getField(object, field).ident(), throwMessage)
                    .getExpression(), "doubleValue").invoke().getExpression();
        }
        throw new IllegalArgumentException("Can't convert to sqlite value: " + type);
    }

    private JCTree.JCExpression getCursorDeclaredFieldValue(TypeMirror type, JCTree.JCExpression dbUtils,
                                                            JCTree.JCExpression cursor, JCTree.JCExpression column) {
        final String typeFqcn = type.toString();
        if (String.class.getName().equals(typeFqcn)) {
            return JCSelector.get(dbUtils, "getString").invoke(cursor, column).getExpression();
        }
        if ("byte[]".equals(typeFqcn)) {
            return JCSelector.get(dbUtils, "getBlob").invoke(cursor, column).getExpression();
        }
        if (Utils.isEnum(type)) {
            final TypeElement declared = JavacEnv.get().getElement(type);
            return JCSelector.get(dbUtils, "getEnum").invoke(cursor, column,
                    JCLiteral.clazz(JCClassName.get(declared))).getExpression();
        }
        if (BigInteger.class.getName().equals(typeFqcn)) {
            return JCSelector.get("java.math", "BigInteger", "valueOf")
                    .invoke(JCSelector.get(dbUtils, "getLong")
                            .invoke(cursor, column).getExpression()).getExpression();
        }
        if (BigDecimal.class.getName().equals(typeFqcn)) {
            return JCSelector.get("java.math", "BigDecimal", "valueOf")
                    .invoke(JCSelector.get(dbUtils, "getDouble")
                            .invoke(cursor, column).getExpression()).getExpression();
        }
        throw new IllegalArgumentException("Can't convert cursor value to field value (" + type + ")");
    }

}
