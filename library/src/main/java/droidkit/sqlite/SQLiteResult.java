package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.database.DatabaseUtils;
import droidkit.util.Dynamic;
import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 * @see {@link SQLiteQuery#list()}
 * @deprecated since 5.0.1, will be removed in 5.1.1
 */
@Deprecated
// FIXME: 09.07.15 remove deprecation in release 5.1.1
public class SQLiteResult<T> extends AbstractList<T> {

    private static final ConcurrentMap<Class<?>, Method> INSTANTIATE = new ConcurrentHashMap<>();

    private final SQLiteQuery<T> mQuery;

    private final Class<T> mType;

    private final List<T> mObjects;

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
    @SuppressWarnings("unchecked")
    private T instantiate(@NonNull Cursor cursor) {
        try {
            return DynamicMethod.invokeStatic(DynamicMethod.find(INSTANTIATE,
                    Dynamic.forName(mType.getName() + "$SQLite"), "instantiate",
                    SQLiteClient.class, Cursor.class), SQLite.obtainClient(), cursor);
        } catch (DynamicException e) {
            throw new IllegalArgumentException("Check that " + mType + " annotated with @SQLiteObject", e);
        }
    }

}
