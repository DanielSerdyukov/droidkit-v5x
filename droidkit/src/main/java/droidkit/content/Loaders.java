package droidkit.content;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 * @see droidkit.app.Loaders
 * @deprecated since 5.0.1 and will be removed in 5.1.1
 */
@Deprecated
public abstract class Loaders {

    private Loaders() {
        //no instance
    }

    @Nullable
    public static <D> Loader<D> init(@NonNull LoaderManager lm, int loaderId, @Nullable Bundle args,
                                     @NonNull Object target) {
        return droidkit.app.Loaders.init(lm, loaderId, args, target);
    }

    @Nullable
    public static <D> Loader<D> restart(@NonNull LoaderManager lm, int loaderId, @Nullable Bundle args,
                                        @NonNull Object target) {
        return droidkit.app.Loaders.restart(lm, loaderId, args, target);
    }

    @SuppressWarnings("unchecked")
    public static void destroy(@NonNull android.app.LoaderManager lm, int loaderId) {
        droidkit.app.Loaders.destroy(lm, loaderId);
    }

}
