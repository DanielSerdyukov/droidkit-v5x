package droidkit.ref;

import android.os.Process;
import android.support.annotation.NonNull;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import droidkit.concurrent.AsyncQueue;

/**
 * @author Daniel Serdyukov
 */
public abstract class CloseGuard<R> extends PhantomReference<R> {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Set<Reference<?>> SET = new CopyOnWriteArraySet<>();

    public CloseGuard(@NonNull R referent) {
        super(referent, new FinalizeReferenceQueue<R>());
        SET.add(this);
    }

    protected abstract void finalizeReferent();

    private static final class FinalizeReferenceQueue<R> extends ReferenceQueue<R> implements Runnable {

        public FinalizeReferenceQueue() {
            AsyncQueue.invoke(this);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final Reference<? extends R> reference = remove();
                    if (reference != null) {
                        ((CloseGuard) reference).finalizeReferent();
                        SET.remove(reference);
                        reference.clear();
                        break;
                    } else {
                        TimeUnit.MILLISECONDS.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

    }

}
