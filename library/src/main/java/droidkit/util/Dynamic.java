package droidkit.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
public final class Dynamic {

    private static final Map<Class<?>, Class<?>> BOXING = new HashMap<>();

    private static final int CALLER_DEPTH = 2;

    static {
        BOXING.put(Boolean.TYPE, Boolean.class);
        BOXING.put(Byte.TYPE, Byte.class);
        BOXING.put(Character.TYPE, Character.class);
        BOXING.put(Short.TYPE, Short.class);
        BOXING.put(Integer.TYPE, Integer.class);
        BOXING.put(Long.TYPE, Long.class);
        BOXING.put(Double.TYPE, Double.class);
        BOXING.put(Float.TYPE, Float.class);
        BOXING.put(Void.TYPE, Void.TYPE);
    }

    private Dynamic() {
    }

    @NonNull
    public static StackTraceElement getCaller() {
        return new Throwable().fillInStackTrace().getStackTrace()[CALLER_DEPTH];
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Class<T> forName(@NonNull String name) throws DynamicException {
        try {
            return (Class<T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new DynamicException(e);
        }
    }

    public static boolean inClasspath(@NonNull String name) {
        try {
            return Class.forName(name) != null;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @NonNull
    public static <T> T init(@NonNull Class<? extends T> clazz, Object... args) throws DynamicException {
        try {
            if (args.length == 0) {
                return clazz.newInstance();
            }
            final Constructor<T> init = findInit(clazz, args);
            final boolean isAccessible = init.isAccessible();
            try {
                init.setAccessible(true);
                return init.newInstance(args);
            } finally {
                init.setAccessible(isAccessible);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T init(@NonNull String className, Object... args) throws DynamicException {
        return (T) init(forName(className), args);
    }

    @NonNull
    public static Class<?> unbox(@NonNull Class<?> type) {
        final Class<?> unboxed = BOXING.get(type);
        if (unboxed != null) {
            return unboxed;
        }
        return type;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findInit(@NonNull Class<? extends T> clazz, Object... args)
            throws DynamicException {
        final Constructor<?>[] inits = clazz.getDeclaredConstructors();
        final Class<?>[] argTypes = DynamicMethod.types(args);
        for (final Constructor<?> init : inits) {
            if (DynamicMethod.hasValidSignature(init.getParameterTypes(), argTypes)) {
                return (Constructor<T>) init;
            }
        }
        throw new DynamicException(new NoSuchMethodException("<init> " + Arrays.toString(argTypes)));
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T newProxy(@NonNull InvocationHandler handler, @NonNull Class<T> proxyInterface) {
        return (T) Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class<?>[]{proxyInterface}, handler);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> T newProxy(@NonNull Object target, @NonNull Class<T> proxyInterface) {
        return (T) Proxy.newProxyInstance(
                proxyInterface.getClassLoader(),
                new Class<?>[]{proxyInterface},
                new ProxyInstance(target)
        );
    }

}
