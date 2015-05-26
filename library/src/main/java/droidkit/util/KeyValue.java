package droidkit.util;

import android.support.annotation.Nullable;

import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
public class KeyValue<K, V> implements Map.Entry<K, V> {

    private final K mKey;

    private V mValue;

    public KeyValue(@Nullable K key) {
        this(key, null);
    }

    public KeyValue(@Nullable K key, @Nullable V value) {
        mKey = key;
        mValue = value;
    }

    @Nullable
    @Override
    public K getKey() {
        return mKey;
    }

    @Nullable
    @Override
    public V getValue() {
        return mValue;
    }

    @Nullable
    @Override
    public V setValue(@Nullable V value) {
        final V oldValue = mValue;
        mValue = value;
        return oldValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final KeyValue keyValue = (KeyValue) o;
        return !(mKey != null ? !mKey.equals(keyValue.mKey) : keyValue.mKey != null)
                && !(mValue != null ? !mValue.equals(keyValue.mValue) : keyValue.mValue != null);
    }

    @Override
    public int hashCode() {
        int result = mKey != null ? mKey.hashCode() : 0;
        result = 31 * result + (mValue != null ? mValue.hashCode() : 0);
        return result;
    }

}
