package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.database.CursorUtils;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class SQLiteResult<T> extends AbstractList<T> {

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
            return makeEntryFromCursor(mQuery, cursor);
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
            mQuery.getClient().executeUpdateDelete("DELETE FROM " + mQuery.getTableName() + " WHERE _id = ?",
                    CursorUtils.getLong(oldCursor, BaseColumns._ID));
            IOUtils.closeQuietly(oldCursor);
            final Cursor cursor = mQuery.cursor();
            mCursorRef.compareAndSet(oldCursor, cursor);
            mSize = cursor.getCount();
            return null;
        }
        throw new ConcurrentModificationException();
    }

    @NonNull
    T makeEntryFromCursor(@NonNull SQLiteQuery<T> query, @NonNull Cursor cursor) {
        return query.instantiate(cursor);
    }

    @NonNull
    AtomicReference<Cursor> getCursorReference() {
        return mCursorRef;
    }

}
