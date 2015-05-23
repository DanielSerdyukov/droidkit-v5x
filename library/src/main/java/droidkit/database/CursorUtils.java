package droidkit.database;

import android.database.Cursor;
import android.support.annotation.NonNull;

import droidkit.util.Dynamic;
import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class CursorUtils {

    private CursorUtils() {
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

    @NonNull
    public static Object getTypedValue(@NonNull Cursor cursor, @NonNull String columnName, @NonNull Class<?> type) {
        final Class<?> unboxedType = Dynamic.unbox(type);
        if (unboxedType == Integer.class) {
            return getInt(cursor, columnName);
        } else if (unboxedType == Long.class) {
            return getLong(cursor, columnName);
        } else if (unboxedType == Double.class) {
            return getDouble(cursor, columnName);
        } else if (unboxedType == Float.class) {
            return getFloat(cursor, columnName);
        } else if (unboxedType == Short.class) {
            return getShort(cursor, columnName);
        } else if (unboxedType == byte[].class) {
            return getBlob(cursor, columnName);
        } else if (unboxedType == Boolean.class) {
            return getBoolean(cursor, columnName);
        } else {
            return getString(cursor, columnName);
        }
    }

}
