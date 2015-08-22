package droidkit.io;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("squid:S1118")
public final class IOUtils {

    @SuppressWarnings({"squid:S1166", "squid:S00108"})
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
