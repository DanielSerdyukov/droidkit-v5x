package droidkit.sqlite;

import android.database.Cursor;
import android.os.Process;
import android.support.annotation.NonNull;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.concurrent.AsyncQueue;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
class SQLiteGuard<T> extends PhantomReference<SQLiteResult<T>> {

    private static final Set<Reference<?>> REFERENCES = new CopyOnWriteArraySet<>();

    private final AtomicReference<Cursor> mCursorRef;

    private SQLiteGuard(@NonNull SQLiteResult<T> referent, @NonNull AtomicReference<Cursor> cursorRef) {
        super(referent, new FinalizableReferentQueue<T>());
        mCursorRef = cursorRef;
        REFERENCES.add(this);
    }

    static <T> SQLiteGuard<T> get(@NonNull SQLiteResult<T> referent) {
        return new SQLiteGuard<>(referent, referent.getCursorReference());
    }

    private void finalizeReferent() {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null) {
            IOUtils.closeQuietly(cursor);
        }
    }

    private static final class FinalizableReferentQueue<T> extends ReferenceQueue<SQLiteResult<T>>
            implements Runnable {

        public FinalizableReferentQueue() {
            AsyncQueue.invoke(this);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final Reference<? extends SQLiteResult<T>> reference = remove();
                    if (reference != null) {
                        REFERENCES.remove(reference);
                        ((SQLiteGuard) reference).finalizeReferent();
                        reference.clear();
                        break;
                    } else {
                        TimeUnit.SECONDS.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

    }

}
