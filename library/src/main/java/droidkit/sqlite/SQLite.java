package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLite {

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
                    .find(type.getName() + "$Helper", "insert", SQLiteClient.class, type)
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
                    .find(type.getName() + "$Helper", "update", SQLiteClient.class, type)
                    .invokeStatic(obtainClient(), object);
        } catch (DynamicException e) {
            throw notSQLiteObject(type, e);
        }
        return object;
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
