package droidkit.javac;

import java.util.Iterator;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
final class Strings {

    private Strings() {
    }

    static <T> String join(String glue, Iterable<T> iterable) {
        return transformAndJoin(glue, iterable, new Func1<T, String>() {
            @Override
            public String call(T t) {
                return String.valueOf(t);
            }
        });
    }

    static <T, R> String transformAndJoin(String glue, Iterable<T> iterable, Func1<T, R> func) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<T> iterator = iterable.iterator();
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

    static String toUnderScope(String camelCase) {
        return camelCase.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String nonEmpty(String string, String emptyValue) {
        if (isNullOrEmpty(string)) {
            return emptyValue;
        }
        return string;
    }

}
