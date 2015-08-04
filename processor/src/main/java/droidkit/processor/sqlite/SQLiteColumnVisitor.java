package droidkit.processor.sqlite;

import java.lang.annotation.Annotation;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import droidkit.annotation.SQLiteColumn;
import droidkit.processor.ProcessingEnv;
import droidkit.processor.Strings;

/**
 * @author Daniel Serdyukov
 */
class SQLiteColumnVisitor implements FieldVisitor {

    @Override
    public Annotation getAnnotation(ProcessingEnv processingEnv, VariableElement field) {
        return field.getAnnotation(SQLiteColumn.class);
    }

    @Override
    public void visit(SQLiteObjectScanner scanner, ProcessingEnv processingEnv, VariableElement field,
                      Annotation annotation) {
        final SQLiteColumn column = (SQLiteColumn) annotation;
        final String fieldName = field.getSimpleName().toString();
        final String columnName = getColumnName(fieldName, column.value());
        final TypeConversion conversion = getTypeConversion(processingEnv, field);
        scanner.addColumnDef(columnName + conversion.sqliteType());
        scanner.putFieldToColumn(fieldName, columnName);
        scanner.putFieldToSetter(fieldName, column.setter());
        scanner.addInstantiateStatement(conversion.javaType(fieldName, columnName, field.asType()));
    }

    private TypeConversion getTypeConversion(ProcessingEnv processingEnv, VariableElement field) {
        for (final TypeConversion conversion : TypeConversion.SUPPORTED) {
            if (conversion.isAcceptable(processingEnv, field)) {
                return conversion;
            }
        }
        processingEnv.printMessage(Diagnostic.Kind.ERROR, field, "Unsupported java -> sqlite type conversion");
        throw new IllegalArgumentException("Unsupported java -> sqlite type conversion");
    }

    private String getColumnName(String fieldName, String columnName) {
        if (columnName.isEmpty()) {
            if ('m' == fieldName.charAt(0)
                    && Character.isUpperCase(fieldName.charAt(1))) {
                columnName = Strings.toUnderScope(fieldName.substring(1));
            } else {
                columnName = Strings.toUnderScope(fieldName);
            }
        }
        return columnName;
    }

}
