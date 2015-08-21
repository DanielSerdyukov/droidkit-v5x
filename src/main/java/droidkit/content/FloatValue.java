package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class FloatValue extends TypedValue {

    public static final float EMPTY = 0.0f;

    private final float mDefaultValue;

    FloatValue(@NonNull KeyValueDelegate delegate, @NonNull String key, float defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(float value) {
        getDelegate().putFloat(getKey(), value);
    }

    public float get() {
        return getDelegate().getFloat(getKey(), mDefaultValue);
    }

}
