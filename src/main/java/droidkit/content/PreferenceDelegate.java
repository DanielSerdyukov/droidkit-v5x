package droidkit.content;

import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
class PreferenceDelegate implements KeyValueDelegate {

    private final SharedPreferences mPrefs;

    PreferenceDelegate(@NonNull SharedPreferences prefs) {
        mPrefs = prefs;
    }

    @Override
    public int getInt(@NonNull String key, int defaultValue) {
        return mPrefs.getInt(key, defaultValue);
    }

    @Override
    public void putInt(@NonNull String key, int value) {
        mPrefs.edit().putInt(key, value).apply();
    }

    @NonNull
    @Override
    public String getString(@NonNull String key, @NonNull String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    @Override
    public void putString(@NonNull String key, @NonNull String value) {
        mPrefs.edit().putString(key, value).apply();
    }

    @Override
    public boolean getBoolean(@NonNull String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    @Override
    public void putBoolean(@NonNull String key, boolean value) {
        mPrefs.edit().putBoolean(key, value).apply();
    }

    @Override
    public long getLong(@NonNull String key, long defaultValue) {
        return mPrefs.getLong(key, defaultValue);
    }

    @Override
    public void putLong(@NonNull String key, long value) {
        mPrefs.edit().putLong(key, value).apply();
    }

    @Override
    public double getDouble(@NonNull String key, double defaultValue) {
        throw new IllegalArgumentException("Unsupported type 'double'");
    }

    @Override
    public void putDouble(@NonNull String key, double value) {
        throw new IllegalArgumentException("Unsupported type 'double'");
    }

    @Override
    public float getFloat(@NonNull String key, float defaultValue) {
        return mPrefs.getFloat(key, defaultValue);
    }

    @Override
    public void putFloat(@NonNull String key, float value) {
        mPrefs.edit().putFloat(key, value).apply();
    }

    @NonNull
    @Override
    public Set<String> getStringSet(@NonNull String key) {
        return mPrefs.getStringSet(key, Collections.<String>emptySet());
    }

    @Override
    public void putStringSet(@NonNull String key, @NonNull Set<String> value) {
        mPrefs.edit().putStringSet(key, value).apply();
    }

    @NonNull
    @Override
    public List<String> getStringList(@NonNull String key) {
        throw new IllegalArgumentException("Unsupported type 'List<String>'");
    }

    @Override
    public void putStringList(@NonNull String key, @NonNull List<String> value) {
        throw new IllegalArgumentException("Unsupported type 'List<String>'");
    }

    @Override
    public <T extends Parcelable> T getParcelable(@NonNull String key) {
        throw new IllegalArgumentException("Unsupported type 'Parcelable'");
    }

    @Override
    public <T extends Parcelable> void putParcelable(@NonNull String key, @NonNull T value) {
        throw new IllegalArgumentException("Unsupported type 'Parcelable'");
    }

    @Override
    public void remove(String key) {
        mPrefs.edit().remove(key).apply();
    }

    @Override
    public void clear() {
        mPrefs.edit().clear().apply();
    }

}
