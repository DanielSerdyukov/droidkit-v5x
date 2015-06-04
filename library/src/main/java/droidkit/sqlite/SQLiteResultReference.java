package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import droidkit.concurrent.AsyncQueue;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
class SQLiteResultReference<T> extends PhantomReference<SQLiteResult<T>> implements Closeable {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Set<Reference<?>> SET = new CopyOnWriteArraySet<>();

    private final AtomicReference<Cursor> mCursorRef;

    SQLiteResultReference(@NonNull SQLiteResult<T> referent) {
        super(referent, new FinalizeReferenceQueue<T>());
        mCursorRef = referent.getCursorReference();
    }

    @NonNull
    static <T> SQLiteResult<T> wrap(SQLiteResult<T> referent) {
        SET.add(new SQLiteResultReference<>(referent));
        return referent;
    }

    @Override
    public void close() {
        final Cursor cursor = mCursorRef.get();
        if (cursor != null) {
            IOUtils.closeQuietly(cursor);
        }
    }

    private static final class FinalizeReferenceQueue<T> extends ReferenceQueue<SQLiteResult<T>> implements Runnable {

        private final AtomicBoolean mAlive = new AtomicBoolean();

        public FinalizeReferenceQueue() {
            AsyncQueue.invoke(this);
        }

        @Override
        public void run() {
            if (mAlive.compareAndSet(false, true)) {
                try {
                    while (!Thread.currentThread().isInterrupted() && mAlive.get()) {
                        final Reference<? extends SQLiteResult<T>> reference = remove();
                        if (reference != null) {
                            SET.remove(reference);
                            ((SQLiteResultReference) reference).close();
                            reference.clear();
                            mAlive.compareAndSet(true, false);
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

}
