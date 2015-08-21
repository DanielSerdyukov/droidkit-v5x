package droidkit.log;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
public class LogCatLogger extends Logger {

    public static final String TAG = "droidkit.log";

    private static final String CRLF = "\n";

    @NonNull
    private static String callerToString(@NonNull StackTraceElement caller) {
        final String className = caller.getClassName();
        final int lastDot = className.lastIndexOf(".");
        final StringBuilder buf = new StringBuilder(256)
                .append(className.substring(lastDot + 1))
                .append(".").append(caller.getMethodName());
        if (caller.isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            final String fileName = caller.getFileName();
            final int lineNumber = caller.getLineNumber();
            if (fileName != null && !fileName.isEmpty()) {
                buf.append("(").append(fileName);
                if (lineNumber >= 0) {
                    buf.append(':');
                    buf.append(lineNumber);
                }
                buf.append(")");
            } else {
                buf.append("(Unknown Source)");
            }
        }
        return buf.toString();
    }

    @Override
    protected void log(int priority, @NonNull Object format, Object... args) {
        final StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];
        log(priority, caller, String.format(Locale.US, String.valueOf(format), args));
    }

    @Override
    protected void throwing(@NonNull Throwable e) {
        Log.e(TAG, e.getMessage(), e);
    }

    @VisibleForTesting
    void log(int priority, @NonNull StackTraceElement caller, @NonNull String message) {
        Log.println(priority, TAG, callerToString(caller) + CRLF + message);
    }

}
