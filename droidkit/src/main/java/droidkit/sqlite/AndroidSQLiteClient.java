package droidkit.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public class AndroidSQLiteClient extends SQLiteClient {

    private final SQLiteOpenHelper mHelper;

    public AndroidSQLiteClient(@NonNull Context context, @Nullable String name, int version) {
        mHelper = new SQLiteHelper(context, name, version);
        mHelper.getWritableDatabase(); // ensure database creation
    }

    @Override
    public void close() {
        mHelper.close();
        super.close();
    }

    @NonNull
    @Override
    protected SQLiteDb getReadableDatabase() {
        return new AndroidSQLiteDb(mHelper.getReadableDatabase());
    }

    @NonNull
    @Override
    protected SQLiteDb getWritableDatabase() {
        return new AndroidSQLiteDb(mHelper.getWritableDatabase());
    }

    private class SQLiteHelper extends SQLiteOpenHelper {

        public SQLiteHelper(@NonNull Context context, @Nullable String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            AndroidSQLiteClient.this.onConfigure(new AndroidSQLiteDb(db));
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            AndroidSQLiteClient.this.onCreate(new AndroidSQLiteDb(db));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            AndroidSQLiteClient.this.onUpgrade(new AndroidSQLiteDb(db), oldVersion, newVersion);
        }

    }

}
