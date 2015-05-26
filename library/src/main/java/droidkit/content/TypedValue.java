package droidkit.content;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
abstract class TypedValue {

    private final KeyValueDelegate mDelegate;

    private final String mKey;

    TypedValue(@NonNull KeyValueDelegate delegate, @NonNull String key) {
        mDelegate = delegate;
        mKey = key;
    }

    @NonNull
    public KeyValueDelegate getDelegate() {
        return mDelegate;
    }

    @NonNull
    public String getKey() {
        return mKey;
    }

    public void remove() {
        getDelegate().remove(getKey());
    }

}
