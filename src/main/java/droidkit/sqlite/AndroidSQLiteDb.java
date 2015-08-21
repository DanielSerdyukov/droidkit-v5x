package droidkit.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class AndroidSQLiteDb implements SQLiteDb {

    private final SQLiteDatabase mDb;

    AndroidSQLiteDb(@NonNull SQLiteDatabase db) {
        mDb = db;
    }

    @Override
    public void setForeignKeyConstraintsEnabled(boolean enable) {
        mDb.setForeignKeyConstraintsEnabled(enable);
    }

    @NonNull
    @Override
    public Cursor query(@NonNull String sql, @Nullable String... bindArgs) {
        return mDb.rawQuery(sql, bindArgs);
    }

    @Override
    public void beginTransactionNonExclusive() {
        mDb.beginTransactionNonExclusive();
    }

    @Override
    public void setTransactionSuccessful() {
        mDb.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        mDb.endTransaction();
    }

    @Override
    public boolean inTransaction() {
        return mDb.inTransaction();
    }

    @Override
    public SQLiteStmt compileStatement(@NonNull String sql) {
        return new AndroidSQLiteStmt(mDb.compileStatement(sql));
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(mDb);
    }

}
