package droidkit.sqlite;

import android.content.ContentResolver;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> {

    public static final String WHERE_ID_EQ = BaseColumns._ID + " = ?";

    SQLiteQuery(@NonNull ContentResolver resolver, @NonNull SQLiteClient client, @NonNull Class<T> type) {
    }

}
