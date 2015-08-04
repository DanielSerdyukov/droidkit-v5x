package droidkit.content;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public class ParcelableValue extends TypedValue {

    ParcelableValue(@NonNull KeyValueDelegate delegate, @NonNull String key) {
        super(delegate, key);
    }

    public <T extends Parcelable> T get() {
        return getDelegate().getParcelable(getKey());
    }

    public <T extends Parcelable> void set(T value) {
        getDelegate().putParcelable(getKey(), value);
    }

}
