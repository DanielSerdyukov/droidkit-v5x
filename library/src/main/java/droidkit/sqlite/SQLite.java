package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.util.Dynamic;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    static final AtomicReference<String> DATABASE_NAME = new AtomicReference<>("data.db");

    static final AtomicInteger DATABASE_VERSION = new AtomicInteger(1);

    static final CopyOnWriteArrayList<String> PRAGMA = new CopyOnWriteArrayList<>();

    static final CopyOnWriteArrayList<String> CREATE = new CopyOnWriteArrayList<>();

    static final CopyOnWriteArrayList<String> UPGRADE = new CopyOnWriteArrayList<>();

    private static final long AUTO_ID = -1;

    private static volatile SQLite sInstance;

    static {
        try {
            Class.forName("droidkit.sqlite.SQLite$Gen");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("droidkit.sqlite.SQLite$Gen not found, try to rebuild project", e);
        }
    }

    private final SQLiteClient mClient;

    private SQLite(@NonNull Context context) {
        if (Dynamic.inClasspath("org.sqlite.database.sqlite.SQLiteDatabase")) {
            throw new IllegalArgumentException("org.sqlite.database.* not supported yet");
        } else {
            mClient = new AndroidSQLiteClient(context.getApplicationContext());
        }
    }

    @NonNull
    public static SQLite of(@NonNull Context context) {
        SQLite instance = sInstance;
        if (instance == null) {
            synchronized (SQLite.class) {
                instance = sInstance;
                if (instance == null) {
                    instance = sInstance = new SQLite(context);
                }
            }
        }
        return instance;
    }

    //region Database Config
    public static void useInMemoryDb() {
        DATABASE_NAME.compareAndSet(DATABASE_NAME.get(), null);
    }

    public static void pragma(@NonNull String... pragma) {
        Collections.addAll(PRAGMA, pragma);
    }

    public static void createTable(@NonNull String name, @NonNull String... columnDef) {
        CREATE.add("CREATE TABLE IF NOT EXISTS " + name + "(" + TextUtils.join(", ", columnDef) + ");");
    }

    public static void alterTable(@NonNull String... query) {
        Collections.addAll(UPGRADE, query);
    }
    //endregion

    static void attach(@NonNull String authority) {

    }

    public void beginTransaction() {
        mClient.beginTransaction();
    }

    public void endTransaction(boolean successful) {
        mClient.endTransaction(successful);
    }

    @NonNull
    public <T> SQLiteQuery<T> where(@NonNull Class<T> type) {
        return new SQLiteQuery<>();
    }

    @NonNull
    public <T> T create(@NonNull Class<T> type) {
        return createWithId(type, AUTO_ID);
    }

    @NonNull
    public <T> T createWithId(@NonNull Class<T> type, long id) {
        throw new UnsupportedOperationException();
    }

    public <T> void save(@NonNull T object) {
        saveWithId(object, AUTO_ID);
    }

    public <T> void saveWithId(@NonNull T object, long id) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public Cursor rawQuery(@NonNull String sql, String... bindArgs) {
        return mClient.rawQuery(sql, bindArgs);
    }

    @Nullable
    public String simpleQueryForString(@NonNull String sql, String... bindArgs) {
        return mClient.simpleQueryForString(sql, bindArgs);
    }

    public void execSQL(@NonNull String sql, Object... bindArgs) {
        mClient.execSQL(sql, bindArgs);
    }

}
