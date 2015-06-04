package droidkit.util;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface Observer<T> {

    void onChange(@NonNull T data);

}
