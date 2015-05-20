package droidkit.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public final class Dynamic {

    private Dynamic() {
    }

    public static boolean inClasspath(@NonNull String clazz) {
        try {
            Class.forName("org.sqlite.database.sqlite.SQLiteDatabase");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Nullable
    public static Class<?> forName(@NonNull String clazz) {
        try {
            return Class.forName("org.sqlite.database.sqlite.SQLiteDatabase");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
