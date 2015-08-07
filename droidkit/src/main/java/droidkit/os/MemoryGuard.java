package droidkit.os;

import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import droidkit.concurrent.AsyncQueue;
import droidkit.util.Objects;
import rx.functions.Action1;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class MemoryGuard extends PhantomReference<Object> {

    private static final int MEMORY_LEAK_THRESHOLD = 100;

    private static final BlockingQueue<ReferenceQueue<?>> QUEUE = new LinkedBlockingQueue<>();

    private static final ConcurrentMap<Reference<?>, Action1<Object>> REFERENCES = new ConcurrentHashMap<>();

    static {
        AsyncQueue.invoke(new Watcher());
    }

    private final Object mObject;

    private MemoryGuard(@NonNull Object referent, @NonNull Object object) {
        super(referent, createReferenceQueue());
        mObject = object;
    }

    @SuppressWarnings("unchecked")
    public static <T> void watch(@NonNull Object referent, @NonNull T object, @NonNull Action1<T> finalizer) {
        if (Objects.equal(referent, object)) {
            throw new IllegalArgumentException("referent and finalizable objects can't be equal");
        }
        REFERENCES.putIfAbsent(new MemoryGuard(referent, object), (Action1<Object>) finalizer);
    }

    @NonNull
    private static <T> ReferenceQueue<T> createReferenceQueue() {
        final ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();
        QUEUE.add(referenceQueue);
        return referenceQueue;
    }

    private static final class Watcher implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final ReferenceQueue<?> referenceQueue = QUEUE.take();
                    final Reference<?> reference = referenceQueue.remove();
                    if (reference != null) {
                        final Action1<Object> finalizer = REFERENCES.remove(reference);
                        if (finalizer != null) {
                            finalizer.call(((MemoryGuard) reference).mObject);
                        }
                        reference.clear();
                    } else {
                        QUEUE.put(referenceQueue);
                    }
                } catch (InterruptedException e) {
                    Log.e("GcWatcher", e.getMessage(), e);
                    Thread.interrupted();
                }
                if (QUEUE.size() > MEMORY_LEAK_THRESHOLD) {
                    throw new IllegalStateException("Too many objects! Possible memory leak!");
                }
            }
        }
    }

}
