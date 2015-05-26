package droidkit.content;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
class BundleDelegate implements KeyValueDelegate {

    private final Bundle mBundle;

    BundleDelegate(@NonNull Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public int getInt(@NonNull String key, int defaultValue) {
        return mBundle.getInt(key, defaultValue);
    }

    @Override
    public void putInt(@NonNull String key, int value) {
        mBundle.putInt(key, value);
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return mBundle.getBoolean(key, defaultValue);
    }

    @Override
    public void putBoolean(@NonNull String key, boolean value) {
        mBundle.putBoolean(key, value);
    }

    @Override
    public long getLong(@NonNull String key, long defaultValue) {
        return mBundle.getLong(key, defaultValue);
    }

    @Override
    public void putLong(@NonNull String key, long value) {
        mBundle.putLong(key, value);
    }

    @NonNull
    @Override
    public String getString(@NonNull String key, @NonNull String defaultValue) {
        return mBundle.getString(key, defaultValue);
    }

    @Override
    public void putString(@NonNull String key, @NonNull String value) {
        mBundle.putString(key, value);
    }

    @Override
    public double getDouble(@NonNull String key, double defaultValue) {
        return mBundle.getDouble(key, defaultValue);
    }

    @Override
    public void putDouble(@NonNull String key, double value) {
        mBundle.putDouble(key, value);
    }

    @Override
    public float getFloat(@NonNull String key, float defaultValue) {
        return mBundle.getFloat(key, defaultValue);
    }

    @Override
    public void putFloat(@NonNull String key, float value) {
        mBundle.putFloat(key, value);
    }

    @NonNull
    @Override
    public Set<String> getStringSet(@NonNull String key) {
        throw new IllegalArgumentException("Unsupported type 'Set<String>'");
    }

    @Override
    public void putStringSet(@NonNull String key, @NonNull Set<String> value) {
        throw new IllegalArgumentException("Unsupported type 'Set<String>'");
    }

    @NonNull
    @Override
    public List<String> getStringList(@NonNull String key) {
        List<String> list = mBundle.getStringArrayList(key);
        if (list == null) {
            list = Collections.emptyList();
        }
        return list;
    }

    @Override
    public void putStringList(@NonNull String key, @NonNull List<String> value) {
        mBundle.putStringArrayList(key, new ArrayList<>(value));
    }

    @Override
    public <T extends Parcelable> T getParcelable(@NonNull String key) {
        return mBundle.getParcelable(key);
    }

    @Override
    public <T extends Parcelable> void putParcelable(@NonNull String key, @NonNull T value) {
        mBundle.putParcelable(key, value);
    }

    @Override
    public void remove(String key) {
        mBundle.remove(key);
    }

    @Override
    public void clear() {
        mBundle.clear();
    }

}
