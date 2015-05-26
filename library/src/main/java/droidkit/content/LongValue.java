package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class LongValue extends TypedValue {

    public static final long EMPTY = 0L;

    private final long mDefaultValue;

    LongValue(@NonNull KeyValueDelegate delegate, @NonNull String key, long defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(long value) {
        getDelegate().putLong(getKey(), value);
    }

    public long get() {
        return getDelegate().getLong(getKey(), mDefaultValue);
    }

}
