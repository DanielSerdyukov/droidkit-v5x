package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.util.Dynamic;
import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    static final AtomicReference<String> DATABASE_NAME = new AtomicReference<>("data.db");

    static final AtomicInteger DATABASE_VERSION = new AtomicInteger(1);

    static final List<String> PRAGMA = new CopyOnWriteArrayList<>();

    static final List<String> CREATE = new CopyOnWriteArrayList<>();

    static final List<String> UPGRADE = new CopyOnWriteArrayList<>();

    static final Map<Class<?>, String> TABLES = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Uri> URIS = new ConcurrentHashMap<>();

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

    public static void onCreate(@NonNull String... query) {
        Collections.addAll(CREATE, query);
    }

    public static void onUpgrade(@NonNull String... query) {
        Collections.addAll(UPGRADE, query);
    }
    //endregion

    @NonNull
    public static String tableOf(@NonNull Class<?> type) {
        return Objects.requireNonNull(TABLES.get(type), "No such table for " + type);
    }

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
        return new SQLiteQuery<>(mClient, type);
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

    public long simpleQueryForLong(@NonNull String sql, String... bindArgs) {
        return mClient.simpleQueryForLong(sql, bindArgs);
    }

    public void execSQL(@NonNull String sql, Object... bindArgs) {
        mClient.execSQL(sql, bindArgs);
    }

    SQLiteClient getClient() {
        return mClient;
    }

}
