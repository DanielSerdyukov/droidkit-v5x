package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class DoubleValue extends TypedValue {

    public static final double EMPTY = 0.0;

    private final double mDefaultValue;

    DoubleValue(@NonNull KeyValueDelegate delegate, @NonNull String key, double defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(double value) {
        getDelegate().putDouble(getKey(), value);
    }

    public double get() {
        return getDelegate().getDouble(getKey(), mDefaultValue);
    }

}
