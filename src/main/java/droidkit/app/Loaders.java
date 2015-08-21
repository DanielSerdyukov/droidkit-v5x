package droidkit.app;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.dynamic.ConstructorLookup;
import droidkit.dynamic.DynamicException;

/**
 * @author Daniel Serdyukov
 */
public abstract class Loaders {

    private Loaders() {
        //no instance
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <D> Loader<D> init(@NonNull LoaderManager lm, int loaderId, @Nullable Bundle args,
                                     @NonNull Object target) {
        if (target instanceof LoaderManager.LoaderCallbacks) {
            return lm.initLoader(loaderId, args, (LoaderManager.LoaderCallbacks<D>) target);
        }
        try {
            return lm.initLoader(loaderId, args, ConstructorLookup.global()
                    .<LoaderManager.LoaderCallbacks<D>>find(target.getClass().getName() + "$LC", target.getClass())
                    .instantiate(target));
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
            return lm.restartLoader(loaderId, args, ConstructorLookup.global()
                    .<LoaderManager.LoaderCallbacks<D>>find(target.getClass().getName() + "$LC", target.getClass())
                    .instantiate(target));
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

}
