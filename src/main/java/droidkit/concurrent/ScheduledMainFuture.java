package droidkit.concurrent;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * @author Daniel Serdyukov
 */
class ScheduledMainFuture<V> extends MainFuture<V> implements ScheduledFuture<V> {

    private final long mDelay;

    ScheduledMainFuture(@NonNull Callable<V> task, long delay) {
        super(task);
        mDelay = delay;
    }

    @Override
    public long getDelay(@NonNull TimeUnit unit) {
        return unit.convert(mDelay, unit);
    }

    @Override
    public int compareTo(@NonNull Delayed another) {
        if (another == this) {
            return 0;
        }
        final long diff = getDelay(NANOSECONDS) - another.getDelay(NANOSECONDS);
        return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
    }

}
