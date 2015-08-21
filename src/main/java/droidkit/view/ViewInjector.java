package droidkit.view;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;

/**
 * @author Daniel Serdyukov
 */
public abstract class ViewInjector {

    private static final String INJECTOR = "$ViewInjector";

    private static final String INJECT = "inject";

    private ViewInjector() {
        //no instance
    }

    public static void inject(@NonNull Activity activity, @NonNull Object target) {
        final Class<?> type = target.getClass();
        try {
            MethodLookup.global().find(type.getName() + INJECTOR, INJECT, Activity.class, type)
                    .invokeStatic(activity, target);
        } catch (DynamicException e) {
            Log.e(ViewInjector.class.getName(), e.getMessage(), e);
        }
    }

    public static void inject(@NonNull Dialog dialog, @NonNull Object target) {
        final Class<?> type = target.getClass();
        try {
            MethodLookup.global().find(type.getName() + INJECTOR, INJECT, Dialog.class, type)
                    .invokeStatic(dialog, target);
        } catch (DynamicException e) {
            Log.e(ViewInjector.class.getName(), e.getMessage(), e);
        }
    }

    public static void inject(@NonNull View view, @NonNull Object target) {
        final Class<?> type = target.getClass();
        try {
            MethodLookup.global().find(type.getName() + INJECTOR, INJECT, View.class, type)
                    .invokeStatic(view, target);
        } catch (DynamicException e) {
            Log.e(ViewInjector.class.getName(), e.getMessage(), e);
        }
    }

}
