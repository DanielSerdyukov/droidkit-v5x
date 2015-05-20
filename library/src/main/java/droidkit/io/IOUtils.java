package droidkit.io;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Daniel Serdyukov
 */
public final class IOUtils {

    public static void closeQuietly(@NonNull Closeable... closeable) {
        for (final Closeable c : closeable) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void closeQuietly(@NonNull Cursor... cursors) {
        for (final Cursor cursor : cursors) {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

}
