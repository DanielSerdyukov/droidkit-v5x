package droidkit.content;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Set;

/**
 * @author Daniel Serdyukov
 */
interface KeyValueDelegate {

    int getInt(@NonNull String key, int defaultValue);

    void putInt(@NonNull String key, int value);

    boolean getBoolean(@NonNull String key, boolean defaultValue);

    void putBoolean(@NonNull String key, boolean value);

    long getLong(@NonNull String key, long defaultValue);

    void putLong(@NonNull String key, long value);

    @NonNull
    String getString(@NonNull String key, @NonNull String defaultValue);

    void putString(@NonNull String key, @NonNull String value);

    double getDouble(@NonNull String key, double defaultValue);

    void putDouble(@NonNull String key, double value);

    float getFloat(@NonNull String key, float defaultValue);

    void putFloat(@NonNull String key, float value);

    @NonNull
    Set<String> getStringSet(@NonNull String key);

    void putStringSet(@NonNull String key, @NonNull Set<String> value);

    @NonNull
    List<String> getStringList(@NonNull String key);

    void putStringList(@NonNull String key, @NonNull List<String> value);

    <T extends Parcelable> T getParcelable(@NonNull String key);

    <T extends Parcelable> void putParcelable(@NonNull String key, @NonNull T value);

    void remove(String key);

    void clear();

}
