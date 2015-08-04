package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class Arrays2 {

    private Arrays2() {
    }

    @NonNull
    public static <T> T getFirst(@NonNull T[] array) {
        checkNotEmpty(array);
        return array[0];
    }

    @Nullable
    public static <T> T getFirst(@NonNull T[] array, @Nullable T emptyValue) {
        if (array.length == 0) {
            return emptyValue;
        }
        return array[0];
    }

    @NonNull
    public static <T> T getLast(@NonNull T[] array) {
        checkNotEmpty(array);
        return array[array.length - 1];
    }

    @Nullable
    public static <T> T getLast(@NonNull T[] array, @Nullable T emptyValue) {
        if (array.length == 0) {
            return emptyValue;
        }
        return array[array.length - 1];
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T, R> R[] transform(@NonNull T[] array, @NonNull Func1<T, R> func1, @NonNull Class<R> type) {
        final R[] transformed = (R[]) Array.newInstance(type, array.length);
        for (int i = 0; i < array.length; ++i) {
            transformed[i] = func1.call(array[i]);
        }
        return transformed;
    }

    private static <T> void checkNotEmpty(@NonNull T[] array) {
        if (array.length == 0) {
            throw new NoSuchElementException("array is empty");
        }
    }

}
