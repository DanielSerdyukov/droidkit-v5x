package droidkit.processor;

import java.util.Iterator;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class Strings {

    private Strings() {
    }

    public static <T> String join(String glue, Iterable<T> iterable) {
        return transformAndJoin(glue, iterable, new Func1<T, String>() {
            @Override
            public String call(T t) {
                return String.valueOf(t);
            }
        });
    }

    public static <T> String join(String glue, Iterable<T> iterable, int offset) {
        return transformAndJoin(glue, iterable, new Func1<T, String>() {
            @Override
            public String call(T t) {
                return String.valueOf(t);
            }
        }, offset);
    }

    public static <T, R> String transformAndJoin(String glue, Iterable<T> iterable, Func1<T, R> func) {
        return transformAndJoin(glue, iterable, func, 0);
    }

    public static <T, R> String transformAndJoin(String glue, Iterable<T> iterable, Func1<T, R> func, int offset) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<T> iterator = iterable.iterator();
        for (int i = 0; i < offset && iterator.hasNext(); ++i) {
            iterator.next();
        }
        if (iterator.hasNext()) {
            do {
                sb.append(func.call(iterator.next()));
                if (iterator.hasNext()) {
                    sb.append(glue);
                }
            } while (iterator.hasNext());
        }
        return sb.toString();
    }

    public static String toUnderScope(String camelCase) {
        return camelCase.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String nonEmpty(String string, String emptyValue) {
        if (isNullOrEmpty(string)) {
            return emptyValue;
        }
        return string;
    }

}
