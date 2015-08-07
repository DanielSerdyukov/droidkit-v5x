package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
public final class SQLite {

    private static final String SQLITE_HELPER = "$SQLiteHelper";

    private static volatile Reference<Context> sContextRef;

    private static volatile Reference<SQLiteClient> sClientRef;

    private static volatile Reference<ContentResolver> sResolverRef;

    private SQLite() {
        //no instance
    }

    public static void beginTransaction() {
        obtainClient().beginTransaction();
    }

    public static void endTransaction() {
        obtainClient().endTransaction();
    }

    public static void rollbackTransaction() {
        obtainClient().rollbackTransaction();
    }

    @NonNull
    public static <T> SQLiteQuery<T> where(@NonNull Class<T> type) {
        return new SQLiteQuery<>(type);
    }

    @NonNull
    public static <T> T save(@NonNull T object) {
        final Class<?> type = object.getClass();
        try {
            MethodLookup.global()
                    .find(type.getName() + SQLITE_HELPER, "insert", SQLiteClient.class, type)
                    .invokeStatic(obtainClient(), object);
        } catch (DynamicException e) {
            throw notSQLiteObject(type, e);
        }
        return object;
    }

    @NonNull
    public static <T> T update(@NonNull T object) {
        final Class<?> type = object.getClass();
        try {
            MethodLookup.global()
                    .find(type.getName() + SQLITE_HELPER, "update", SQLiteClient.class, type)
                    .invokeStatic(obtainClient(), object);
        } catch (DynamicException e) {
            throw notSQLiteObject(type, e);
        }
        return object;
    }

    @NonNull
    public static <T> T remove(@NonNull T object) {
        final Class<?> type = object.getClass();
        try {
            MethodLookup.global()
                    .find(type.getName() + SQLITE_HELPER, "remove", SQLiteClient.class, type)
                    .invokeStatic(obtainClient(), object);
        } catch (DynamicException e) {
            throw notSQLiteObject(type, e);
        }
        return object;
    }

    public static <T> int clear(@NonNull Class<T> type) {
        return clear(SQLiteSchema.resolveTable(type));
    }

    public static int clear(@NonNull String table) {
        return obtainClient().executeUpdateDelete("DELETE FROM " + table + ";");
    }

    public static void clearAll() {
        final SQLiteClient client = obtainClient();
        final Cursor cursor = client.query("SELECT name FROM sqlite_master" +
                " WHERE type='table'" +
                " AND name <> 'android_metadata'");
        final List<String> tables = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                do {
                    tables.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
        client.beginTransaction();
        try {
            for (final String table : tables) {
                client.executeUpdateDelete("DELETE FROM " + table + ";");
            }
        } finally {
            client.endTransaction();
        }
    }

    public static void notifyChange(@NonNull Class<?> type) {
        obtainResolver().notifyChange(SQLiteSchema.resolveUri(type), null);
    }

    static void attach(@NonNull SQLiteClient client, @NonNull Context context) {
        synchronized (SQLite.class) {
            sClientRef = new WeakReference<>(client);
            sContextRef = new WeakReference<>(context.getApplicationContext());
            sResolverRef = new WeakReference<>(context.getContentResolver());
        }
    }

    @NonNull
    static Context obtainContext() {
        final Context context = sContextRef.get();
        if (context == null) {
            throw notAttachedYet();
        }
        return context;
    }

    @NonNull
    static SQLiteClient obtainClient() {
        final SQLiteClient client = sClientRef.get();
        if (client == null) {
            throw notAttachedYet();
        }
        return client;
    }

    @NonNull
    static ContentResolver obtainResolver() {
        final ContentResolver resolver = sResolverRef.get();
        if (resolver == null) {
            throw notAttachedYet();
        }
        return resolver;
    }

    @NonNull
    private static RuntimeException notAttachedYet() {
        throw new IllegalStateException("SQLite not attached yet, check that SQLiteProvider" +
                " registered in AndroidManifest.xml");
    }

    @NonNull
    private static RuntimeException notSQLiteObject(@NonNull Class<?> type, @NonNull Throwable e) {
        throw new IllegalArgumentException(type + " is not sqlite object, check that class" +
                " annotated with @SQLiteObject", e);
    }

}
