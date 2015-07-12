package droidkit.database;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;

import droidkit.sqlite.SQLiteException;
import droidkit.sqlite.SQLiteStatement;
import droidkit.util.Dynamic;
import droidkit.util.Strings;

/**
 * @author Daniel Serdyukov
 */
public final class DatabaseUtils {

    private static final boolean JODA_TIME_SUPPORT = Dynamic.inClasspath("org.joda.time.DateTime");

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
        if (value == null) {
            stmt.bindNull(index);
        } else if (value instanceof Double || value instanceof Float
                || value instanceof BigDecimal) {
            stmt.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof Number) {
            stmt.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Boolean) {
            boolean bool = (boolean) value;
            if (bool) {
                stmt.bindLong(index, 1);
            } else {
                stmt.bindLong(index, 0);
            }
        } else if (value instanceof byte[]) {
            stmt.bindBlob(index, (byte[]) value);
        } else if (value instanceof Enum) {
            stmt.bindString(index, ((Enum) value).name());
        } else if (JODA_TIME_SUPPORT && value instanceof DateTime) {
            stmt.bindLong(index, ((DateTime) value).getMillis());
        } else if (value instanceof String) {
            stmt.bindString(index, (String) value);
        } else {
            throw new SQLiteException("Unsupported sqlite type: " + value.getClass());
        }
    }

}
