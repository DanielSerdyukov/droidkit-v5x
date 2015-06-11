package droidkit.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.database.DatabaseUtils;
import droidkit.io.IOUtils;
import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 */
class SQLiteResult<T> extends AbstractList<T> implements Closeable {

    private static final ConcurrentMap<Class<?>, Method> CREATE_METHODS = new ConcurrentHashMap<>();

    private final AtomicReference<Cursor> mCursorRef = new AtomicReference<>();

    private final Reference<Context> mContext;

    private final Reference<SQLiteClient> mClient;

    private final Class<T> mType;

    private final SQLiteQuery<T> mQuery;

    private final boolean mNotifyOnChange;

    private int mSize;

    SQLiteResult(@NonNull Context context, @NonNull SQLiteClient client, @NonNull Cursor cursor,
                 @NonNull Class<T> type, @NonNull SQLiteQuery<T> query, boolean notifyOnChange) {
        mContext = new WeakReference<>(context);
        mClient = new WeakReference<>(client);
        mCursorRef.compareAndSet(null, cursor);
        mType = type;
        mQuery = query;
        mNotifyOnChange = notifyOnChange;
        mSize = cursor.getCount();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int location) {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null && !cursor.isClosed() && cursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(cursor, BaseColumns._ID);
            final SQLiteCache<T> cache = SQLiteCache.of(mType);
            T entry = cache.get(rowId);
            if (entry == null) {
                entry = instantiate(cursor);
                cache.put(rowId, entry);
            }
            return entry;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public void add(int location, T object) {
        final Cursor oldCursor = mCursorRef.get();
        try {
            SQLite.of(mContext.get()).save(object, mNotifyOnChange);
            final Cursor cursor = mQuery.cursor();
            mCursorRef.compareAndSet(oldCursor, cursor);
            mSize = cursor.getCount();
        } finally {
            IOUtils.closeQuietly(oldCursor);
        }
    }

    @Override
    public T remove(int location) {
        final Cursor oldCursor = mCursorRef.get();
        if (oldCursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(oldCursor, BaseColumns._ID);
            mQuery.rebuild().equalTo(BaseColumns._ID, rowId).remove();
            IOUtils.closeQuietly(oldCursor);
            final Cursor cursor = mQuery.cursor();
            mCursorRef.compareAndSet(oldCursor, cursor);
            mSize = cursor.getCount();
            return SQLiteCache.of(mQuery.getType()).remove(rowId);
        }
        throw new ConcurrentModificationException();
    }

    @Override
    public void close() {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null) {
            IOUtils.closeQuietly(cursor);
            mSize = 0;
        }
    }

    @NonNull
    AtomicReference<Cursor> getCursorReference() {
        return mCursorRef;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private T instantiate(@NonNull Cursor cursor) {
        try {
            final Method method = DynamicMethod.find(CREATE_METHODS, mType, "_create",
                    SQLiteClient.class, Cursor.class);
            return DynamicMethod.invokeStatic(method, mClient.get(), cursor);
        } catch (DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
