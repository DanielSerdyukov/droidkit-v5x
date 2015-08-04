package droidkit.processor.sqlite;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
class EnumConversion extends StringConversion {

    @Override
    public boolean isAcceptable(ProcessingEnv processingEnv, VariableElement field) {
        return processingEnv.isTypeOfKind(ElementKind.ENUM, field.asType());
    }

    @Override
    public CodeBlock javaType(String fieldName, String columnName, TypeMirror type) {
        return CodeBlock.builder()
                .addStatement("object.$L = $T.getEnum(cursor, $S, $T.class)", fieldName,
                        ClassName.get("droidkit.util", "Cursors"), columnName,
                        ClassName.bestGuess(type.toString()))
                .build();
    }

}
