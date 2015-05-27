package droidkit.log;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import droidkit.io.IOUtils;
import droidkit.util.Dynamic;

/**
 * @author Daniel Serdyukov
 */
public final class Logger {

    public static void debug(@NonNull Object format, Object... args) {
        Log.d(makeTag(Dynamic.getCaller()), formatMessage(format, args));
    }

    public static void info(@NonNull Object format, Object... args) {
        Log.i(makeTag(Dynamic.getCaller()), formatMessage(format, args));
    }

    public static void warn(@NonNull Object format, Object... args) {
        Log.w(makeTag(Dynamic.getCaller()), formatMessage(format, args));
    }

    public static void error(@NonNull Object format, Object... args) {
        Log.e(makeTag(Dynamic.getCaller()), formatMessage(format, args));
    }

    public static void error(@NonNull Throwable e) {
        Log.d(makeTag(Dynamic.getCaller()), formatMessage(e));
    }

    public static void wtf(@NonNull Object format, Object... args) {
        Log.wtf(makeTag(Dynamic.getCaller()), formatMessage(format, args));
    }

    @NonNull
    private static String formatMessage(@Nullable Object format, Object... args) {
        if (format instanceof String && args.length > 0) {
            return String.format((String) format, args);
        }
        return String.valueOf(format);
    }

    @NonNull
    private static String formatMessage(@Nullable Throwable e) {
        if (e != null) {
            final StringWriter trace = new StringWriter();
            final PrintWriter traceWriter = new PrintWriter(new BufferedWriter(trace, 1024), true);
            try {
                e.printStackTrace(traceWriter);
            } finally {
                IOUtils.closeQuietly(traceWriter);
            }
            return trace.toString();
        }
        return "null";
    }

    @NonNull
    private static String makeTag(@NonNull StackTraceElement caller) {
        return makeCallerName(caller) + "[" + Thread.currentThread().getName() + "]";
    }

    @NonNull
    private static String makeCallerName(@NonNull StackTraceElement caller) {
        final String className = caller.getClassName();
        final int lastDot = className.lastIndexOf(".");
        final StringBuilder buf = new StringBuilder(256)
                .append(className.substring(lastDot + 1));
        if (caller.isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            final String fileName = caller.getFileName();
            final int lineNumber = caller.getLineNumber();
            if (!TextUtils.isEmpty(fileName)) {
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

}
