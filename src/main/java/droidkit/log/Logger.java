package droidkit.log;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("squid:S1312")
public class Logger {

    public static final Logger NONE = new Logger();

    public static final Logger LOGCAT = new LogCatLogger();

    protected Logger() {

    }

    public static void debug(@NonNull Class<?> clazz, @NonNull Object format, Object... args) {
        LogManager.get().getLogger(clazz).log(Log.DEBUG, format, args);
    }

    public static void info(@NonNull Class<?> clazz, @NonNull Object format, Object... args) {
        LogManager.get().getLogger(clazz).log(Log.INFO, format, args);
    }

    public static void warn(@NonNull Class<?> clazz, @NonNull Object format, Object... args) {
        LogManager.get().getLogger(clazz).log(Log.WARN, format, args);
    }

    public static void error(@NonNull Class<?> clazz, @NonNull Object format, Object... args) {
        LogManager.get().getLogger(clazz).log(Log.ERROR, format, args);
    }

    public static void error(@NonNull Class<?> clazz, @NonNull Throwable e) {
        LogManager.get().getLogger(clazz).throwing(e);
    }

    //region deprecated

    /**
     * @see #debug(Class, Object, Object...)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     */
    @Deprecated
    public static void debug(@NonNull Object format, Object... args) {
        debug(Logger.class, format, args);
    }

    /**
     * @see #info(Class, Object, Object...)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     */
    @Deprecated
    public static void info(@NonNull Object format, Object... args) {
        info(Logger.class, format, args);
    }

    /**
     * @see #warn(Class, Object, Object...)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     */
    @Deprecated
    public static void warn(@NonNull Object format, Object... args) {
        warn(Logger.class, format, args);
    }

    /**
     * @see #error(Class, Object, Object...)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     */
    @Deprecated
    public static void error(@NonNull Object format, Object... args) {
        error(Logger.class, format, args);
    }

    /**
     * @see #error(Class, Throwable)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     */
    @Deprecated
    public static void error(@NonNull Throwable e) {
        error(Logger.class, e);
    }

    /**
     * @see #debug(Class, Object, Object...)
     * TODO: remove in one of the next releases
     * @deprecated since 5.2.1
     * do not use this method
     */
    @Deprecated
    public static void wtf(@NonNull Object format, Object... args) {
        debug(Logger.class, format, args);
    }
    //endregion

    protected void log(int priority, @NonNull Object format, Object... args) {
        // do nothing
    }

    protected void throwing(@NonNull Throwable e) {
        // do nothing
    }

}
