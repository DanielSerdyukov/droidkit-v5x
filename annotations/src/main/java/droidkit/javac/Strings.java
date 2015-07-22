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
        return join(glue, iterable, new Func1<T, Object>() {
            @Override
            public Object call(T t) {
                return t;
            }
        });
    }

    static <T> String join(String glue, Iterable<T> iterable, int from) {
        return join(glue, iterable, new Func1<T, Object>() {
            @Override
            public Object call(T t) {
                return t;
            }
        }, from);
    }

    static <T, R> String join(String glue, Iterable<T> iterable, Func1<T, R> transform) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<T> iterator = iterable.iterator();
        if (iterator.hasNext()) {
            do {
                sb.append(transform.call(iterator.next()));
                if (iterator.hasNext()) {
                    sb.append(glue);
                }
            } while (iterator.hasNext());
        }
        return sb.toString();
    }

    static <T, R> String join(String glue, Iterable<T> iterable, Func1<T, R> transform, int from) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<T> iterator = iterable.iterator();
        for (int i = 0; i < from && iterator.hasNext(); ++i) {
            iterator.next();
        }
        if (iterator.hasNext()) {
            do {
                sb.append(transform.call(iterator.next()));
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
