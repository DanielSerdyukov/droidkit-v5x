package droidkit.sqlite;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;

import rx.Subscriber;


/**
 * @author Daniel Serdyukov
 */
class SQLiteLoaderCallbacks<T> implements LoaderManager.LoaderCallbacks<List<T>> {

    private final Loader<List<T>> mLoader;

    private final Subscriber<? super List<T>> mSubscriber;

    public SQLiteLoaderCallbacks(@NonNull Subscriber<? super List<T>> subscriber, @NonNull Loader<List<T>> loader) {
        mSubscriber = subscriber;
        mLoader = loader;
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> data) {
        mSubscriber.onNext(data);
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        mSubscriber.onCompleted();
    }

}
