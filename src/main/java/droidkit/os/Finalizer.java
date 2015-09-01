package droidkit.os;

import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

import droidkit.concurrent.NamedThreadFactory;
import rx.functions.Action1;

/**
 * @author Daniel Serdyukov
 */
public class Finalizer<T> extends PhantomReference<Object> {

    private static final String TAG = "Finalizer";

    private static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue<>();

    private static final List<Reference<?>> REFERENCES = new CopyOnWriteArrayList<>();

    static {
        Executors.newSingleThreadExecutor(new NamedThreadFactory(TAG)).submit(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final Reference<?> reference = REFERENCE_QUEUE.remove();
                        ((Finalizer<?>) reference).finalizeReferent();
                        REFERENCES.remove(reference);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                        Thread.interrupted();
                    }
                }
            }
        });
    }

    private final T mReferent;

    private final Action1<T> mFinalizer;

    private Finalizer(@NonNull Object anchor, @NonNull T referent, @NonNull Action1<T> finalizer) {
        super(anchor, REFERENCE_QUEUE);
        mReferent = referent;
        mFinalizer = finalizer;
    }

    public static <T> boolean create(@NonNull Object anchor, @NonNull T referent, @NonNull Action1<T> finalizer) {
        return REFERENCES.add(new Finalizer<>(anchor, referent, finalizer));
    }

    public static <T> Object create(T referent, Action1<T> finalizer) {
        final Object anchor = new Object();
        REFERENCES.add(new Finalizer<>(anchor, referent, finalizer));
        return anchor;
    }

    private void finalizeReferent() {
        mFinalizer.call(mReferent);
    }

}
