package droidkit.content;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.functions.Func3;

/**
 * @author Daniel Serdyukov
 */
class KeyValueProxy implements InvocationHandler {

    private static final Map<Class<?>, Transform> TRANSFORM = new HashMap<>();

    static {
        TRANSFORM.put(IntValue.class, new IntValueTransform());
        TRANSFORM.put(StringValue.class, new StringValueTransform());
        TRANSFORM.put(BoolValue.class, new BoolValueTransform());
        TRANSFORM.put(LongValue.class, new LongValueTransform());
        TRANSFORM.put(DoubleValue.class, new DoubleValueTransform());
        TRANSFORM.put(FloatValue.class, new FloatValueTransform());
        TRANSFORM.put(StringSetValue.class, new StringSetValueTransform());
        TRANSFORM.put(StringListValue.class, new StringListValueTransform());
        TRANSFORM.put(ParcelableValue.class, new ParcelableValueTransform());
    }

    private final Map<String, TypedValue> mKeyValue = new ConcurrentHashMap<>();

    private final KeyValueDelegate mDelegate;

    protected KeyValueProxy(@NonNull KeyValueDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String key = method.getName();
        final Class<?> returnType = method.getReturnType();
        final Transform transform = TRANSFORM.get(returnType);
        if (transform != null) {
            return transform.call(mDelegate, mKeyValue, key);
        }
        throw new IllegalArgumentException("No such value for key='" + key + "' with type '" + returnType + "'");
    }

    private interface Transform extends Func3<KeyValueDelegate, Map<String, TypedValue>, String, TypedValue> {
    }

    private static final class IntValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            IntValue value = (IntValue) map.get(key);
            if (value == null) {
                value = new IntValue(delegate, key, IntValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class StringValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            StringValue value = (StringValue) map.get(key);
            if (value == null) {
                value = new StringValue(delegate, key, StringValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class BoolValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            BoolValue value = (BoolValue) map.get(key);
            if (value == null) {
                value = new BoolValue(delegate, key, BoolValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class LongValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            LongValue value = (LongValue) map.get(key);
            if (value == null) {
                value = new LongValue(delegate, key, LongValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class DoubleValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            DoubleValue value = (DoubleValue) map.get(key);
            if (value == null) {
                value = new DoubleValue(delegate, key, DoubleValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class FloatValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            FloatValue value = (FloatValue) map.get(key);
            if (value == null) {
                value = new FloatValue(delegate, key, FloatValue.EMPTY);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class StringSetValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            StringSetValue value = (StringSetValue) map.get(key);
            if (value == null) {
                value = new StringSetValue(delegate, key);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class StringListValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            StringListValue value = (StringListValue) map.get(key);
            if (value == null) {
                value = new StringListValue(delegate, key);
                map.put(key, value);
            }
            return value;
        }

    }

    private static final class ParcelableValueTransform implements Transform {

        @NonNull
        @Override
        public TypedValue call(@NonNull KeyValueDelegate delegate, @NonNull Map<String, TypedValue> map,
                               @NonNull String key) {
            ParcelableValue value = (ParcelableValue) map.get(key);
            if (value == null) {
                value = new ParcelableValue(delegate, key);
                map.put(key, value);
            }
            return value;
        }

    }

}
