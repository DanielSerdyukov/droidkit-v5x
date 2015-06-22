package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sqlite.database.sqlite.SQLiteOpenHelper;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteOrgClient extends SQLiteClient {

    private final SQLiteOpenHelper mHelper;

    SQLiteOrgClient(@NonNull Context context, @Nullable String databaseName, int databaseVersion,
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

    private static final class SQLiteOpenHelperImpl extends org.sqlite.database.sqlite.SQLiteOpenHelper {

        private final Reference<Callbacks> mCallbacksRef;

        public SQLiteOpenHelperImpl(@NonNull Context context, @Nullable String name, int version,
                                    @NonNull Callbacks callbacks) {
            super(context, name, null, version);
            mCallbacksRef = new WeakReference<>(callbacks);
        }

        @Override
        public void onConfigure(@NonNull org.sqlite.database.sqlite.SQLiteDatabase db) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseConfigure(new SQLiteDatabaseWrapper(db));
            }
        }

        @Override
        public void onCreate(@NonNull org.sqlite.database.sqlite.SQLiteDatabase db) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseCreate(new SQLiteDatabaseWrapper(db));
            }
        }

        @Override
        public void onUpgrade(@NonNull org.sqlite.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
            final Callbacks callbacks = mCallbacksRef.get();
            if (callbacks != null) {
                callbacks.onDatabaseUpgrade(new SQLiteDatabaseWrapper(db), oldVersion, newVersion);
            }
        }

    }

    private static final class SQLiteDatabaseWrapper implements SQLiteDatabase {

        private final Reference<org.sqlite.database.sqlite.SQLiteDatabase> mDb;

        public SQLiteDatabaseWrapper(@NonNull org.sqlite.database.sqlite.SQLiteDatabase db) {
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
            final org.sqlite.database.sqlite.SQLiteDatabase db = obtainDatabase();
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
        private org.sqlite.database.sqlite.SQLiteDatabase obtainDatabase() {
            return Objects.requireNonNull(mDb.get(),
                    "Something's wrong! SQLiteDatabase has been removed by the garbage collector");
        }

    }

    private static final class SQLiteStatementWrapper implements SQLiteStatement {

        private final org.sqlite.database.sqlite.SQLiteStatement mStmt;

        private SQLiteStatementWrapper(@NonNull org.sqlite.database.sqlite.SQLiteStatement stmt) {
            mStmt = stmt;
        }

        @Override
        public void rebind(@NonNull Object... args) {
            mStmt.clearBindings();
            for (int i = 0; i < args.length; ++i) {
                final Object value = args[i];
                final int index = i + 1;
                if (value == null) {
                    mStmt.bindNull(index);
                } else if (value instanceof Double || value instanceof Float) {
                    mStmt.bindDouble(index, ((Number) value).doubleValue());
                } else if (value instanceof Number) {
                    mStmt.bindLong(index, ((Number) value).longValue());
                } else if (value instanceof Boolean) {
                    Boolean bool = (Boolean) value;
                    if (bool) {
                        mStmt.bindLong(index, 1);
                    } else {
                        mStmt.bindLong(index, 0);
                    }
                } else if (value instanceof byte[]) {
                    mStmt.bindBlob(index, (byte[]) value);
                } else {
                    mStmt.bindString(index, value.toString());
                }
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
        public void close() {
            mStmt.close();
        }

    }

}
