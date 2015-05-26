package droidkit.content;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

    int intValue() default IntValue.EMPTY;

    long longValue() default LongValue.EMPTY;

    double doubleValue() default DoubleValue.EMPTY;

    float floatValue() default FloatValue.EMPTY;

    boolean boolValue() default BoolValue.EMPTY;

    String stringValue() default StringValue.EMPTY;

}
