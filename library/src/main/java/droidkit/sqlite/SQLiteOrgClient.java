package droidkit.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;
import org.sqlite.database.sqlite.SQLiteProgram;
import org.sqlite.database.sqlite.SQLiteStatement;

import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class SQLiteOrgClient extends SQLiteOpenHelper implements SQLiteClient {

    private final SQLiteDbInfo mDbInfo;

    public SQLiteOrgClient(@NonNull Context context, @NonNull SQLiteDbInfo dbInfo) {
        super(context, dbInfo.getName(), null, dbInfo.getVersion());
        mDbInfo = dbInfo;
    }

    private static void bindObjectToProgram(SQLiteProgram prog, int index, Object value) {
        if (value == null) {
            prog.bindNull(index);
        } else if (value instanceof Double || value instanceof Float) {
            prog.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof Number) {
            prog.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            if (bool) {
                prog.bindLong(index, 1);
            } else {
                prog.bindLong(index, 0);
            }
        } else if (value instanceof byte[]) {
            prog.bindBlob(index, (byte[]) value);
        } else {
            prog.bindString(index, value.toString());
        }
    }

    @Override
    public void beginTransaction() {
        final SQLiteDatabase db = getWritableDatabase();
        if (!db.inTransaction()) {
            db.beginTransactionNonExclusive();
        }
    }

    @Override
    public void endTransaction(boolean successful) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db.inTransaction()) {
            if (successful) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
        }
    }

    @NonNull
    @Override
    public Cursor rawQuery(@NonNull String sql, @NonNull String... bindArgs) {
        return getReadableDatabase().rawQuery(sql, bindArgs);
    }

    @Nullable
    @Override
    public String simpleQueryForString(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.simpleQueryForString();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    @Override
    public void execSQL(@NonNull String sql, @NonNull Object... bindArgs) {
        getWritableDatabase().execSQL(sql, bindArgs);
    }

    @Override
    public int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteStatement stmt = getWritableDatabase().compileStatement(sql);
        try {
            for (int i = 0; i < bindArgs.length; ++i) {
                bindObjectToProgram(stmt, i + 1, bindArgs[i]);
            }
            return stmt.executeUpdateDelete();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    @NonNull
    @Override
    public Cursor query(@NonNull String table, @Nullable String[] columns, @Nullable String where,
                        @Nullable String[] bindArgs, @Nullable String orderBy) {
        return getReadableDatabase().query(table, columns, where, bindArgs, null, null, orderBy);
    }

    @Override
    public long insert(@NonNull String table, @NonNull ContentValues values) {
        return getWritableDatabase().insert(table, BaseColumns._ID, values);
    }

    @Override
    public int delete(@NonNull String table, @Nullable String where, @Nullable String[] bindArgs) {
        return getWritableDatabase().delete(table, where, bindArgs);
    }

    @Override
    public int update(@NonNull String table, @NonNull ContentValues values, @Nullable String where,
                      @Nullable String[] bindArgs) {
        return getWritableDatabase().update(table, values, where, bindArgs);
    }

    @Override
    public long insertRowId(@NonNull String table) {
        final SQLiteStatement stmt = getWritableDatabase()
                .compileStatement("INSERT INTO " + table + "(_id) VALUES(null);");
        try {
            return stmt.executeInsert();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    //region SQLiteOpenHelper implementation
    @Override
    public void onConfigure(@NonNull SQLiteDatabase db) {
        for (final String pragma : mDbInfo.getPragma()) {
            db.execSQL(pragma);
        }
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        for (final String create : SQLite.CREATE) {
            db.execSQL(create);
        }
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        if (SQLite.UPGRADE.isEmpty()) {
            final Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master" +
                    " WHERE type='table'" +
                    " AND name <> 'android_metadata'", null);
            try {
                if (cursor.moveToFirst()) {
                    do {
                        db.execSQL("DROP TABLE IF EXISTS " + cursor.getString(0) + ";");
                    } while (cursor.moveToNext());
                }
            } finally {
                IOUtils.closeQuietly(cursor);
            }
            onCreate(db);
        } else {
            for (final String upgrade : SQLite.UPGRADE) {
                db.execSQL(upgrade);
            }
        }
    }
    //endregion

}
