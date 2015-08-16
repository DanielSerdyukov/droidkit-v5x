package droidkit.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class ArraysUtils {

    private ArraysUtils() {
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
