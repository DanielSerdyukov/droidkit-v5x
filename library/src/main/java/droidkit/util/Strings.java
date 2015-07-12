package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.content.StringValue;

/**
 * @author Daniel Serdyukov
 */
public final class Strings {

    private Strings() {
    }

    @NonNull
    public static String nullToEmpty(@Nullable String string) {
        if (string == null) {
            return StringValue.EMPTY;
        }
        return string;
    }

    @NonNull
    public static String requireNotNull(@Nullable String string, @NonNull String defaultValue) {
        if (string == null) {
            return defaultValue;
        }
        return string;
    }

}
