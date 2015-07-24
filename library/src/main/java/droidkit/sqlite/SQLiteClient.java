package droidkit.sqlite;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteClient {

    long executeInsert(@NonNull String sql, @Nullable Object... bindArgs);

    int executeUpdateDelete(@NonNull String sql, @Nullable Object... bindArgs);

}
