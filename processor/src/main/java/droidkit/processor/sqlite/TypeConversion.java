package droidkit.processor.sqlite;

import com.squareup.javapoet.CodeBlock;

import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import droidkit.processor.ProcessingEnv;

/**
 * @author Daniel Serdyukov
 */
interface TypeConversion {

    List<TypeConversion> SUPPORTED = Arrays.asList(
            new IntConversion(),
            new LongConversion(),
            new DoubleConversion(),
            new BooleanConversion(),
            new StringConversion(),
            new EnumConversion(),
            new FloatConversion(),
            new ShortConversion(),
            new BigIntegerConversion(),
            new BigDecimalConversion(),
            new DateTimeConversion(),
            new BlobConversion()
    );

    boolean isAcceptable(ProcessingEnv processingEnv, VariableElement field);

    String sqliteType();

    CodeBlock javaType(String fieldName, String columnName, TypeMirror type);

}
