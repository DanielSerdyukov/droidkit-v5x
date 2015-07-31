package droidkit.sqlite;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;
import droidkit.io.IOUtils;
import droidkit.os.MemoryGuard;
import droidkit.util.Cursors;
import rx.functions.Action1;

/**
 * @author Daniel Serdyukov
 * @deprecated since 5.0.1, will be removed in 5.1.1
 */
@Deprecated
public class SQLiteResult<T> extends AbstractList<T> {

    private static final Action1<CursorWrapper> CLOSE_GUARD = new Action1<CursorWrapper>() {
        @Override
        public void call(CursorWrapper wrapper) {
            IOUtils.closeQuietly(wrapper);
        }
    };

    private final SQLiteQuery<T> mQuery;

    private final Class<T> mType;

    private final List<T> mObjects;

    private CursorWrapper mWrapper;

    SQLiteResult(@NonNull SQLiteQuery<T> query, @NonNull Cursor initialCursor, @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
        mWrapper = new CursorWrapper(initialCursor);
        mObjects = new ArrayList<>(Collections.nCopies(mWrapper.getCount(), (T) null));
        MemoryGuard.watch(this, mWrapper, CLOSE_GUARD);
    }

    @Override
    public T get(int location) {
        if (mWrapper.moveToPosition(location)) {
            T entry = mObjects.get(location);
            if (entry == null) {
                entry = instantiate(mWrapper);
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
        final Cursor oldCursor = mWrapper.getWrappedCursor();
        try {
            mWrapper = new CursorWrapper(mQuery.cursor());
        } finally {
            IOUtils.closeQuietly(oldCursor);
        }
    }

    @Override
    public T remove(int location) {
        final Cursor oldCursor = mWrapper.getWrappedCursor();
        try {
            if (oldCursor.moveToPosition(location)) {
                final T entry = mObjects.remove(location);
                final long rowId = Cursors.getLong(oldCursor, BaseColumns._ID);
                SQLite.where(mType).equalTo(BaseColumns._ID, rowId).remove();
                mWrapper = new CursorWrapper(mQuery.cursor());
                return entry;
            }
            throw new ArrayIndexOutOfBoundsException(location);
        } finally {
            IOUtils.closeQuietly(oldCursor);
        }
    }

    @Override
    public int size() {
        if (mWrapper.isClosed()) {
            return 0;
        }
        return mWrapper.getCount();
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private T instantiate(@NonNull Cursor cursor) {
        try {
            return MethodLookup.global()
                    .find(mType.getName() + "$Helper", "instantiate", Cursor.class)
                    .invokeStatic(cursor);
        } catch (DynamicException e) {
            throw new SQLiteException("Can't instantiate object", e);
        }
    }

}
