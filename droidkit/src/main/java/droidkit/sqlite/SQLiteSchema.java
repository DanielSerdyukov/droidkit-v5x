package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.log.Logger;
import droidkit.util.Lists;
import droidkit.util.Maps;
import rx.functions.Action1;
import rx.functions.Action2;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteSchema {

    private static final String CONTENT = "content";

    private static final AtomicReference<String> AUTHORITY = new AtomicReference<>();

    private static final ConcurrentMap<Class<?>, Uri> URIS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, String> TABLES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, String> SCHEMA = new ConcurrentHashMap<>();

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
        final String table = TABLES.get(type);
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

    static void attachInfo(ProviderInfo info) {
        AUTHORITY.compareAndSet(null, info.authority);
        try {
            Class.forName("droidkit.sqlite.SQLiteMetaData");
        } catch (ClassNotFoundException e) {
            Logger.error(e.getMessage());
        }
    }

    static void createTables(@NonNull Action2<String, String> create) {
        for (final Map.Entry<String, String> entry : SCHEMA.entrySet()) {
            create.call(entry.getKey(), entry.getValue());
        }
    }

    static void dropTables(@NonNull Action1<String> drop) {
        for (final String table : SCHEMA.keySet()) {
            drop.call(table);
        }
    }

    @NonNull
    static String tableOf(@NonNull Uri uri) {
        return Lists.getFirst(uri.getPathSegments());
    }

    @NonNull
    static Uri baseUri(@NonNull Uri uri, @NonNull String table) {
        return uri.buildUpon().path(table).build();
    }

    static void attachTableInfo(@NonNull Class<?> type, String table, String columns) {
        TABLES.put(type, table);
        SCHEMA.put(table, columns);
    }

}
