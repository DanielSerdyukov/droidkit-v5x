package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Serdyukov
 */
public final class ConstructorLookup {

    private final ConcurrentMap<String, ConstructorHandle<?>> mCache = new ConcurrentHashMap<>();

    private ConstructorLookup() {
    }

    static ConstructorLookup local() {
        return new ConstructorLookup();
    }

    static ConstructorLookup global() {
        return Holder.INSTANCE;
    }

    static String makeKey(@NonNull Class<?> clazz, @NonNull Class<?>... argTypes) {
        return clazz.getName() + ".<init>(" + Arrays.toString(argTypes) + ")";
    }

    @NonNull
    @SuppressWarnings("unchecked")
    <T> ConstructorHandle<T> findConstructor(@NonNull Class<T> clazz, @NonNull Class<?>... argTypes)
            throws DynamicException {
        final String methodKey = makeKey(clazz, argTypes);
        ConstructorHandle<T> constructorHandle = (ConstructorHandle<T>) mCache.get(methodKey);
        if (constructorHandle == null) {
            final ConstructorHandle<T> handle = ConstructorHandle.find(clazz, argTypes);
            constructorHandle = (ConstructorHandle<T>) mCache.putIfAbsent(methodKey, handle);
            if (constructorHandle == null) {
                constructorHandle = handle;
            }
        }
        return constructorHandle;
    }

    private static final class Holder {
        public static final ConstructorLookup INSTANCE = new ConstructorLookup();
    }

}
