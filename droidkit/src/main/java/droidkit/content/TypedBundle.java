package droidkit.content;

import android.os.Bundle;
import android.support.annotation.NonNull;

import droidkit.dynamic.ProxyInstance;

/**
 * @author Daniel Serdyukov
 */
public final class TypedBundle extends KeyValueProxy {

    private TypedBundle(@NonNull Bundle bundle) {
        super(new BundleDelegate(bundle));
    }

    public static <T> T from(@NonNull Class<? extends T> type) {
        return from(new Bundle(), type);
    }

    public static <T> T from(@NonNull Bundle bundle, @NonNull Class<? extends T> type) {
        return ProxyInstance.create(type, new TypedBundle(bundle));
    }

}
