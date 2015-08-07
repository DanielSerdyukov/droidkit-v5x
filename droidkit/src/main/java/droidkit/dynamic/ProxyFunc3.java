package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
public interface ProxyFunc3 {

    Object invoke(@NonNull Object object, @NonNull Method method, Object[] args) throws Exception;

}
