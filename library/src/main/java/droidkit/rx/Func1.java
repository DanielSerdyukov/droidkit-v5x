package droidkit.rx;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface Func1<R, T> {

    @NonNull
    R call(@NonNull T t);

}
