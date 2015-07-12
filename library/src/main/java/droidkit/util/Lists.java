package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public final class Lists {

    private Lists() {
    }

    @NonNull
    public static <T> T getFirst(@NonNull List<T> list) {
        checkNotEmpty(list);
        return list.get(0);
    }

    @Nullable
    public static <T> T getFirst(@NonNull List<T> list, @Nullable T emptyValue) {
        if (list.isEmpty()) {
            return emptyValue;
        }
        return list.get(0);
    }

    @NonNull
    public static <T> T getLast(@NonNull List<T> list) {
        checkNotEmpty(list);
        return list.get(list.size() - 1);
    }

    @Nullable
    public static <T> T getLast(@NonNull List<T> list, @Nullable T emptyValue) {
        if (list.isEmpty()) {
            return emptyValue;
        }
        return list.get(list.size() - 1);
    }

    @NonNull
    public static <T, R> List<R> transform(@NonNull List<T> list, @NonNull Func1<T, R> transform) {
        final List<R> transformed = new ArrayList<>(list.size());
        for (final T element : list) {
            transformed.add(transform.call(element));
        }
        return transformed;
    }

    private static <T> void checkNotEmpty(@NonNull List<T> list) {
        if (list.isEmpty()) {
            throw new NoSuchElementException("list is empty");
        }
    }

}
