package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.util.Dynamic;
import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;
import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    static final List<String> CREATE = new CopyOnWriteArrayList<>();

    static final List<String> UPGRADE = new CopyOnWriteArrayList<>();

    static final ConcurrentMap<Class<?>, String> TABLES = new ConcurrentHashMap<>();

    private static final AtomicReference<String> AUTHORITY = new AtomicReference<>();

    private static final ConcurrentMap<Class<?>, Uri> URIS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Method> CREATE_METHODS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Method> SAVE_METHODS = new ConcurrentHashMap<>();

    private static volatile SQLite sInstance;

    static {
        try {
            Class.forName("droidkit.sqlite.SQLite$Gen");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Schema class not found, try to rebuild project", e);
        }
    }

    private final Context mContext;

    private final SQLiteClient mClient;

    private SQLite(@NonNull Context context) {
        mContext = context.getApplicationContext();
        if (Dynamic.inClasspath("org.sqlite.database.sqlite.SQLiteDatabase")) {
            mClient = new SQLiteOrgClient(mContext, SQLiteDbInfo.from(context));
        } else {
            mClient = new AndroidSQLiteClient(mContext, SQLiteDbInfo.from(context));
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

    public static void attach(@NonNull String authority) {
        AUTHORITY.compareAndSet(null, authority);
    }

    @NonNull
    public static String tableOf(@NonNull Class<?> type) {
        return Objects.requireNonNull(TABLES.get(type), "No such table for " + type);
    }

    @NonNull
    public static Uri uriOf(@NonNull Class<?> type) {
        Uri uri = URIS.get(type);
        if (uri == null) {
            final String authority = AUTHORITY.get();
            if (TextUtils.isEmpty(authority)) {
                throw new IllegalStateException("SQLite not attached to authority");
            }
            final Uri newUri = new Uri.Builder()
                    .scheme("content")
                    .authority(authority)
                    .path(tableOf(type))
                    .build();
            uri = URIS.putIfAbsent(type, newUri);
            if (uri == null) {
                uri = newUri;
            }
        }
        return uri;
    }

    public void beginTransaction() {
        mClient.beginTransaction();
    }

    public void endTransaction(boolean successful) {
        mClient.endTransaction(successful);
    }

    @NonNull
    public <T> SQLiteQuery<T> where(@NonNull Class<T> type) {
        return new SQLiteQuery<>(mContext, mClient, type);
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public <T> T create(@NonNull Class<T> type, boolean notifyChange) {
        try {
            final T object = DynamicMethod.invokeStatic(DynamicMethod
                    .find(CREATE_METHODS, type, "_create", SQLiteClient.class), mClient);
            if (notifyChange) {
                mContext.getContentResolver().notifyChange(uriOf(type), null, false);
            }
            return object;
        } catch (DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public <T> void save(@NonNull T object, boolean notifyChange) {
        try {
            final Class<?> type = object.getClass();
            DynamicMethod.invokeStatic(DynamicMethod
                    .find(SAVE_METHODS, type, "_save", SQLiteClient.class, type), mClient, object);
            if (notifyChange) {
                mContext.getContentResolver().notifyChange(uriOf(type), null, false);
            }
        } catch (DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void execSQL(@NonNull String sql, Object... bindArgs) {
        mClient.execSQL(sql, bindArgs);
    }

    @NonNull
    public Cursor rawQuery(@NonNull String sql, String... bindArgs) {
        return mClient.rawQuery(sql, bindArgs);
    }

    SQLiteClient getClient() {
        return mClient;
    }

}
