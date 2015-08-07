package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
public interface ProxyFunc2 {

    Object invoke(@NonNull Method method, Object[] args) throws Exception;

}
