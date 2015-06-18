package droidkit.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class DatabaseUtils {

    private DatabaseUtils() {
    }

    @NonNull
    public static String getString(@NonNull Cursor cursor, @NonNull String columnName) {
        return Objects.stringNonNull(cursor.getString(cursor.getColumnIndex(columnName)));
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

    public static void putValue(@NonNull ContentValues values, @NonNull String key, @Nullable Object value) {
        if (value == null) {
            values.putNull(key);
        } else if (value instanceof String) {
            values.put(key, (String) value);
        } else if (value instanceof Integer) {
            values.put(key, (Integer) value);
        } else if (value instanceof Long) {
            values.put(key, (Long) value);
        } else if (value instanceof Float) {
            values.put(key, (Float) value);
        } else if (value instanceof Double) {
            values.put(key, (Double) value);
        } else if (value instanceof Boolean) {
            if (((Boolean) value)) {
                values.put(key, 1);
            } else {
                values.put(key, 0);
            }
        } else if (value instanceof Enum) {
            values.put(key, ((Enum<?>) value).name());
        } else if (value instanceof byte[]) {
            values.put(key, (byte[]) value);
        } else {
            throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
        }
    }

}
