package droidkit.concurrent;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Daniel Serdyukov
 */
public final class MainQueue {

    private static volatile Handler sHandler;

    private MainQueue() {

    }

    /**
     * @deprecated since 5.0.1, will be removed in 5.1.1
     * no instance required more
     */
    @Deprecated
    public static MainQueue get() {
        return Holder.INSTANCE;
    }

    @NonNull
    public static Handler getHandler() {
        Handler handler = sHandler;
        if (handler == null) {
            synchronized (MainQueue.class) {
                handler = sHandler;
                if (handler == null) {
                    handler = sHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return handler;
    }

    @NonNull
    public static Future<?> invoke(@NonNull Runnable task) {
        return invoke(Executors.callable(task));
    }

    @NonNull
    public static <V> Future<V> invoke(@NonNull Callable<V> task) {
        final RunnableFuture<V> future = new MainFuture<>(task);
        getHandler().post(future);
        return future;
    }

    @NonNull
    public static ScheduledFuture<?> invoke(@NonNull Runnable task, long delay) {
        return invoke(Executors.callable(task), delay);
    }

    @NonNull
    public static <V> ScheduledFuture<V> invoke(@NonNull Callable<V> task, long delay) {
        final ScheduledMainFuture<V> future = new ScheduledMainFuture<>(task, delay);
        getHandler().postDelayed(future, delay);
        return future;
    }

    private static abstract class Holder {
        public static final MainQueue INSTANCE = new MainQueue();

        private Holder() {
            //no instance
        }
    }

}
