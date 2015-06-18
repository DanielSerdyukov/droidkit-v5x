package droidkit.sqlite;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.util.DynamicException;
import droidkit.util.DynamicField;
import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    private static volatile SQLite sInstance;

    private final ConcurrentMap<Class<?>, Uri> mUriMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<Class<?>, String> mTableMap = new ConcurrentHashMap<>();

    private final Context mContext;

    private final SQLiteClient mClient;

    private final String mAuthority;

    private SQLite(@NonNull Context context, @NonNull SQLiteClient client, @NonNull String authority) {
        mContext = context.getApplicationContext();
        mClient = client;
        mAuthority = authority;
    }

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
    public static String tableOf(@NonNull Class<?> type) {
        return obtainReference().resolveTable(type);
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
        return new SQLiteQuery<>(obtainContext().getContentResolver(), obtainClient(), type);
    }

    @NonNull
    public static <T> T create(@NonNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public static void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        obtainClient().execute(sql, bindArgs);
    }

    @NonNull
    public static Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return obtainClient().query(sql, bindArgs);
    }

    @NonNull
    private static SQLiteClient obtainClient() {
        return obtainReference().mClient;
    }

    @NonNull
    private static Context obtainContext() {
        return obtainReference().mContext;
    }

    @NonNull
    private static SQLite obtainReference() {
        SQLite instance = sInstance;
        if (instance == null) {
            synchronized (SQLite.class) {
                instance = sInstance;
                if (instance == null) {
                    throw new IllegalStateException();
                }
            }
        }
        return instance;
    }

    @NonNull
    private String resolveTable(@NonNull Class<?> type) {
        try {
            String table = mTableMap.get(type);
            if (table == null) {
                final String newTable = DynamicField.getStatic(type, "_TABLE_");
                table = mTableMap.putIfAbsent(type, newTable);
                if (table == null) {
                    table = newTable;
                }
            }
            return Objects.requireNonNull(table, "Something's wrong! This should not have happened.");
        } catch (DynamicException e) {
            throw new IllegalArgumentException("Check that type annotated with @SQLiteObject" + type, e);
        }
    }

    @NonNull
    private Uri resolveUri(@NonNull Class<?> type) {
        Uri uri = mUriMap.get(type);
        if (uri == null) {
            final Uri newUri = new Uri.Builder()
                    .scheme("content")
                    .authority(mAuthority)
                    .path(resolveTable(type))
                    .build();
            uri = mUriMap.putIfAbsent(type, newUri);
            if (uri == null) {
                uri = newUri;
            }
        }
        return uri;
    }

}
