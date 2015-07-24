package droidkit.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.functions.Action1;
import rx.functions.Action2;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteClientAndroid extends SQLiteOpenHelper implements SQLiteClient {

    public SQLiteClientAndroid(@NonNull Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(@NonNull final SQLiteDatabase db) {
        SQLiteSchema.createTables(new Action2<String, String>() {
            @Override
            public void call(@NonNull String table, @NonNull String columns) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + table + "(" + columns + ");");
            }
        });
    }

    @Override
    public void onUpgrade(@NonNull final SQLiteDatabase db, int oldVersion, int newVersion) {
        SQLiteSchema.dropTables(new Action1<String>() {
            @Override
            public void call(@NonNull String table) {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + table + ";");
            }
        });
        onCreate(db);
    }

    @Override
    public long executeInsert(@NonNull String sql, @Nullable Object... bindArgs) {
        return 0;
    }

    @Override
    public int executeUpdateDelete(@NonNull String sql, @Nullable Object... bindArgs) {
        return 0;
    }

}
