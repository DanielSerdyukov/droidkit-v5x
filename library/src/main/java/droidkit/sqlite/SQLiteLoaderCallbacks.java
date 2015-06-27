package droidkit.sqlite;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observer;


/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<List<T>> {

    private final SQLiteQuery<T> mQuery;

    private final Reference<Observer<List<T>>> mObserverRef;

    public SQLiteLoaderCallbacks(@NonNull SQLiteQuery<T> query, @NonNull Observer<List<T>> observer) {
        mQuery = query;
        mObserverRef = new WeakReference<>(observer);
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        final Observer<List<T>> observer = mObserverRef.get();
        if (observer != null) {
            return new SQLiteLoader<>(SQLite.obtainContext(), mQuery);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        final Observer<List<T>> observer = mObserverRef.get();
        if (observer != null) {
            observer.onNext(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        final Observer<List<T>> observer = mObserverRef.get();
        if (observer != null) {
            observer.onCompleted();
        }
    }

}
