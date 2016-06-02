package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class BoolValue extends TypedValue {

    public static final boolean EMPTY = false;

    private final boolean mDefaultValue;

    BoolValue(@NonNull KeyValueDelegate delegate, @NonNull String key, boolean defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(boolean value) {
        getDelegate().putBoolean(getKey(), value);
    }

    public boolean get() {
        return get(mDefaultValue);
    }

    public boolean get(boolean defaultValue) {
        return getDelegate().getBoolean(getKey(), defaultValue);
    }

    public boolean toggle() {
        final boolean oldValue = get();
        set(!oldValue);
        return oldValue;
    }

}
