package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Closeable;

import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class CursorAnchor implements Closeable {

    private Cursor mCursor;

    CursorAnchor(@NonNull Cursor cursor) {
        mCursor = cursor;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    void anchor(@NonNull Cursor cursor) {
        if (mCursor != null) {
            IOUtils.closeQuietly(mCursor);
        }
        mCursor = cursor;
    }

    public int getCount() {
        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public void close() {
        if (mCursor != null) {
            IOUtils.closeQuietly(mCursor);
        }
    }

    public boolean moveToPosition(int location) {
        return mCursor != null && !mCursor.isClosed() && mCursor.moveToPosition(location);
    }

}
