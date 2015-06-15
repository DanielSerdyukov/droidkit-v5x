package droidkit.sqlite;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import droidkit.concurrent.MainQueue;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoader<T> extends AsyncTaskLoader<List<T>> {

    private final ContentObserver mObserver =
            new ContentObserver(MainQueue.getHandler()) {
                @Override
                public boolean deliverSelfNotifications() {
                    return true;
                }

                @Override
                public void onChange(boolean selfChange) {
                    onContentChanged();
                }
            };

    private final SQLiteQuery<T> mQuery;

    private Uri mObserveOn;

    private boolean mNotifyForDescendants;

    private SQLiteResult<T> mResult;

    public SQLiteLoader(@NonNull Context context, @NonNull Class<T> type) {
        this(context, SQLite.of(context).where(type));
    }

    public SQLiteLoader(@NonNull Context context, @NonNull SQLiteQuery<T> query) {
        super(context);
        mQuery = query;
        observeOn(SQLite.uriOf(query.getType()));
        notifyForDescendants(true);
    }

    @NonNull
    public final SQLiteLoader<T> observeOn(@Nullable Uri observeOn) {
        mObserveOn = observeOn;
        return this;
    }

    @NonNull
    public final SQLiteLoader<T> notifyForDescendants(boolean notifyForDescendants) {
        mNotifyForDescendants = notifyForDescendants;
        return this;
    }

    @Override
    public List<T> loadInBackground() {
        final SQLiteResult<T> result = mQuery.result();
        if (mObserveOn != null) {
            getContext().getContentResolver()
                    .registerContentObserver(mObserveOn, mNotifyForDescendants, mObserver);
        }
        return result;
    }

    @Override
    public void deliverResult(List<T> result) {
        if (isReset()) {
            if (result instanceof SQLiteResult) {
                IOUtils.closeQuietly((SQLiteResult) result);
            }
            return;
        }
        final SQLiteResult<T> oldResult = mResult;
        mResult = (SQLiteResult<T>) result;
        if (isStarted()) {
            super.deliverResult(result);
        }
        if (oldResult != null && oldResult != result) {
            IOUtils.closeQuietly((SQLiteResult) oldResult);
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
        getContext().getContentResolver().unregisterContentObserver(mObserver);
        if (mResult != null) {
            IOUtils.closeQuietly(mResult);
        }
        mResult = null;
    }

}
