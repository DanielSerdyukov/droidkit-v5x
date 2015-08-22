package droidkit.dynamic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;

import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public class FieldHandle {

    private final Field mField;

    FieldHandle(@NonNull Field field) {
        mField = field;
    }

    @NonNull
    @SuppressWarnings("squid:S1166")
    static FieldHandle find(@NonNull Class<?> clazz, @NonNull String name)
            throws DynamicException {
        Class<?> localClass = clazz;
        do {
            try {
                return new FieldHandle(localClass.getDeclaredField(name));
            } catch (NoSuchFieldException ignored) {
                // continue searching in super class
            }
        } while ((localClass = localClass.getSuperclass()) != null);
        throw new DynamicException("No such field %s.%s", clazz.getName(), name);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getStatic() throws DynamicException {
        try {
            forceAccessible();
            return (T) mField.get(null);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T getStatic(@NonNull T nullDefault) throws DynamicException {
        try {
            forceAccessible();
            return Objects.nullValue((T) mField.get(null), nullDefault);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getVirtual(@NonNull Object receiver) throws DynamicException {
        try {
            forceAccessible();
            return (T) mField.get(receiver);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getVirtual(@NonNull Object receiver, @NonNull T nullDefault) throws DynamicException {
        try {
            forceAccessible();
            return Objects.nullValue((T) mField.get(receiver), nullDefault);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setStatic(@Nullable Object value) throws DynamicException {
        try {
            forceAccessible();
            mField.set(null, value);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void setVirtual(@NonNull Object receiver, @Nullable Object value) throws DynamicException {
        try {
            forceAccessible();
            mField.set(receiver, value);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

    private void forceAccessible() {
        if (!mField.isAccessible()) {
            mField.setAccessible(true);
        }
    }

}
