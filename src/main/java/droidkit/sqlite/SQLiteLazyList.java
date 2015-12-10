package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.os.AutoClose;
import droidkit.os.Finalizer;
import droidkit.util.Cursors;
import rx.functions.Action1;

/**
 * @author Daniel Serdyukov
 */
class SQLiteLazyList<T> extends AbstractList<T> {

    private static final Action1<CursorAnchor> AUTO_CLOSE = new AutoClose<>();

    private final SQLiteRawQuery mQuery;

    private final Class<T> mType;

    private final List<T> mObjects;

    private CursorAnchor mAnchor;

    SQLiteLazyList(@NonNull SQLiteRawQuery query, @NonNull Cursor initialCursor, @NonNull Class<T> type) {
        mQuery = query;
        mType = type;
        mAnchor = new CursorAnchor(initialCursor);
        mObjects = new ArrayList<>(Collections.nCopies(mAnchor.getCount(), (T) null));
        Finalizer.create(this, mAnchor, AUTO_CLOSE);
    }

    @Override
    public T get(int location) {
        if (mAnchor.moveToPosition(location)) {
            T entry = mObjects.get(location);
            if (entry == null) {
                entry = instantiate(mAnchor.getCursor());
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
        mAnchor.anchor(mQuery.cursor());
    }

    @Override
    public T remove(int location) {
        final Cursor oldCursor = mAnchor.getCursor();
        if (oldCursor.moveToPosition(location)) {
            final T entry = mObjects.remove(location);
            final long rowId = Cursors.getLong(oldCursor, BaseColumns._ID);
            SQLite.where(mType).equalTo(BaseColumns._ID, rowId).clear();
            mAnchor.anchor(mQuery.cursor());
            return entry;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }

    @Override
    public int size() {
        return mAnchor.getCount();
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private T instantiate(@NonNull Cursor cursor) {
        return SQLiteList.unpackOne(cursor, mType);
    }

}
