package droidkit.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
interface SQLiteClient {

    @NonNull
    Context getContext();

    void beginTransaction();

    void endTransaction(boolean successful);

    @NonNull
    Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs);

    @Nullable
    String simpleQueryForString(@NonNull String sql, @NonNull Object... bindArgs);

    long simpleQueryForLong(@NonNull String sql, @NonNull Object... bindArgs);

    void execSQL(@NonNull String sql, @NonNull Object... bindArgs);

    int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs);

    @NonNull
    Cursor query(@NonNull String table, @Nullable String[] columns, @Nullable String where,
                 @Nullable String[] bindArgs, @Nullable String orderBy);

    long insert(@NonNull String table, @NonNull ContentValues values);

    int delete(@NonNull String table, @Nullable String where, @Nullable String[] bindArgs);

    int update(@NonNull String table, @NonNull ContentValues values, @Nullable String where,
               @Nullable String[] bindArgs);

}
