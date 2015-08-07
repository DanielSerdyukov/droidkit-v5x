package droidkit.sqlite;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoader<T> extends AsyncTaskLoader<List<T>> {

    private final SQLiteQuery<T> mQuery;

    private ContentObserver mObserver;

    private List<T> mResult;

    SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query, @NonNull Class<T> type) {
        super(context);
        mQuery = query;
        observeOn(SQLiteSchema.resolveUri(type));
    }

    @UiThread
    public final void observeOn(@NonNull Uri uri) {
        registerContentObserver(uri);
    }

    @Override
    public List<T> loadInBackground() {
        return mQuery.list();
    }

    @Override
    public void deliverResult(List<T> result) {
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
