package droidkit.processor.sqlite;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class DateTimeConversion extends DoubleConversion {

    @Override
    public boolean isAcceptable(ProcessingEnv processingEnv, VariableElement field) {
        return processingEnv.isSubtype(field.asType(), "org.joda.time.DateTime");
    }

    @Override
    public CodeBlock javaType(String fieldName, String columnName, TypeMirror type) {
        return CodeBlock.builder()
                .addStatement("object.$L = $T.getDateTime(cursor, $S)", fieldName,
                        ClassName.get("droidkit.util", "Cursors"), columnName)
                .build();
    }

}
