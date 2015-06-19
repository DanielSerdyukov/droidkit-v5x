package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.database.DatabaseUtils;
import droidkit.io.IOUtils;
import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 */
class SQLiteResult<T> extends AbstractList<T> {

    private static final ConcurrentMap<Class<?>, Method> CREATE = new ConcurrentHashMap<>();

    private final SQLiteQuery<T> mQuery;

    private final Class<T> mType;

    private Cursor mCursor;

    SQLiteResult(@NonNull SQLiteQuery<T> query, @NonNull Class<T> type, @NonNull Cursor cursor) {
        mQuery = query;
        mType = type;
        mCursor = cursor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int location) {
        if (mCursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(mCursor, BaseColumns._ID);
            T entry = SQLiteCache.get(mType, rowId);
            if (entry == null) {
                entry = instantiate(mCursor);
                SQLiteCache.put(mType, rowId, entry);
            }
            return entry;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    @Override
    public void add(int location, T object) {
        final Cursor oldCursor = mCursor;
        try {
            SQLite.save(object);
            mCursor = mQuery.cursor();
        } finally {
            IOUtils.closeQuietly(oldCursor);
        }
    }

    @Override
    public T remove(int location) {
        final Cursor oldCursor = mCursor;
        try {
            if (oldCursor.moveToPosition(location)) {
                final long rowId = DatabaseUtils.getLong(oldCursor, BaseColumns._ID);
                SQLite.where(mType).equalTo(BaseColumns._ID, rowId).remove();
                mCursor = mQuery.cursor();
                return SQLiteCache.remove(mType, rowId);
            }
            throw new ArrayIndexOutOfBoundsException(location);
        } finally {
            IOUtils.closeQuietly(oldCursor);
        }
    }

    @Override
    public int size() {
        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    protected void finalize() throws Throwable {
        IOUtils.closeQuietly(mCursor);
        super.finalize();
    }

    @Nullable
    private T instantiate(@NonNull Cursor cursor) {
        try {
            Method create = CREATE.get(mType);
            if (create == null) {
                final Method method = mType.getDeclaredMethod("_create", SQLiteClient.class, Cursor.class);
                create = CREATE.putIfAbsent(mType, method);
                if (create == null) {
                    create = method;
                }
            }
            return DynamicMethod.invokeStatic(create, SQLite.obtainClient(), cursor);
        } catch (NoSuchMethodException | DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
