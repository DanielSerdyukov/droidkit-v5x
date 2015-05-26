package droidkit.util;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Daniel Serdyukov
 */
class ProxyInstance implements InvocationHandler {

    private final Object mTarget;

    ProxyInstance(@NonNull Object target) {
        mTarget = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            final Method targetMethod = DynamicMethod.find(mTarget.getClass(), method.getName(),
                    DynamicMethod.types(args));
            if (method.getReturnType() == targetMethod.getReturnType()) {
                return DynamicMethod.invoke(mTarget, targetMethod, args);
            }
            throw new UnsupportedOperationException(method.getName() + ": return type mismatch, expected '"
                    + method.getReturnType() + "', actual '" + targetMethod.getReturnType() + "'");
        } catch (DynamicException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
