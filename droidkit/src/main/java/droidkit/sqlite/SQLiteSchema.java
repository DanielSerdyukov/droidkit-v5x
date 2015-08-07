package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.log.Logger;
import droidkit.util.Lists;
import droidkit.util.Maps;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteSchema {

    private static final String CONTENT = "content";

    private static final AtomicReference<String> AUTHORITY = new AtomicReference<>();

    private static final ConcurrentMap<Class<?>, Uri> URIS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, String> TABLE_RESOLUTION = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, String> TABLES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, List<String>> INDICES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Action2<ContentResolver, Uri>> NOTIFICATION_BEHAVIORS;

    private static final Action2<ContentResolver, Uri> DEFAULT_NOTIFICATION_BEHAVIOR;

    static {
        NOTIFICATION_BEHAVIORS = new ConcurrentHashMap<>();
        DEFAULT_NOTIFICATION_BEHAVIOR = new Action2<ContentResolver, Uri>() {
            @Override
            public void call(@NonNull ContentResolver resolver, Uri uri) {
                resolver.notifyChange(uri, null);
            }
        };
    }

    private SQLiteSchema() {
    }

    @NonNull
    public static String resolveTable(@NonNull Class<?> type) {
        final String table = TABLE_RESOLUTION.get(type);
        if (table == null) {
            throw new SQLiteException("No such table for %s", type.getName());
        }
        return table;
    }

    @NonNull
    public static Uri resolveUri(@NonNull Class<?> type) {
        Uri uri = URIS.get(type);
        if (uri == null) {
            final Uri newUri = new Uri.Builder()
                    .scheme(CONTENT)
                    .authority(AUTHORITY.get())
                    .path(resolveTable(type))
                    .build();
            uri = URIS.putIfAbsent(type, newUri);
            if (uri == null) {
                uri = newUri;
            }
        }
        return uri;
    }

    public static void notifyChange(@NonNull Class<?> type) {
        Maps.getNonNull(NOTIFICATION_BEHAVIORS, type, DEFAULT_NOTIFICATION_BEHAVIOR)
                .call(SQLite.obtainResolver(), SQLiteSchema.resolveUri(type));
    }

    public static void createTables(@NonNull Action2<String, String> func) {
        for (final Map.Entry<String, String> table : TABLES.entrySet()) {
            func.call(table.getKey(), table.getValue());
        }
    }

    public static void createIndices(@NonNull Action2<String, String> func) {
        for (final Map.Entry<String, List<String>> table : INDICES.entrySet()) {
            for (final String column : table.getValue()) {
                func.call(table.getKey(), column);
            }
        }
    }

    public static void dropTables(@NonNull Action1<String> func, Func1<String, Boolean> criteria) {
        for (final String table : TABLE_RESOLUTION.values()) {
            if (criteria.call(table)) {
                func.call(table);
            }
        }
    }

    static void attachInfo(ProviderInfo info) {
        AUTHORITY.compareAndSet(null, info.authority);
        try {
            Class.forName("droidkit.sqlite.SQLiteMetaData");
        } catch (ClassNotFoundException e) {
            Logger.error(e.getMessage());
        }
    }

    @NonNull
    static String columnsOf(String table) {
        return TABLES.get(table);
    }

    @NonNull
    static List<String> indicesOf(String table) {
        return INDICES.get(table);
    }

    @NonNull
    static String tableOf(@NonNull Uri uri) {
        return Lists.getFirst(uri.getPathSegments());
    }

    @NonNull
    static Uri baseUri(@NonNull Uri uri, @NonNull String table) {
        return uri.buildUpon().path(table).build();
    }

    @Keep
    static void attachTableInfo(@NonNull Class<?> type, @NonNull String table, @NonNull String columns) {
        TABLE_RESOLUTION.putIfAbsent(type, table);
        TABLES.putIfAbsent(table, columns);
    }

    @Keep
    static void attachIndicesInfo(@NonNull String table, @NonNull List<String> indices) {
        INDICES.putIfAbsent(table, indices);
    }

}
