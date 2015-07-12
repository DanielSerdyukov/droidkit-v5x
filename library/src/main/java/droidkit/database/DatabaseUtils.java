package droidkit.database;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import droidkit.sqlite.SQLiteException;
import droidkit.sqlite.SQLiteStatement;
import droidkit.util.Dynamic;
import droidkit.util.Objects;
import droidkit.util.Strings;

/**
 * @author Daniel Serdyukov
 */
public final class DatabaseUtils {

    private static final boolean JODA_TIME_SUPPORT = Dynamic.inClasspath("org.joda.time.DateTime");

    private static final List<ValueBinder> BINDERS = Arrays.asList(
            new NullBinder(),
            new DoubleBinder(),
            new LongBinder(),
            new BooleanBinder(),
            new BlobBinder(),
            new EnumBinder(),
            new DateTimeBinder(),
            new StringBinder()
    );

    private DatabaseUtils() {
    }

    @NonNull
    public static String getString(@NonNull Cursor cursor, @NonNull String columnName) {
        return Strings.nullToEmpty(cursor.getString(cursor.getColumnIndex(columnName)));
    }

    public static long getLong(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    public static int getInt(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static short getShort(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getShort(cursor.getColumnIndex(columnName));
    }

    public static double getDouble(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    public static float getFloat(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getFloat(cursor.getColumnIndex(columnName));
    }

    public static byte[] getBlob(@NonNull Cursor cursor, @NonNull String columnName) {
        return cursor.getBlob(cursor.getColumnIndex(columnName));
    }

    public static boolean getBoolean(@NonNull Cursor cursor, @NonNull String columnName) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        final int fieldType = cursor.getType(columnIndex);
        switch (fieldType) {
            case Cursor.FIELD_TYPE_INTEGER:
                return cursor.getLong(columnIndex) > 0;
            case Cursor.FIELD_TYPE_FLOAT:
                return cursor.getDouble(columnIndex) > 0;
            case Cursor.FIELD_TYPE_STRING:
                return cursor.getString(columnIndex) != null;
            case Cursor.FIELD_TYPE_BLOB:
                return cursor.getBlob(columnIndex) != null;
            default:
                return false;
        }
    }

    @Nullable
    public static <T extends Enum<T>> T getEnum(@NonNull Cursor cursor, @NonNull String columnName,
                                                @NonNull Class<T> enumType) {
        final String value = getString(cursor, columnName);
        if (!TextUtils.isEmpty(value)) {
            return Enum.valueOf(enumType, value);
        }
        return null;
    }

    @Nullable
    public static BigInteger getBigInt(@NonNull Cursor cursor, @NonNull String columnName) {
        return BigInteger.valueOf(getLong(cursor, columnName));
    }

    @Nullable
    public static BigDecimal getBigDec(@NonNull Cursor cursor, @NonNull String columnName) {
        return BigDecimal.valueOf(getDouble(cursor, columnName));
    }

    @Nullable
    public static DateTime getDateTime(@NonNull Cursor cursor, @NonNull String columnName) {
        return new DateTime(getLong(cursor, columnName));
    }

    public static void bindObjectToStatement(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
        for (final ValueBinder binder : BINDERS) {
            if (binder.canBind(value)) {
                binder.bind(stmt, index, value);
                return;
            }
        }
        throw new SQLiteException("Unsupported sqlite type: " + Objects.requireNonNull(value).getClass());
    }

    //region Binders
    private interface ValueBinder {

        boolean canBind(@Nullable Object value);

        void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value);

    }

    private static class NullBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value == null;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindNull(index);
        }

    }

    private static class LongBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Number;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindLong(index, Objects.requireNonNull((Number) value).longValue());
        }

    }

    private static class DoubleBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Double
                    || value instanceof Float
                    || value instanceof BigDecimal;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindDouble(index, Objects.requireNonNull((Number) value).doubleValue());
        }

    }

    private static class BooleanBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Boolean;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            final boolean bool = Objects.requireNonNull((Boolean) value);
            if (bool) {
                stmt.bindLong(index, 1);
            } else {
                stmt.bindLong(index, 0);
            }
        }

    }

    private static class BlobBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof byte[];
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindBlob(index, Objects.requireNonNull((byte[]) value));
        }

    }

    private static class StringBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof String;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((String) value));
        }

    }

    private static class EnumBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Enum;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((Enum) value).name());
        }

    }

    private static class DateTimeBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return JODA_TIME_SUPPORT && value instanceof DateTime;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindLong(index, Objects.requireNonNull((DateTime) value).getMillis());
        }

    }
    //endregion

}
