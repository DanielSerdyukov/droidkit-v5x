package droidkit.view;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * @author Daniel Serdyukov
 */
public abstract class ViewInjector {

    private ViewInjector() {
        //no instance
    }

    public static void inject(@NonNull Activity activity, @NonNull Object target) {

    }

    public static void inject(@NonNull Dialog dialog, @NonNull Object target) {

    }

    public static void inject(@NonNull View view, @NonNull Object target) {
        
    }

}
