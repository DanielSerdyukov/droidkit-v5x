package droidkit.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
class AndroidSQLiteDatabaseWrapper implements SQLiteDatabaseWrapper {

    private final SQLiteDatabase mDb;

    AndroidSQLiteDatabaseWrapper(SQLiteDatabase db) {
        mDb = db;
    }

    @Override
    public void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        mDb.execSQL(sql, bindArgs);
    }

    @NonNull
    @Override
    public Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return mDb.rawQuery(sql, Arrays.copyOf(bindArgs, bindArgs.length, String[].class));
    }

}
