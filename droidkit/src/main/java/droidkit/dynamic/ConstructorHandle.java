package droidkit.dynamic;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author Daniel Serdyukov
 */
public class ConstructorHandle<T> {

    private final Constructor<T> mConstructor;

    ConstructorHandle(@NonNull Constructor<T> constructor) {
        mConstructor = constructor;
    }

    @NonNull
    static <T> ConstructorHandle<T> find(@NonNull Class<T> clazz, @NonNull Class<?>... argTypes)
            throws DynamicException {
        try {
            return new ConstructorHandle<>(clazz.getDeclaredConstructor(argTypes));
        } catch (NoSuchMethodException e) {
            throw new DynamicException("No such constructor %s(%s)", clazz.getName(), Arrays.toString(argTypes), e);
        }
    }

    @NonNull
    @SuppressLint("NewApi")
    public T instantiate(@Nullable Object... args) throws DynamicException {
        try {
            if (!mConstructor.isAccessible()) {
                mConstructor.setAccessible(true);
            }
            return mConstructor.newInstance(args);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new DynamicException(e);
        }
    }

}
