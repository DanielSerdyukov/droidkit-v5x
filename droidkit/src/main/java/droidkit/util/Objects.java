package droidkit.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Daniel Serdyukov
 *         java.util.Objects compatibility class
 */
public abstract class Objects {

    private static final ObjectsVersion IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            IMPL = new ObjectsKitKat();
        } else {
            IMPL = new ObjectsCompat();
        }
    }

    private Objects() {
    }

    public static <T> int compare(@Nullable T a, @Nullable T b, @NonNull Comparator<? super T> c) {
        return IMPL.compare(a, b, c);
    }

    public static boolean deepEquals(Object a, Object b) {
        return IMPL.deepEquals(a, b);
    }

    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return IMPL.equal(a, b);
    }

    public static int hash(Object... values) {
        return IMPL.hash(values);
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T object) {
        return IMPL.requireNonNull(object);
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T object, @NonNull String throwMessage) {
        return IMPL.requireNonNull(object, throwMessage);
    }

    @NonNull
    public static <T> T nullToDefault(@Nullable T object, @NonNull T nullDefault) {
        if (object == null) {
            return nullDefault;
        }
        return object;
    }

    @NonNull
    public static String toString(@Nullable Object object) {
        return IMPL.toString(object);
    }

    @NonNull
    public static String toString(@Nullable Object object, @NonNull String nullString) {
        return IMPL.toString(object, nullString);
    }

    private interface ObjectsVersion {

        <T> int compare(@Nullable T a, @Nullable T b, @NonNull Comparator<? super T> c);

        boolean deepEquals(Object a, Object b);

        boolean equal(@Nullable Object a, @Nullable Object b);

        int hash(Object... values);

        @NonNull
        <T> T requireNonNull(@Nullable T o);

        @NonNull
        <T> T requireNonNull(@Nullable T o, @NonNull String message);

        @NonNull
        String toString(@Nullable Object o);

        @NonNull
        String toString(@Nullable Object o, @NonNull String nullString);

    }

    private static class ObjectsCompat implements ObjectsVersion {

        @Override
        public <T> int compare(@Nullable T a, @Nullable T b, @NonNull Comparator<? super T> c) {
            if (a == b) {
                return 0;
            }
            return c.compare(a, b);
        }

        @Override
        public boolean deepEquals(@Nullable Object a, @Nullable Object b) {
            if (a == null || b == null) {
                return a == b;
            } else if (a instanceof Object[] && b instanceof Object[]) {
                return Arrays.deepEquals((Object[]) a, (Object[]) b);
            } else if (a instanceof boolean[] && b instanceof boolean[]) {
                return Arrays.equals((boolean[]) a, (boolean[]) b);
            } else if (a instanceof byte[] && b instanceof byte[]) {
                return Arrays.equals((byte[]) a, (byte[]) b);
            } else if (a instanceof char[] && b instanceof char[]) {
                return Arrays.equals((char[]) a, (char[]) b);
            } else if (a instanceof double[] && b instanceof double[]) {
                return Arrays.equals((double[]) a, (double[]) b);
            } else if (a instanceof float[] && b instanceof float[]) {
                return Arrays.equals((float[]) a, (float[]) b);
            } else if (a instanceof int[] && b instanceof int[]) {
                return Arrays.equals((int[]) a, (int[]) b);
            } else if (a instanceof long[] && b instanceof long[]) {
                return Arrays.equals((long[]) a, (long[]) b);
            } else if (a instanceof short[] && b instanceof short[]) {
                return Arrays.equals((short[]) a, (short[]) b);
            }
            return a.equals(b);
        }

        @Override
        public boolean equal(@Nullable Object a, @Nullable Object b) {
            return (a == null) ? (b == null) : a.equals(b);
        }

        @Override
        public int hash(Object... values) {
            return Arrays.hashCode(values);
        }

        @NonNull
        @Override
        public <T> T requireNonNull(@Nullable T o) {
            if (o == null) {
                throw new NullPointerException();
            }
            return o;
        }

        @NonNull
        @Override
        public <T> T requireNonNull(@Nullable T o, @NonNull String message) {
            if (o == null) {
                throw new NullPointerException(message);
            }
            return o;
        }

        @NonNull
        @Override
        public String toString(@Nullable Object o) {
            if (o == null) {
                return "null";
            }
            return o.toString();
        }

        @NonNull
        @Override
        public String toString(@Nullable Object o, @NonNull String nullString) {
            if (o == null) {
                return nullString;
            }
            return o.toString();
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static class ObjectsKitKat implements ObjectsVersion {

        @Override
        public <T> int compare(@Nullable T a, @Nullable T b, @NonNull Comparator<? super T> c) {
            return java.util.Objects.compare(a, b, c);
        }

        @Override
        public boolean deepEquals(Object a, Object b) {
            return java.util.Objects.deepEquals(a, b);
        }

        @Override
        public boolean equal(@Nullable Object a, @Nullable Object b) {
            return java.util.Objects.equals(a, b);
        }

        @Override
        public int hash(Object... values) {
            return java.util.Objects.hash(values);
        }

        @NonNull
        @Override
        public <T> T requireNonNull(@Nullable T o) {
            return java.util.Objects.requireNonNull(o);
        }

        @NonNull
        @Override
        public <T> T requireNonNull(@Nullable T o, @NonNull String message) {
            return java.util.Objects.requireNonNull(o, message);
        }

        @NonNull
        @Override
        public String toString(@Nullable Object o) {
            return java.util.Objects.toString(o);
        }

        @NonNull
        @Override
        public String toString(@Nullable Object o, @NonNull String nullString) {
            return java.util.Objects.toString(o, nullString);
        }

    }

}
