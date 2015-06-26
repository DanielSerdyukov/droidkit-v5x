package droidkit.concurrent;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final String DEFAULT_NAME = "Thread #";

    private final AtomicInteger mThreadNumber = new AtomicInteger(1);

    private final ThreadGroup mGroup;

    private final String mName;

    public NamedThreadFactory() {
        this(DEFAULT_NAME);
    }

    public NamedThreadFactory(@NonNull String name) {
        mName = name;
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            mGroup = sm.getThreadGroup();
        } else {
            mGroup = Thread.currentThread().getThreadGroup();
        }
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        final Thread thread = new Thread(mGroup, r, mName + mThreadNumber.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    }

}
