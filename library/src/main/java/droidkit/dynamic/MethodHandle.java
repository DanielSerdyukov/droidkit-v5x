package droidkit.dynamic;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class MethodHandle {

    private final Method mMethod;

    MethodHandle(@NonNull Method method) {
        mMethod = method;
    }

    public static MethodLookup lookup() {
        return MethodLookup.local();
    }

    public static MethodLookup lookupGlobal() {
        return MethodLookup.global();
    }

    @NonNull
    static MethodHandle find(@NonNull Class<?> clazz, @NonNull String name, @NonNull Class<?>... argTypes)
            throws DynamicException {
        Class<?> localClass = clazz;
        do {
            try {
                return new MethodHandle(localClass.getDeclaredMethod(name, argTypes));
            } catch (NoSuchMethodException ignored) {
                // continue searching in super class
            }
        } while ((localClass = localClass.getSuperclass()) != null);
        throw new DynamicException("No such method %s.%s(%s)", clazz.getName(), name, Arrays.toString(argTypes));
    }

    @Nullable
    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public <T> T invokeStatic(@Nullable Object... args) throws DynamicException {
        try {
            if (!mMethod.isAccessible()) {
                mMethod.setAccessible(true);
            }
            return (T) mMethod.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DynamicException(e);
        }
    }

    @Nullable
    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public <T> T invokeVirtual(@NonNull Object receiver, @Nullable Object... args) throws DynamicException {
        try {
            if (!mMethod.isAccessible()) {
                mMethod.setAccessible(true);
            }
            return (T) mMethod.invoke(receiver, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DynamicException(e);
        }
    }

}
