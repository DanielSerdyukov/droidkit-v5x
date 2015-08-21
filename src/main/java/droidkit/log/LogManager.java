package droidkit.log;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.util.Maps;
import droidkit.util.Objects;

/**
 * @author Daniel Serdyukov
 */
public class LogManager {

    private final ConcurrentMap<Class<?>, Logger> mLoggers = new ConcurrentHashMap<>();

    private final AtomicReference<Logger> mGlobalLoggerRef = new AtomicReference<>(Logger.LOGCAT);

    private LogManager() {
    }

    public static LogManager get() {
        return Holder.INSTANCE;
    }

    public void addLogger(@NonNull Class<?> clazz, @NonNull Logger logger) {
        mLoggers.putIfAbsent(clazz, logger);
    }

    @NonNull
    public Logger getLogger(@NonNull Class<?> clazz) {
        return Maps.getNonNull(mLoggers, clazz, getGlobalLogger());
    }

    public void setGlobalLogger(@NonNull Logger logger) {
        mGlobalLoggerRef.compareAndSet(Logger.LOGCAT, logger);
    }

    @NonNull
    public Logger getGlobalLogger() {
        return Objects.notNull(mGlobalLoggerRef.get(), Logger.LOGCAT);
    }


    private static abstract class Holder {
        public static final LogManager INSTANCE = new LogManager();

        private Holder() {
            //no instance
        }
    }

}
