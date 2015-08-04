package droidkit.os;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

/**
 * @author Daniel Serdyukov
 */
public final class BackgroundThread extends HandlerThread {

    private static volatile BackgroundThread sInstance;

    private static volatile Handler sHandler;

    private BackgroundThread() {
        super("android.bg", Process.THREAD_PRIORITY_BACKGROUND);
    }

    private static void ensureThreadLocked() {
        BackgroundThread instance = sInstance;
        if (instance == null) {
            synchronized (BackgroundThread.class) {
                instance = sInstance;
                if (instance == null) {
                    sInstance = new BackgroundThread();
                    sInstance.start();
                    sHandler = new Handler(sInstance.getLooper());
                }
            }
        }
    }

    public static BackgroundThread get() {
        ensureThreadLocked();
        return sInstance;
    }

    public static Handler getHandler() {
        ensureThreadLocked();
        return sHandler;
    }

}
