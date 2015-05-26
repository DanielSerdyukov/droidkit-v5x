package droidkit.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.lang.reflect.Method;

import droidkit.util.Dynamic;

/**
 * @author Daniel Serdyukov
 */
public final class TypedPrefs extends KeyValueProxy {

    private TypedPrefs(@NonNull SharedPreferences prefs) {
        super(new PreferenceDelegate(prefs));
    }

    public static <T> void setupDefaults(@NonNull Context context, @NonNull Class<? extends T> type) {
        setupDefaults(PreferenceManager.getDefaultSharedPreferences(context), type);
    }

    public static <T> void setupDefaults(@NonNull SharedPreferences prefs, @NonNull Class<? extends T> type) {
        final Method[] methods = type.getDeclaredMethods();
        final SharedPreferences.Editor editor = prefs.edit();
        try {
            for (final Method method : methods) {
                final Value value = method.getAnnotation(Value.class);
                final String key = method.getName();
                if (value != null && !prefs.contains(key)) {
                    final Class<?> returnType = method.getReturnType();
                    if (IntValue.class.isAssignableFrom(returnType)) {
                        editor.putInt(key, value.intValue());
                    } else if (StringValue.class.isAssignableFrom(returnType)) {
                        editor.putString(key, value.stringValue());
                    } else if (BoolValue.class.isAssignableFrom(returnType)) {
                        editor.putBoolean(key, value.boolValue());
                    } else if (LongValue.class.isAssignableFrom(returnType)) {
                        editor.putLong(key, value.longValue());
                    } else if (FloatValue.class.isAssignableFrom(returnType)) {
                        editor.putFloat(key, value.floatValue());
                    }
                }
            }
        } finally {
            editor.apply();
        }
    }

    public static <T> T from(@NonNull Context context, @NonNull Class<? extends T> type) {
        return from(PreferenceManager.getDefaultSharedPreferences(context), type);
    }

    public static <T> T from(@NonNull SharedPreferences prefs, @NonNull Class<? extends T> type) {
        return Dynamic.newProxy(new TypedPrefs(prefs), type);
    }

}
