package droidkit.sqlite;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import droidkit.util.Observer;

/**
 * @author Daniel Serdyukov
 */
class SQLiteLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<List<T>> {

    private final Reference<SQLiteLoader<T>> mLoader;

    private final Reference<Observer<List<T>>> mObserver;

    SQLiteLoaderCallbacks(@NonNull SQLiteLoader<T> loader, @NonNull Observer<List<T>> observer) {
        mLoader = new WeakReference<>(loader);
        mObserver = new WeakReference<>(observer);
    }

    @Override
    public final Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return mLoader.get();
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        final Observer<List<T>> observer = mObserver.get();
        if (observer != null) {
            observer.onChange(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        mLoader.clear();
    }

}
