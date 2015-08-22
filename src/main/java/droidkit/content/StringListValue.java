package droidkit.content;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class StringListValue extends TypedValue {

    public static List<String> EMPTY = Collections.emptyList();

    StringListValue(@NonNull KeyValueDelegate delegate, @NonNull String key) {
        super(delegate, key);
    }

    @NonNull
    public List<String> get() {
        return getDelegate().getStringList(getKey());
    }

    public void set(@NonNull List<String> value) {
        getDelegate().putStringList(getKey(), value);
    }

}
