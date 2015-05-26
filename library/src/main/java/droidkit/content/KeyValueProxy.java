package droidkit.content;

import android.support.annotation.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Serdyukov
 */
class KeyValueProxy implements InvocationHandler {

    private final Map<String, TypedValue> mKeyValue = new ConcurrentHashMap<>();

    private final KeyValueDelegate mDelegate;

    protected KeyValueProxy(@NonNull KeyValueDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String key = extractKey(method);
        final Class<?> returnType = method.getReturnType();
        if (IntValue.class.isAssignableFrom(returnType)) {
            return getIntValue(key, IntValue.EMPTY);
        } else if (StringValue.class.isAssignableFrom(returnType)) {
            return getStringValue(key, StringValue.EMPTY);
        } else if (BoolValue.class.isAssignableFrom(returnType)) {
            return getBoolValue(key, BoolValue.EMPTY);
        } else if (LongValue.class.isAssignableFrom(returnType)) {
            return getLongValue(key, LongValue.EMPTY);
        } else if (DoubleValue.class.isAssignableFrom(returnType)) {
            return getDoubleValue(key, DoubleValue.EMPTY);
        } else if (FloatValue.class.isAssignableFrom(returnType)) {
            return getFloatValue(key, FloatValue.EMPTY);
        } else if (StringSetValue.class.isAssignableFrom(returnType)) {
            return getStringSetValue(key);
        } else if (StringListValue.class.isAssignableFrom(returnType)) {
            return getStringListValue(key);
        } else if (ParcelableValue.class.isAssignableFrom(returnType)) {
            return getParcelableValue(key);
        }
        return invokeForKey(key, returnType);
    }

    protected Object invokeForKey(String key, Class<?> returnType) {
        throw new UnsupportedOperationException("No such value for key='" + key + "' with type '" + returnType + "'");
    }

    @NonNull
    private String extractKey(@NonNull Method method) {
        return method.getName();
    }

    @NonNull
    private IntValue getIntValue(@NonNull String key, int defaultValue) {
        IntValue value = (IntValue) mKeyValue.get(key);
        if (value == null) {
            value = new IntValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private StringValue getStringValue(@NonNull String key, String defaultValue) {
        StringValue value = (StringValue) mKeyValue.get(key);
        if (value == null) {
            value = new StringValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private BoolValue getBoolValue(@NonNull String key, boolean defaultValue) {
        BoolValue value = (BoolValue) mKeyValue.get(key);
        if (value == null) {
            value = new BoolValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private LongValue getLongValue(@NonNull String key, long defaultValue) {
        LongValue value = (LongValue) mKeyValue.get(key);
        if (value == null) {
            value = new LongValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private DoubleValue getDoubleValue(@NonNull String key, double defaultValue) {
        DoubleValue value = (DoubleValue) mKeyValue.get(key);
        if (value == null) {
            value = new DoubleValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private FloatValue getFloatValue(@NonNull String key, float defaultValue) {
        FloatValue value = (FloatValue) mKeyValue.get(key);
        if (value == null) {
            value = new FloatValue(mDelegate, key, defaultValue);
            mKeyValue.put(key, value);
        }
        return value;
    }


    @NonNull
    private StringSetValue getStringSetValue(@NonNull String key) {
        StringSetValue value = (StringSetValue) mKeyValue.get(key);
        if (value == null) {
            value = new StringSetValue(mDelegate, key);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private StringListValue getStringListValue(@NonNull String key) {
        StringListValue value = (StringListValue) mKeyValue.get(key);
        if (value == null) {
            value = new StringListValue(mDelegate, key);
            mKeyValue.put(key, value);
        }
        return value;
    }

    @NonNull
    private ParcelableValue getParcelableValue(@NonNull String key) {
        ParcelableValue value = (ParcelableValue) mKeyValue.get(key);
        if (value == null) {
            value = new ParcelableValue(mDelegate, key);
            mKeyValue.put(key, value);
        }
        return value;
    }

}
