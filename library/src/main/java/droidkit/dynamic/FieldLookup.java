package droidkit.dynamic;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Daniel Serdyukov
 */
public final class FieldLookup {

    private final ConcurrentMap<String, FieldHandle> mCache = new ConcurrentHashMap<>();

    private FieldLookup() {
    }

    public static FieldLookup local() {
        return new FieldLookup();
    }

    public static FieldLookup global() {
        return Holder.INSTANCE;
    }

    static String makeKey(@NonNull Class<?> clazz, @NonNull String name) {
        return clazz.getName() + "." + name;
    }

    @NonNull
    FieldHandle find(@NonNull Class<?> clazz, @NonNull String name)
            throws DynamicException {
        final String methodKey = makeKey(clazz, name);
        FieldHandle fieldHandle = mCache.get(methodKey);
        if (fieldHandle == null) {
            final FieldHandle handle = FieldHandle.find(clazz, name);
            fieldHandle = mCache.putIfAbsent(methodKey, handle);
            if (fieldHandle == null) {
                fieldHandle = handle;
            }
        }
        return fieldHandle;
    }

    private static final class Holder {
        public static final FieldLookup INSTANCE = new FieldLookup();
    }

}
