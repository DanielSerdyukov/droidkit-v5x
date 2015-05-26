package droidkit.util;

import android.support.annotation.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Daniel Serdyukov
 */
public abstract class ObjectPool<T> {

    private final ReentrantReadWriteLock mLock = new ReentrantReadWriteLock();

    private final Deque<T> mDeque = new ArrayDeque<>();

    public T obtain() {
        final ReentrantReadWriteLock.ReadLock lock = mLock.readLock();
        lock.lock();
        try {
            return obtainNonBlock();
        } finally {
            lock.unlock();
        }
    }

    public T obtainNonBlock() {
        final T object = mDeque.pollLast();
        if (object == null) {
            return newEntry();
        }
        return object;
    }

    public void release(@NonNull T entry) {
        final ReentrantReadWriteLock.WriteLock lock = mLock.writeLock();
        lock.lock();
        try {
            releaseNonBlock(entry);
        } finally {
            lock.unlock();
        }
    }

    public void releaseNonBlock(@NonNull T entry) {
        mDeque.offerLast(entry);
    }

    @NonNull
    protected abstract T newEntry();

}
