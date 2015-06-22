package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.database.DatabaseUtils;
import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 */
class SQLiteResult<T> extends AbstractList<T> {

    private static final ConcurrentMap<Class<?>, Method> CREATE = new ConcurrentHashMap<>();

    private final SQLiteQuery<T> mQuery;

    private final Class<T> mType;

    private final ArrayList<T> mObjects;

    private final AtomicReference<Cursor> mCursorRef = new AtomicReference<>();

    SQLiteResult(@NonNull SQLiteQuery<T> query, @NonNull Class<T> type, @NonNull Cursor cursor) {
        mQuery = query;
        mType = type;
        mCursorRef.compareAndSet(null, cursor);
        mObjects = new ArrayList<>(Collections.nCopies(cursor.getCount(), (T) null));
        SQLiteGuard.get(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int location) {
        final Cursor cursor = mCursorRef.get();
        if (cursor.moveToPosition(location)) {
            T entry = mObjects.get(location);
            if (entry == null) {
                entry = instantiate(cursor);
                mObjects.set(location, entry);
            }
            return entry;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    @Override
    public void add(int location, T object) {
        SQLite.save(object);
        mObjects.add(location, object);
        final Cursor oldCursor = mCursorRef.get();
        mCursorRef.compareAndSet(oldCursor, mQuery.cursor());
        oldCursor.close();
    }

    @Override
    public T remove(int location) {
        final Cursor oldCursor = mCursorRef.get();
        if (oldCursor.moveToPosition(location)) {
            final long rowId = DatabaseUtils.getLong(oldCursor, BaseColumns._ID);
            final T entry = mObjects.remove(location);
            SQLite.where(mType).equalTo(BaseColumns._ID, rowId).remove();
            mCursorRef.compareAndSet(oldCursor, mQuery.cursor());
            oldCursor.close();
            return entry;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    @Override
    public int size() {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null && !cursor.isClosed()) {
            return cursor.getCount();
        }
        return 0;
    }

    @NonNull
    AtomicReference<Cursor> getCursorReference() {
        return mCursorRef;
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
