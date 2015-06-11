package droidkit.sqlite;

import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public final class SQLiteClientUtils {

    private SQLiteClientUtils() {
    }

    public static int updateColumn(@NonNull SQLiteClient client, @NonNull String table, long rowId,
                                   @NonNull String column, @Nullable Object value) {
        return client.executeUpdateDelete("UPDATE " + table + " SET " + column + " = ? WHERE " +
                BaseColumns._ID + " = ?", (value instanceof Enum) ? (((Enum<?>) value).name()) : value, rowId);
    }

}
