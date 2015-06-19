package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteDatabase {

    boolean inTransaction();

    void beginTransaction();

    void endTransaction(boolean successful);

    void execSQL(@NonNull String sql, @NonNull Object... bindArgs);

    @NonNull
    Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs);

    @NonNull
    SQLiteStatement compileStatement(@NonNull String sql);

}
