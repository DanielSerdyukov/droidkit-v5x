package droidkit.sqlite;

import android.support.annotation.NonNull;
import android.util.LruCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteCache<T> extends LruCache<Long, T> {

    private static final AtomicInteger MAX_SIZE = new AtomicInteger(1024);

    private static final ConcurrentMap<Class<?>, LruCache<Long, ?>> CACHE_MAP = new ConcurrentHashMap<>();

    private SQLiteCache() {
        super(MAX_SIZE.get());
    }

    public static void setMaxSize(int size) {
        MAX_SIZE.compareAndSet(MAX_SIZE.get(), size);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> SQLiteCache<T> of(@NonNull Class<T> type) {
        SQLiteCache<T> cache = (SQLiteCache<T>) CACHE_MAP.get(type);
        if (cache == null) {
            final SQLiteCache<T> newCache = new SQLiteCache<>();
            cache = (SQLiteCache<T>) CACHE_MAP.putIfAbsent(type, newCache);
            if (cache == null) {
                cache = newCache;
            }
        }
        return cache;
    }

}
