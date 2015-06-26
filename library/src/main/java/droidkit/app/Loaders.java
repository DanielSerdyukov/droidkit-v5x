package droidkit.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.util.Dynamic;
import droidkit.util.DynamicException;

/**
 * @author Daniel Serdyukov
 */
public final class Loaders {

    private Loaders() {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> android.content.Loader<D> init(@NonNull android.app.LoaderManager lm, int loaderId,
                                                     @Nullable Bundle args, @NonNull Object target) {
        if (target instanceof android.app.LoaderManager.LoaderCallbacks) {
            return lm.initLoader(loaderId, args, (android.app.LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.initLoader(loaderId, args, Dynamic.<android.app.LoaderManager.LoaderCallbacks<D>>init(target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw new IllegalArgumentException("No such found LoaderCallbacks for " + target +
                    ", loaderId=" + loaderId, e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> android.support.v4.content.Loader<D> init(@NonNull android.support.v4.app.LoaderManager lm,
                                                                int loaderId, @Nullable Bundle args,
                                                                @NonNull Object target) {
        if (target instanceof android.support.v4.app.LoaderManager.LoaderCallbacks) {
            return lm.initLoader(loaderId, args, (android.support.v4.app.LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.initLoader(loaderId, args, Dynamic.<android.support.v4.app.LoaderManager.LoaderCallbacks<D>>init(target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw new IllegalArgumentException("No such found LoaderCallbacks for " + target +
                    ", loaderId=" + loaderId, e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> android.content.Loader<D> restart(@NonNull android.app.LoaderManager lm, int loaderId,
                                                        @Nullable Bundle args, @NonNull Object target) {
        if (target instanceof android.app.LoaderManager.LoaderCallbacks) {
            return lm.restartLoader(loaderId, args, (android.app.LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.restartLoader(loaderId, args, Dynamic.<android.app.LoaderManager.LoaderCallbacks<D>>init(target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw new IllegalArgumentException("No such found LoaderCallbacks for " + target +
                    ", loaderId=" + loaderId, e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> android.support.v4.content.Loader<D> restart(@NonNull android.support.v4.app.LoaderManager lm,
                                                                   int loaderId, @Nullable Bundle args,
                                                                   @NonNull Object target) {
        if (target instanceof android.support.v4.app.LoaderManager.LoaderCallbacks) {
            return lm.restartLoader(loaderId, args, (android.support.v4.app.LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.restartLoader(loaderId, args, Dynamic.<android.support.v4.app.LoaderManager.LoaderCallbacks<D>>init(target.getClass().getName() + "$LC", target));
        } catch (DynamicException e) {
            throw new IllegalArgumentException("No such found LoaderCallbacks for " + target +
                    ", loaderId=" + loaderId, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void destroy(@NonNull android.app.LoaderManager lm, int loaderId) {
        lm.destroyLoader(loaderId);
    }

    @SuppressWarnings("unchecked")
    public static void destroy(@NonNull android.support.v4.app.LoaderManager lm, int loaderId) {
        lm.destroyLoader(loaderId);
    }

}
