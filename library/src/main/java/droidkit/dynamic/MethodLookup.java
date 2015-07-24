package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Serdyukov
 */
public final class MethodLookup {

    private final ConcurrentMap<String, MethodHandle> mCache = new ConcurrentHashMap<>();

    private MethodLookup() {
    }

    public static MethodLookup local() {
        return new MethodLookup();
    }

    public static MethodLookup global() {
        return Holder.INSTANCE;
    }

    static String makeKey(@NonNull Class<?> clazz, @NonNull String name, @NonNull Class<?>... argTypes) {
        return clazz.getName() + "." + name + "(" + Arrays.toString(argTypes) + ")";
    }

    @NonNull
    MethodHandle find(@NonNull Class<?> clazz, @NonNull String name, @NonNull Class<?>... argTypes)
            throws DynamicException {
        final String methodKey = makeKey(clazz, name, argTypes);
        MethodHandle methodHandle = mCache.get(methodKey);
        if (methodHandle == null) {
            final MethodHandle newMethodHandle = MethodHandle.find(clazz, name, argTypes);
            methodHandle = mCache.putIfAbsent(methodKey, newMethodHandle);
            if (methodHandle == null) {
                methodHandle = newMethodHandle;
            }
        }
        return methodHandle;
    }

    private static final class Holder {
        public static final MethodLookup INSTANCE = new MethodLookup();
    }

}
