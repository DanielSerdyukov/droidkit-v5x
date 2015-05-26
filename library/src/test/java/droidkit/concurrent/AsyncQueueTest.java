package droidkit.concurrent;

import android.os.Looper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class AsyncQueueTest {

    private static final int DELAY = 1000;

    @Test
    public void testInvoke() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                Assert.assertNotSame(Looper.getMainLooper(), Looper.myLooper());
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Test
    public void testInvokeWithDelay() throws Exception {
        final long start = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(1);
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                Assert.assertNotSame(Looper.getMainLooper(), Looper.myLooper());
                latch.countDown();
            }
        }, 1000);
        latch.await(2, TimeUnit.SECONDS);
        final double time = System.currentTimeMillis() - start;
        Assert.assertEquals(DELAY, time, 20d);
    }

}
