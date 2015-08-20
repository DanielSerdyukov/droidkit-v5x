package droidkit.concurrent;

import android.os.Looper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class MainQueueTest {

    @Test
    public void testGetHandler() throws Exception {
        Assert.assertEquals(Looper.getMainLooper(), MainQueue.getHandler().getLooper());
    }

    @Test
    public void testInvokeRunnable() throws Exception {
        final AtomicBoolean invoked = new AtomicBoolean();
        MainQueue.invoke(new Runnable() {
            @Override
            public void run() {
                invoked.compareAndSet(false, true);
            }
        });
        Assert.assertTrue(invoked.get());
    }

    @Test
    public void testScheduleRunnable() throws Exception {
        final AtomicLong invoked = new AtomicLong(System.currentTimeMillis());
        MainQueue.invoke(new Runnable() {
            @Override
            public void run() {
                invoked.set(System.currentTimeMillis() - invoked.get());
            }
        }, 1000);
        Assert.assertTrue(invoked.get() >= 1000);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvokeCallable() throws Exception {
        final AtomicBoolean invoked = new AtomicBoolean();
        final Future<Object> future = MainQueue.invoke(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                invoked.compareAndSet(false, true);
                return null;
            }
        });
        Assert.assertTrue(invoked.get());
        future.get();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testScheduleCallable() throws Exception {
        final AtomicLong invoked = new AtomicLong(System.currentTimeMillis());
        final Future<Object> future = MainQueue.invoke(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                invoked.set(System.currentTimeMillis() - invoked.get());
                return null;
            }
        }, 1000);
        Assert.assertTrue(invoked.get() >= 1000);
        future.get();
    }

}