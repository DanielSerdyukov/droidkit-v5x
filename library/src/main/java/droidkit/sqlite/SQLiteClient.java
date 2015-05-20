package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
interface SQLiteClient {

    void beginTransaction();

    void endTransaction(boolean successful);

    @NonNull
    Cursor rawQuery(@NonNull String sql, String... bindArgs);

    @Nullable
    String simpleQueryForString(@NonNull String sql, String... bindArgs);

    void execSQL(@NonNull String sql, Object... bindArgs);

}
