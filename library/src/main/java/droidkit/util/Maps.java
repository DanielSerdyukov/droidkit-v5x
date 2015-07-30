package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
public abstract class Maps {

    private Maps() {
        //no instance
    }

    @Nullable
    public static <K, V> V getNonNull(@NonNull Map<K, V> map, @Nullable K key, @Nullable V nullValue) {
        final V value = map.get(key);
        if (value == null) {
            return nullValue;
        }
        return value;
    }

}
