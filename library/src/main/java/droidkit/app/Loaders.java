package droidkit.app;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.util.Dynamic;
import droidkit.util.DynamicException;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;

/**
 * @author Daniel Serdyukov
 */
public final class Loaders {

    private Loaders() {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> Loader<D> init(@NonNull LoaderManager lm, int loaderId, @Nullable Bundle args,
                                     @NonNull Object target) {
        if (target instanceof LoaderManager.LoaderCallbacks) {
            return lm.initLoader(loaderId, args, (LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.initLoader(loaderId, args, Dynamic.<LoaderManager.LoaderCallbacks<D>>init(
                    target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw noSuchLoaderCallbacks(target, loaderId, e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> Loader<D> restart(@NonNull LoaderManager lm, int loaderId, @Nullable Bundle args,
                                        @NonNull Object target) {
        if (target instanceof LoaderManager.LoaderCallbacks) {
            return lm.restartLoader(loaderId, args, (LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.restartLoader(loaderId, args, Dynamic.<LoaderManager.LoaderCallbacks<D>>init(
                    target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw noSuchLoaderCallbacks(target, loaderId, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void destroy(@NonNull android.app.LoaderManager lm, int loaderId) {
        lm.destroyLoader(loaderId);
    }

    @NonNull
    private static IllegalArgumentException noSuchLoaderCallbacks(@NonNull Object target, int loaderId,
                                                                  @NonNull Exception wrapped) {
        return new IllegalArgumentException("No such found LoaderCallbacks for " + target +
                ", loaderId=" + loaderId, wrapped);
    }

    public static <T> LoaderManager.LoaderCallbacks<T> callbacks(@NonNull final Func0<Loader<T>> onCreate,
                                                                 @NonNull final Action1<T> onLoad,
                                                                 @NonNull final Action0 onReset) {
        return new LoaderManager.LoaderCallbacks<T>() {
            @Override
            public Loader<T> onCreateLoader(int id, Bundle args) {
                return onCreate.call();
            }

            @Override
            public void onLoadFinished(Loader<T> loader, T data) {
                onLoad.call(data);
            }

            @Override
            public void onLoaderReset(Loader<T> loader) {
                onReset.call();
            }
        };
    }

    public static <T> LoaderManager.LoaderCallbacks<T> callbacks(@NonNull final Func0<Loader<T>> onCreate,
                                                                 @NonNull final Action1<T> onLoad) {
        return new LoaderManager.LoaderCallbacks<T>() {
            @Override
            public Loader<T> onCreateLoader(int id, Bundle args) {
                return onCreate.call();
            }

            @Override
            public void onLoadFinished(Loader<T> loader, T data) {
                onLoad.call(data);
            }

            @Override
            public void onLoaderReset(Loader<T> loader) {

            }
        };
    }

}
