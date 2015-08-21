package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class IntValue extends TypedValue {

    public static final int EMPTY = 0;

    private final int mDefaultValue;

    IntValue(@NonNull KeyValueDelegate delegate, @NonNull String key, int defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(int value) {
        getDelegate().putInt(getKey(), value);
    }

    public int get() {
        return getDelegate().getInt(getKey(), mDefaultValue);
    }

}
