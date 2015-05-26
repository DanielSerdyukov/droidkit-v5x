package droidkit.content;

import android.support.annotation.NonNull;

import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
public class StringSetValue extends TypedValue {

    StringSetValue(@NonNull KeyValueDelegate delegate, @NonNull String key) {
        super(delegate, key);
    }

    @NonNull
    public Set<String> get() {
        return getDelegate().getStringSet(getKey());
    }

    public void set(@NonNull Set<String> value) {
        getDelegate().putStringSet(getKey(), value);
    }

}
