package droidkit.concurrent;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Serdyukov
 */
public class AsyncQueue extends ScheduledThreadPoolExecutor {

    private static final int CORE_SIZE = Runtime.getRuntime().availableProcessors();

    private static final ThreadFactory THREAD_FACTORY = new NamedThreadFactory("Async #");

    private AsyncQueue(int corePoolSize) {
        super(corePoolSize, THREAD_FACTORY);
    }

    public static AsyncQueue get() {
        return Holder.INSTANCE;
    }

    public static AsyncQueue create() {
        return new AsyncQueue(CORE_SIZE);
    }

    @NonNull
    public static Future<?> invoke(@NonNull Runnable task) {
        return Holder.INSTANCE.submit(task);
    }

    @NonNull
    public static <V> Future<V> invoke(@NonNull Callable<V> task) {
        return Holder.INSTANCE.submit(task);
    }

    @NonNull
    public static ScheduledFuture<?> invoke(@NonNull Runnable task, long delay) {
        return Holder.INSTANCE.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    @NonNull
    public static <V> ScheduledFuture<V> invoke(@NonNull Callable<V> task, long delay) {
        return Holder.INSTANCE.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    private static final class Holder {
        public static final AsyncQueue INSTANCE = new AsyncQueue(CORE_SIZE);

        private Holder() {
        }
    }

}
