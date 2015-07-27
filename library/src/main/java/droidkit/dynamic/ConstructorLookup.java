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

    public static ConstructorLookup local() {
        return new ConstructorLookup();
    }

    public static ConstructorLookup global() {
        return Holder.INSTANCE;
    }

    static String makeKey(@NonNull Class<?> clazz, @NonNull Class<?>... argTypes) {
        return clazz.getName() + ".<init>(" + Arrays.toString(argTypes) + ")";
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> ConstructorHandle<T> find(@NonNull String fqcn, @NonNull Class<?>... argTypes) throws DynamicException {
        try {
            return (ConstructorHandle<T>) find(Class.forName(fqcn), argTypes);
        } catch (DynamicException | ClassNotFoundException e) {
            throw new DynamicException(e);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> ConstructorHandle<T> find(@NonNull Class<T> clazz, @NonNull Class<?>... argTypes)
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

    private abstract static class Holder {
        public static final ConstructorLookup INSTANCE = new ConstructorLookup();

        private Holder() {
            //no instance
        }
    }

}
