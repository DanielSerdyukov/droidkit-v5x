package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Daniel Serdyukov
 */
public class ProxyInstance {

    private ProxyInstance() {
        //no instance
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T create(@NonNull Class<T> iface, @NonNull final ProxyFunc2 func) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return func.invoke(method, args);
            }
        });
    }

    @NonNull
    public static <T> T wrap(@NonNull Class<T> iface, @NonNull final Object object,
                             @NonNull final MethodLookup lookup) {
        return wrap(iface, object, new ProxyFunc3() {
            @Override
            public Object invoke(@NonNull Object object, @NonNull Method method, Object[] args) throws Exception {
                return lookup.find(object.getClass(), method.getName(), method.getParameterTypes())
                        .invokeVirtual(object, args);
            }
        });
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T wrap(@NonNull Class<T> iface, @NonNull final Object object, @NonNull final ProxyFunc3 func) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return func.invoke(object, method, args);
            }
        });
    }

}
