package droidkit.util;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Daniel Serdyukov
 */
public final class Cursors {

    private Cursors() {
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
                return cursor.getDouble(columnIndex) > 0.0;
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

    @NotNull
    public static BigInteger getBigInteger(@NonNull Cursor cursor, @NonNull String columnName) {
        return BigInteger.valueOf(getLong(cursor, columnName));
    }

    @NotNull
    public static BigDecimal getBigDecimal(@NonNull Cursor cursor, @NonNull String columnName) {
        return BigDecimal.valueOf(getDouble(cursor, columnName));
    }

    @NotNull
    public static DateTime getDateTime(@NonNull Cursor cursor, @NonNull String columnName) {
        return new DateTime(getLong(cursor, columnName));
    }


}
