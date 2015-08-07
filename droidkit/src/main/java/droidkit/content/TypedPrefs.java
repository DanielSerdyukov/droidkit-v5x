package droidkit.content;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import droidkit.dynamic.ProxyInstance;
import rx.functions.Action3;

/**
 * @author Daniel Serdyukov
 */
public final class TypedPrefs extends KeyValueProxy {

    private static final Map<Class<?>, EditorFacade> EDITORS = new HashMap<>();

    static {
        EDITORS.put(IntValue.class, new IntEditorFacade());
        EDITORS.put(StringValue.class, new StringEditorFacade());
        EDITORS.put(BoolValue.class, new BoolEditorFacade());
        EDITORS.put(LongValue.class, new LongEditorFacade());
        EDITORS.put(FloatValue.class, new FloatEditorFacade());
    }

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
                setupIfAbsent(prefs, editor, method);
            }
        } finally {
            editor.apply();
        }
    }

    public static <T> T from(@NonNull Context context, @NonNull Class<? extends T> type) {
        return from(PreferenceManager.getDefaultSharedPreferences(context), type);
    }

    public static <T> T from(@NonNull SharedPreferences prefs, @NonNull Class<? extends T> type) {
        return ProxyInstance.create(type, new TypedPrefs(prefs));
    }

    private static void setupIfAbsent(@NonNull SharedPreferences prefs, @NonNull SharedPreferences.Editor editor,
                                      @NonNull Method method) {
        final String key = method.getName();
        final Value value = method.getAnnotation(Value.class);
        if (value != null && !prefs.contains(key)) {
            final EditorFacade facade = EDITORS.get(method.getReturnType());
            if (facade != null) {
                facade.call(editor, key, value);
            }
        }
    }

    private interface EditorFacade extends Action3<SharedPreferences.Editor, String, Value> {

    }

    private static final class IntEditorFacade implements EditorFacade {

        @Override
        public void call(SharedPreferences.Editor editor, String key, Value value) {
            editor.putInt(key, value.intValue());
        }

    }

    private static final class StringEditorFacade implements EditorFacade {

        @Override
        public void call(SharedPreferences.Editor editor, String key, Value value) {
            editor.putString(key, value.stringValue());
        }

    }

    private static final class BoolEditorFacade implements EditorFacade {

        @Override
        public void call(SharedPreferences.Editor editor, String key, Value value) {
            editor.putBoolean(key, value.boolValue());
        }

    }

    private static final class LongEditorFacade implements EditorFacade {

        @Override
        public void call(SharedPreferences.Editor editor, String key, Value value) {
            editor.putLong(key, value.longValue());
        }

    }

    private static final class FloatEditorFacade implements EditorFacade {

        @Override
        public void call(SharedPreferences.Editor editor, String key, Value value) {
            editor.putFloat(key, value.floatValue());
        }

    }

}
