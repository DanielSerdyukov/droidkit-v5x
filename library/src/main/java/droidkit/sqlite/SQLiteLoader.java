package droidkit.sqlite;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoader<T> extends AsyncTaskLoader<List<T>> {

    private final SQLiteQuery<T> mQuery;

    private ContentObserver mObserver;

    private List<T> mResult;

    public SQLiteLoader(@NonNull Context context, @NonNull Class<T> type) {
        this(context, SQLite.where(type));
    }

    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query) {
        this(context, query, query.getUri(), true);
    }

    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query, @Nullable Uri observeOn) {
        this(context, query, observeOn, observeOn != null);
    }

    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query, @Nullable Uri observeOn,
                        boolean notifyForDescendants) {
        super(context);
        mQuery = query;
        if (observeOn != null) {
            mObserver = new ForceLoadContentObserver();
            getContext().getContentResolver().registerContentObserver(observeOn, notifyForDescendants, mObserver);
        }
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
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
        }
        mResult = null;
        mObserver = null;
    }

}
