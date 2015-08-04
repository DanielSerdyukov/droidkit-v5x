package droidkit.processor.sqlite;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class BlobConversion extends StringConversion {

    @Override
    public boolean isAcceptable(ProcessingEnv processingEnv, VariableElement field) {
        return TypeKind.ARRAY == field.asType().getKind()
                && "byte[]".equals(field.asType().toString());
    }

    @Override
    public CodeBlock javaType(String fieldName, String columnName, TypeMirror type) {
        return CodeBlock.builder()
                .addStatement("object.$L = $T.getBlob(cursor, $S)", fieldName,
                        ClassName.get("droidkit.util", "Cursors"), columnName)
                .build();
    }

}
