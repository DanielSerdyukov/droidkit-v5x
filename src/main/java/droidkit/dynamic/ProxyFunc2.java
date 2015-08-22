package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
public interface ProxyFunc2 {

    @SuppressWarnings("squid:S00112")
    Object invoke(@NonNull Method method, Object[] args) throws Exception;

}
