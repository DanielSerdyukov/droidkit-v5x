package droidkit.concurrent;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Serdyukov
 */
class MainFuture<V> implements RunnableFuture<V> {

    private static final int NEW = 0;

    private static final int COMPLETING = 1;

    private static final int EXCEPTIONAL = 2;

    private static final int CANCELLED = 3;

    private final Callable<V> mTask;

    private volatile int mState;

    MainFuture(@NonNull Callable<V> task) {
        mTask = task;
        mState = NEW;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        MainQueue.getHandler().removeCallbacks(this);
        mState = CANCELLED;
        return true;
    }

    @Override
    public boolean isCancelled() {
        return mState >= CANCELLED;
    }

    @Override
    public boolean isDone() {
        return mState != NEW;
    }

    @Override
    public V get() {
        throw new UnsupportedOperationException("MainQueue does not support getting the result");
    }

    @Override
    public V get(long timeout, @NonNull TimeUnit unit) {
        throw new UnsupportedOperationException("MainQueue does not support getting the result");
    }

    @Override
    public void run() {
        try {
            mTask.call();
            mState = COMPLETING;
        } catch (Exception e) {
            Log.e("MainFuture", e.getMessage(), e);
            mState = EXCEPTIONAL;
        }
    }

}
