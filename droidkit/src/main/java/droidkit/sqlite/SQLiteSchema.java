package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;
import droidkit.log.Logger;
import droidkit.util.Lists;
import droidkit.util.Maps;
import rx.functions.Action2;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteSchema {

    private static final String CONTENT = "content";

    private static final AtomicReference<String> AUTHORITY = new AtomicReference<>();

    private static final ConcurrentMap<Class<?>, Uri> URIS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, String> RESOLUTIONS = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Class<?>> HELPERS = new ConcurrentHashMap<>();

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
        final String table = RESOLUTIONS.get(type);
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

    public static void createTables(@NonNull SQLiteDb db, @NonNull Func1<String, Boolean> criteria) {
        final MethodLookup methodLookup = MethodLookup.local();
        for (final Map.Entry<String, Class<?>> entry : HELPERS.entrySet()) {
            try {
                if (criteria.call(entry.getKey())) {
                    methodLookup.find(entry.getValue(), "createTable", SQLiteDb.class).invokeStatic(db);
                    methodLookup.find(entry.getValue(), "createIndices", SQLiteDb.class).invokeStatic(db);
                    methodLookup.find(entry.getValue(), "createRelationTables", SQLiteDb.class).invokeStatic(db);
                    methodLookup.find(entry.getValue(), "createTriggers", SQLiteDb.class).invokeStatic(db);
                }
            } catch (DynamicException e) {
                throw SQLite.notSQLiteObject(entry.getValue(), e);
            }
        }
    }

    public static void dropTables(@NonNull SQLiteDb db, @NonNull Func1<String, Boolean> criteria) {
        final MethodLookup methodLookup = MethodLookup.local();
        for (final Map.Entry<String, Class<?>> entry : HELPERS.entrySet()) {
            try {
                if (criteria.call(entry.getKey())) {
                    methodLookup.find(entry.getValue(), "dropTable", SQLiteDb.class).invokeStatic(db);
                    methodLookup.find(entry.getValue(), "dropRelationTables", SQLiteDb.class).invokeStatic(db);
                }
            } catch (DynamicException e) {
                throw SQLite.notSQLiteObject(entry.getValue(), e);
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
    static String tableOf(@NonNull Uri uri) {
        return Lists.getFirst(uri.getPathSegments());
    }

    @NonNull
    static Uri baseUri(@NonNull Uri uri, @NonNull String table) {
        return uri.buildUpon().path(table).build();
    }

    @Keep
    static void attachTableInfo(@NonNull Class<?> type, @NonNull String table, @NonNull Class<?> helper) {
        RESOLUTIONS.putIfAbsent(type, table);
        HELPERS.putIfAbsent(table, helper);
    }

}
