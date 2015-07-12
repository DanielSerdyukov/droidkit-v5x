package droidkit.sqlite;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.util.Dynamic;
import droidkit.util.DynamicException;
import droidkit.util.DynamicField;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    private static final ConcurrentMap<Class<?>, String> TABLES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Method> INSERT = new ConcurrentHashMap<>();

    private static volatile SQLite sInstance;

    private final ConcurrentMap<Class<?>, Uri> mUriMap = new ConcurrentHashMap<>();

    private final Context mContext;

    private final SQLiteClient mClient;

    private final String mAuthority;

    private SQLite(@NonNull Context context, @NonNull SQLiteClient client, @NonNull String authority) {
        mContext = context.getApplicationContext();
        mClient = client;
        mAuthority = authority;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public static String tableOf(@NonNull Class<?> type) {
        try {
            String tableName = TABLES.get(type);
            if (TextUtils.isEmpty(tableName)) {
                final String newTableName = DynamicField.getStatic(
                        Dynamic.forName(type.getName() + "$SQLite"), "TABLE");
                tableName = TABLES.putIfAbsent(type, newTableName);
                if (TextUtils.isEmpty(tableName)) {
                    tableName = newTableName;
                }
            }
            return tableName;
        } catch (DynamicException e) {
            throw new IllegalArgumentException("Check that " + type + " annotated with @SQLiteObject", e);
        }
    }

    @NonNull
    public static Uri uriOf(@NonNull Class<?> type, @NonNull Object... segments) {
        Uri uri = obtainReference().resolveUri(type);
        if (segments.length > 0) {
            final Uri.Builder builder = uri.buildUpon();
            for (final Object segment : segments) {
                builder.appendPath(String.valueOf(segment));
            }
            uri = builder.build();
        }
        return uri;
    }

    public static void beginTransaction() {
        obtainClient().beginTransaction();
    }

    public static void endTransaction(boolean successful) {
        obtainClient().endTransaction(successful);
    }

    @NonNull
    public static <T> SQLiteQuery<T> where(@NonNull Class<T> type) {
        return new SQLiteQuery<>(type);
    }

    @NonNull
    public static <T> T save(@NonNull T object) {
        return save(object, true);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static <T> T save(@NonNull T object, boolean notifyChange) {
        final Class<?> type = object.getClass();
        try {
            DynamicMethod.invokeStatic(DynamicMethod.find(INSERT, Dynamic.forName(type.getName() + "$SQLite"),
                    "insert", SQLiteClient.class, type), obtainClient(), object);
            if (notifyChange) {
                notifyChange(type);
            }
            return object;
        } catch (DynamicException e) {
            throw new IllegalArgumentException("Check that " + type + " annotated with @SQLiteObject", e);
        }
    }

    public static void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        obtainClient().execute(sql, bindArgs);
    }

    @NonNull
    public static Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return obtainClient().query(sql, bindArgs);
    }

    public static void notifyChange(@NonNull Class<?> type) {
        obtainContext().getContentResolver().notifyChange(uriOf(type), null);
    }

    //region internal
    static void initWithClient(@NonNull Context context, @NonNull SQLiteClient client, @NonNull ProviderInfo info) {
        SQLite instance = sInstance;
        if (instance == null) {
            synchronized (SQLite.class) {
                instance = sInstance;
                if (instance == null) {
                    sInstance = new SQLite(context, client, info.authority);
                }
            }
        }
    }

    @NonNull
    static SQLiteClient obtainClient() {
        return obtainReference().mClient;
    }

    @NonNull
    static Context obtainContext() {
        return obtainReference().mContext;
    }

    @NonNull
    static SQLite obtainReference() {
        SQLite instance = sInstance;
        if (instance == null) {
            synchronized (SQLite.class) {
                instance = sInstance;
                if (instance == null) {
                    throw new IllegalStateException("SQLite not initialized yet.");
                }
            }
        }
        return instance;
    }

    static void shutdown() {
        synchronized (SQLite.class) {
            sInstance = null;
        }
    }

    @NonNull
    private Uri resolveUri(@NonNull Class<?> type) {
        Uri uri = mUriMap.get(type);
        if (uri == null) {
            final Uri newUri = new Uri.Builder()
                    .scheme(SQLiteProvider.CONTENT)
                    .authority(mAuthority)
                    .path(tableOf(type))
                    .build();
            uri = mUriMap.putIfAbsent(type, newUri);
            if (uri == null) {
                uri = newUri;
            }
        }
        return uri;
    }
    //endregion

}
