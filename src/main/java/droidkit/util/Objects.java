package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Daniel Serdyukov
 *         java.util.Objects compatibility class
 */
public abstract class Objects {

    private static final List<ArrayEquals> DEEP_EQUALS = Arrays.asList(
            new ObjectArrayEquals(),
            new BooleanArrayEquals(),
            new ByteArrayEquals(),
            new CharArrayEquals(),
            new DoubleArrayEquals(),
            new FloatArrayEquals(),
            new IntArrayEquals(),
            new LongArrayEquals(),
            new ShortArrayEquals()
    );

    private Objects() {
    }

    public static <T> int compare(@Nullable T a, @Nullable T b, @NonNull Comparator<? super T> c) {
        if (a == b) {
            return 0;
        }
        return c.compare(a, b);
    }

    public static boolean deepEquals(@Nullable Object a, @Nullable Object b) {
        if (a == null || b == null) {
            return a == b;
        }
        for (final ArrayEquals func : DEEP_EQUALS) {
            if (func.isAcceptable(a, b)) {
                return func.deepEquals(a, b);
            }
        }
        return a.equals(b);
    }

    @SuppressWarnings("squid:S1221")
    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public static int hash(Object... values) {
        return Arrays.hashCode(values);
    }

    @NonNull
    public static <T> T notNull(@Nullable T o, @NonNull T nullValue) {
        if (o == null) {
            return nullValue;
        }
        return o;
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return o;
    }

    @NonNull
    public static <T> T requireNonNull(@Nullable T o, @NonNull String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
        return o;
    }

    @NonNull
    public static String toString(@Nullable Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString();
    }

    @NonNull
    public static String toString(@Nullable Object o, @NonNull String nullString) {
        if (o == null) {
            return nullString;
        }
        return o.toString();
    }

    //region equals functions
    private interface ArrayEquals {

        boolean isAcceptable(@Nullable Object a, @Nullable Object b);

        boolean deepEquals(@NonNull Object a, @NonNull Object b);

    }

    private static class ObjectArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof Object[] && b instanceof Object[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.deepEquals((Object[]) a, (Object[]) b);
        }

    }

    private static class BooleanArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof boolean[] && b instanceof boolean[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((boolean[]) a, (boolean[]) b);
        }

    }

    private static class ByteArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof byte[] && b instanceof byte[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((byte[]) a, (byte[]) b);
        }

    }

    private static class CharArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof char[] && b instanceof char[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((char[]) a, (char[]) b);
        }

    }

    private static class DoubleArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof double[] && b instanceof double[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((double[]) a, (double[]) b);
        }

    }

    private static class FloatArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof float[] && b instanceof float[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((float[]) a, (float[]) b);
        }

    }

    private static class IntArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof int[] && b instanceof int[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((int[]) a, (int[]) b);
        }

    }

    private static class LongArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof long[] && b instanceof long[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((long[]) a, (long[]) b);
        }

    }

    private static class ShortArrayEquals implements ArrayEquals {

        @Override
        public boolean isAcceptable(@Nullable Object a, @Nullable Object b) {
            return a instanceof short[] && b instanceof short[];
        }

        @Override
        public boolean deepEquals(@NonNull Object a, @NonNull Object b) {
            return Arrays.equals((short[]) a, (short[]) b);
        }

    }
    //endregion

}
