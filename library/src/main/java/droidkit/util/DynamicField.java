package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public final class DynamicField {

    private DynamicField() {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T get(@NonNull Object target, @NonNull String fieldName) throws DynamicException {
        return get(target, find(target.getClass(), fieldName));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T get(@Nullable Object target, @NonNull Field field) throws DynamicException {
        final boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getStatic(@NonNull Class<?> clazz, @NonNull String fieldName) throws DynamicException {
        return get(null, find(clazz, fieldName));
    }

    @SuppressWarnings("unchecked")
    public static void set(@NonNull Object target, @NonNull String fieldName, @Nullable Object value)
            throws DynamicException {
        set(target, find(target.getClass(), fieldName), value);
    }

    @SuppressWarnings("unchecked")
    public static void set(@Nullable Object target, @NonNull Field field, @Nullable Object value)
            throws DynamicException {
        final boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new DynamicException(e);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    @SuppressWarnings("unchecked")
    public static void setStatic(@NonNull Class<?> clazz, @NonNull String fieldName, @Nullable Object value)
            throws DynamicException {
        set(null, find(clazz, fieldName), value);
    }

    @NonNull
    public static Field find(@NonNull Class<?> clazz, @NonNull String name) throws DynamicException {
        do {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        throw new DynamicException(new NoSuchFieldException(name));
    }

    @NonNull
    public static Field find(@NonNull String className, @NonNull String name) throws DynamicException {
        return find(Dynamic.forName(className), name);
    }

    @NonNull
    public static List<Field> annotatedWith(@NonNull Class<?> type, @NonNull Class<? extends Annotation> annotation) {
        final List<Field> annotatedMethods = new ArrayList<>();
        do {
            final Field[] methods = type.getDeclaredFields();
            for (final Field field : methods) {
                if (field.isAnnotationPresent(annotation)) {
                    annotatedMethods.add(field);
                }
            }
        } while ((type = type.getSuperclass()) != null);
        return annotatedMethods;
    }

}
