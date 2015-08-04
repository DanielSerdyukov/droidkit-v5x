package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class StringValue extends TypedValue {

    public static final String EMPTY = "";

    private final String mDefaultValue;

    StringValue(@NonNull KeyValueDelegate delegate, @NonNull String key, @NonNull String defaultValue) {
        super(delegate, key);
        mDefaultValue = defaultValue;
    }

    public void set(@NonNull String value) {
        getDelegate().putString(getKey(), value);
    }

    @NonNull
    public String get() {
        return getDelegate().getString(getKey(), mDefaultValue);
    }

}
