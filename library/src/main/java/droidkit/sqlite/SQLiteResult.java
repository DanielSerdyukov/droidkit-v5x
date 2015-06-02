package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

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
class SQLiteResult<T> extends AbstractList<T> {

    public static final String WHERE_ID = " WHERE _id = ?";

    private static final ConcurrentMap<Class<?>, Method> CREATE_METHODS = new ConcurrentHashMap<>();

    private final SQLiteQuery<T> mQuery;

    private final AtomicReference<Cursor> mCursorRef = new AtomicReference<>();

    private int mSize;

    SQLiteResult(@NonNull SQLiteQuery<T> query, @NonNull Cursor cursor) {
        mQuery = query;
        mCursorRef.compareAndSet(null, cursor);
        mSize = cursor.getCount();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int location) {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null && !cursor.isClosed() && cursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(cursor, BaseColumns._ID);
            final SQLiteCache<T> cache = SQLiteCache.of(mQuery.getType());
            T entry = cache.get(rowId);
            if (entry == null) {
                entry = instantiate(mQuery, cursor);
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
    public T remove(int location) {
        final Cursor oldCursor = mCursorRef.get();
        if (oldCursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(oldCursor, BaseColumns._ID);
            mQuery.getClient().executeUpdateDelete("DELETE FROM " + mQuery.getTableName() + WHERE_ID, rowId);
            IOUtils.closeQuietly(oldCursor);
            final Cursor cursor = mQuery.cursor();
            mCursorRef.compareAndSet(oldCursor, cursor);
            mSize = cursor.getCount();
            return SQLiteCache.of(mQuery.getType()).remove(rowId);
        }
        throw new ConcurrentModificationException();
    }

    @NonNull
    AtomicReference<Cursor> getCursorReference() {
        return mCursorRef;
    }

    @NonNull
    T instantiate(@NonNull SQLiteQuery<T> query, @NonNull Cursor cursor) {
        try {
            final Method method = DynamicMethod.find(CREATE_METHODS, query.getType(), "create",
                    SQLiteClient.class, Cursor.class);
            //noinspection ConstantConditions
            return DynamicMethod.invokeStatic(method, query.getClient(), cursor);
        } catch (DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
