package droidkit.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel Serdyukov
 */
public class AsyncQueueTest {

    @Test
    public void testGet() throws Exception {
        Assert.assertEquals(AsyncQueue.get(), AsyncQueue.get());
    }

    @Test
    public void testCreate() throws Exception {
        Assert.assertNotNull(AsyncQueue.create());
    }

    @Test
    public void testInvokeRunnable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });
        Assert.assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void testScheduleRunnable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong time = new AtomicLong(System.currentTimeMillis());
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                time.set(System.currentTimeMillis() - time.get());
                latch.countDown();
            }
        }, 1000);
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
        Assert.assertTrue(time.longValue() >= 1000);
    }

    @Test
    public void testInvokeCallable() throws Exception {
        final Future<String> future = AsyncQueue.invoke(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "invoked";
            }
        });
        Assert.assertEquals("invoked", future.get());
    }

    @Test
    public void testScheduleCallable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicLong time = new AtomicLong(System.currentTimeMillis());
        final ScheduledFuture<String> future = AsyncQueue.invoke(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "invoked";
            }
        }, 1000);
        Assert.assertEquals("invoked", future.get());
        Assert.assertTrue(time.longValue() >= 1000);
    }

}