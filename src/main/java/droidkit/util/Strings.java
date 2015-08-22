package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import droidkit.content.StringValue;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("squid:S1118")
public final class Strings {

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

    public static String toUnderScope(String string) {
        return string.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

}
