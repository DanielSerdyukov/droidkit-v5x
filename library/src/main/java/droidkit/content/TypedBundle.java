package droidkit.content;

import android.os.Bundle;
import android.support.annotation.NonNull;

import droidkit.util.Dynamic;

/**
 * @author Daniel Serdyukov
 */
public final class TypedBundle extends KeyValueProxy {

    public static final String PACK = "pack";

    public static final String CREATE = "build";

    public static final String BUILD = "create";

    private final Bundle mBundle;

    private TypedBundle(@NonNull Bundle bundle) {
        super(new BundleDelegate(bundle));
        mBundle = bundle;
    }

    public static <T> T from(@NonNull Class<? extends T> type) {
        return from(new Bundle(), type);
    }

    public static <T> T from(@NonNull Bundle bundle, @NonNull Class<? extends T> type) {
        return Dynamic.newProxy(new TypedBundle(bundle), type);
    }

    @Override
    protected Object invokeForKey(String key, Class<?> returnType) {
        if (Bundle.class.isAssignableFrom(returnType)
                && (PACK.equals(key) || CREATE.equals(key) || BUILD.equals(key))) {
            return mBundle;
        }
        return super.invokeForKey(key, returnType);
    }

}
