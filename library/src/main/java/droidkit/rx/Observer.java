package droidkit.rx;

import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
public interface Observer<T> {

    void onNext(@NonNull T data);

    void onComplete();

    void onError(@NonNull Throwable e);

}
