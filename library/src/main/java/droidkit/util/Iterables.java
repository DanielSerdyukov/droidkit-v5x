package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public final class Iterables {

    private Iterables() {
    }

    @NonNull
    public static <T> T getFirst(@NonNull Iterable<T> iterable) {
        if (iterable instanceof List) {
            return Lists.getFirst((List<T>) iterable);
        }
        return iterable.iterator().next();
    }

    @Nullable
    public static <T> T getFirst(@NonNull Iterable<T> iterable, @Nullable T emptyValue) {
        if (iterable instanceof Collection) {
            final Collection<T> collection = (Collection<T>) iterable;
            if (collection.isEmpty()) {
                return emptyValue;
            } else if (iterable instanceof List) {
                return Lists.getFirst((List<T>) iterable, emptyValue);
            }
        }
        return iterable.iterator().next();
    }

    @NonNull
    public static <T> T getLast(@NonNull Iterable<T> iterable) {
        if (iterable instanceof List) {
            return Lists.getLast((List<T>) iterable);
        }
        return getLast(iterable.iterator());
    }

    @Nullable
    public static <T> T getLast(@NonNull Iterable<T> iterable, @Nullable T emptyValue) {
        if (iterable instanceof Collection) {
            final Collection<T> collection = (Collection<T>) iterable;
            if (collection.isEmpty()) {
                return emptyValue;
            } else if (iterable instanceof List) {
                return Lists.getLast((List<T>) iterable, emptyValue);
            }
        }
        return getLast(iterable.iterator());
    }

    @NonNull
    public static <T, R> Iterable<R> transform(@NonNull Iterable<T> iterable, @NonNull Func1<T, R> transform) {
        if (iterable instanceof List) {
            return Lists.transform((List<T>) iterable, transform);
        }
        final List<R> transformed = new ArrayList<>();
        for (final T element : iterable) {
            transformed.add(transform.call(element));
        }
        return transformed;
    }

    private static <T> T getLast(@NonNull Iterator<T> iterator) {
        T last = iterator.next();
        while (iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
    }

}
