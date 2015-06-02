package droidkit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface SQLitePk {

    int value() default 5; // SQLiteDatabase.CONFLICT_REPLACE

    String setter() default "";

}
