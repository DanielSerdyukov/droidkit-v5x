package droidkit.sqlite;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoader<T> extends AsyncTaskLoader<SQLiteResult<T>> {

    private final SQLiteQuery<T> mQuery;

    private ContentObserver mObserver;

    private SQLiteResult<T> mResult;

    public SQLiteLoader(@NonNull Context context, @NonNull Class<T> type) {
        this(context, SQLite.where(type));
    }

    /**
     * @see SQLiteQuery#loader()
     * @deprecated since 5.0.1, will be removed in 5.1.1
     */
    @Deprecated
    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query) {
        super(context);
        mQuery = query;
        observeOn(SQLiteSchema.resolveUri(mQuery.getType()));
    }

    @UiThread
    public final void observeOn(@NonNull Uri uri) {
        registerContentObserver(uri);
    }

    @Override
    public SQLiteResult<T> loadInBackground() {
        return mQuery.all();
    }

    @Override
    public void deliverResult(SQLiteResult<T> result) {
        if (isReset()) {
            return;
        }
        mResult = result;
        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        unregisterContentObserver();
        mResult = null;
    }

    private void registerContentObserver(@NonNull Uri uri) {
        mObserver = new ForceLoadContentObserver();
        getContext().getContentResolver().registerContentObserver(uri, true, mObserver);
    }

    private void unregisterContentObserver() {
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

}
