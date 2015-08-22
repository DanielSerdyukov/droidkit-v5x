package droidkit.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public final class ArraysUtils {

    private ArraysUtils() {
        //no instance
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

}
