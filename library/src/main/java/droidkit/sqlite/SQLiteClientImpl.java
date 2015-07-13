package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
class SQLiteClientImpl extends SQLiteClient {

    private final SQLiteOpenHelper mHelper;

    SQLiteClientImpl(@NonNull Context context, @Nullable String databaseName, int databaseVersion,
                     @NonNull Callbacks callbacks) {
        mHelper = new SQLiteOpenHelperImpl(context, databaseName, databaseVersion, callbacks);
        getWritableDatabase();
    }

    @Override
    protected SQLiteDatabase getReadableDatabase() {
        return new SQLiteDatabaseWrapper(mHelper.getReadableDatabase());
    }

    @Override
    protected SQLiteDatabase getWritableDatabase() {
        return new SQLiteDatabaseWrapper(mHelper.getWritableDatabase());
    }

    @Override
    protected void shutdown() {
        mHelper.close();
    }

    private static final class SQLiteOpenHelperImpl extends SQLiteOpenHelper {

        private final Reference<Callbacks> mCallbacksRef;

        public SQLiteOpenHelperImpl(@NonNull Context context, @Nullable String name, int version,
                                    @NonNull Callbacks callbacks) {
            super(context, name, null, version);
            mCallbacksRef = new WeakReference<>(callbacks);
        }

        @Override
        public void onConfigure(@NonNull android.database.sqlite.SQLiteDatabase db) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseConfigure(new SQLiteDatabaseWrapper(db));
            }
        }

        @Override
        public void onCreate(@NonNull android.database.sqlite.SQLiteDatabase db) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseCreate(new SQLiteDatabaseWrapper(db));
            }
        }

        @Override
        public void onUpgrade(@NonNull android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseUpgrade(new SQLiteDatabaseWrapper(db), oldVersion, newVersion);
            }
        }

    }

    private static final class SQLiteDatabaseWrapper implements SQLiteDatabase {

        private final Reference<android.database.sqlite.SQLiteDatabase> mDb;

        public SQLiteDatabaseWrapper(@NonNull android.database.sqlite.SQLiteDatabase db) {
            mDb = new WeakReference<>(db);
        }

        @Override
        public boolean inTransaction() {
            return obtainDatabase().inTransaction();
        }

        @Override
        public void beginTransaction() {
            obtainDatabase().beginTransactionNonExclusive();
        }

        @Override
        public void endTransaction(boolean successful) {
            final android.database.sqlite.SQLiteDatabase db = obtainDatabase();
            if (successful) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
        }

        @Override
        public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
            obtainDatabase().execSQL(sql, bindArgs);
        }

        @NonNull
        @Override
        public Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs) {
            return obtainDatabase().rawQuery(sql, bindArgs);
        }

        @NonNull
        @Override
        public SQLiteStatement compileStatement(@NonNull String sql) {
            return new SQLiteStatementWrapper(obtainDatabase().compileStatement(sql));
        }

        @NonNull
        private android.database.sqlite.SQLiteDatabase obtainDatabase() {
            return Objects.requireNonNull(mDb.get(),
                    "Something's wrong! SQLiteDatabase has been removed by the garbage collector");
        }

    }

    private static final class SQLiteStatementWrapper implements SQLiteStatement {

        private final android.database.sqlite.SQLiteStatement mStmt;

        private SQLiteStatementWrapper(@NonNull android.database.sqlite.SQLiteStatement stmt) {
            mStmt = stmt;
        }

        @Override
        public void rebind(@NonNull Object... args) {
            mStmt.clearBindings();
            for (int i = 0; i < args.length; ++i) {
                bindObjectToStatement(this, i + 1, args[i]);
            }
        }

        @Override
        public void execute() {
            mStmt.execute();
        }

        @Override
        public long executeInsert() {
            return mStmt.executeInsert();
        }

        @Override
        public int executeUpdateDelete() {
            return mStmt.executeUpdateDelete();
        }

        @Override
        public String simpleQueryForString() {
            return mStmt.simpleQueryForString();
        }

        @Override
        public void bindNull(int index) {
            mStmt.bindNull(index);
        }

        @Override
        public void bindDouble(int index, double value) {
            mStmt.bindDouble(index, value);
        }

        @Override
        public void bindLong(int index, long value) {
            mStmt.bindLong(index, value);
        }

        @Override
        public void bindBlob(int index, @NonNull byte[] value) {
            mStmt.bindBlob(index, value);
        }

        @Override
        public void bindString(int index, @NonNull String value) {
            mStmt.bindString(index, value);
        }

        @Override
        public void close() {
            mStmt.close();
        }

    }

}
