package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
class AndroidSQLiteClient extends SQLiteOpenHelper implements SQLiteClient {

    private final Reference<Callbacks> mCallbacksRef;

    AndroidSQLiteClient(@NonNull Context context, @Nullable String name, int version, @NonNull Callbacks callbacks) {
        super(context, name, null, version);
        mCallbacksRef = new WeakReference<>(callbacks);
        getWritableDatabase();
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void endTransaction(boolean successful) {

    }

    @Override
    public void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        getWritableDatabase().execSQL(sql, bindArgs);
    }

    @Override
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public long executeInsert(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.executeInsert();
        } finally {
            stmt.close();
        }
    }

    @Override
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                DatabaseUtils.bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.executeUpdateDelete();
        } finally {
            stmt.close();
        }
    }

    @NonNull
    @Override
    public Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return getReadableDatabase().rawQuery(sql, Arrays.copyOf(bindArgs, bindArgs.length, String[].class));
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        final Callbacks callbacks = mCallbacksRef.get();
        if (callbacks != null) {
            callbacks.onConfigure(new AndroidSQLiteDatabaseWrapper(db));
        }
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        final Callbacks callbacks = mCallbacksRef.get();
        if (callbacks != null) {
            callbacks.onCreate(new AndroidSQLiteDatabaseWrapper(db));
        }
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        final Callbacks callbacks = mCallbacksRef.get();
        if (callbacks != null) {
            callbacks.onUpgrade(new AndroidSQLiteDatabaseWrapper(db), oldVersion, newVersion);
        }
    }

}
