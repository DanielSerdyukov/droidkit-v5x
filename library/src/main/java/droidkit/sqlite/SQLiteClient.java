package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteClient {

    void beginTransaction();

    void endTransaction(boolean successful);

    void execute(@NonNull String sql, @NonNull Object... bindArgs);

    long executeInsert(@NonNull String sql, @NonNull Object... bindArgs);

    int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs);

    @NonNull
    Cursor query(@NonNull String sql, @NonNull Object... bindArgs);

    interface Callbacks {

        void onConfigure(@NonNull SQLiteDatabaseWrapper db);

        void onCreate(@NonNull SQLiteDatabaseWrapper db);

        void onUpgrade(@NonNull SQLiteDatabaseWrapper db, int oldVersion, int newVersion);

    }

}
