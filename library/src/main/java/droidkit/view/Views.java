package droidkit.view;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author Daniel Serdyukov
 */
public final class Views {

    private Views() {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(@NonNull View root, @IdRes int viewId) {
        return (T) root.findViewById(viewId);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(@NonNull Activity root, @IdRes int viewId) {
        return (T) root.findViewById(viewId);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends View> T findById(@NonNull Dialog root, @IdRes int viewId) {
        return (T) root.findViewById(viewId);
    }

}
