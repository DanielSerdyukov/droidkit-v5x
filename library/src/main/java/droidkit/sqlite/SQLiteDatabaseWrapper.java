package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteDatabaseWrapper {

    void execute(@NonNull String sql, @NonNull Object... bindArgs);

    @NonNull
    Cursor query(@NonNull String sql, @NonNull Object... bindArgs);

}
