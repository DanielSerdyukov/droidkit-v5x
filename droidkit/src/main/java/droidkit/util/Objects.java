package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public abstract class Objects {

    private Objects() {
    }

    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return object;
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T object, @NonNull T nullDefault) {
        if (object == null) {
            return nullDefault;
        }
        return object;
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T object, @NonNull String throwMessage) {
        if (object == null) {
            throw new NullPointerException(throwMessage);
        }
        return object;
    }

    @NonNull
    public static String toString(@Nullable Object object) {
        return String.valueOf(object);
    }

    @NonNull
    public static String toString(@Nullable Object object, @NonNull String nullString) {
        if (object == null) {
            return nullString;
        }
        return String.valueOf(object);
    }

}
