package droidkit.sqlite;

import android.content.Loader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.concurrent.AsyncQueue;
import droidkit.sqlite.bean.Foo;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Lists;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteLoaderTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        final Foo bean = new Foo();
        bean.setText("first");
        SQLite.save(bean);
    }

    @Test
    public void testLoad() throws Exception {
        final Loader<List<Foo>> loader = SQLite.where(Foo.class).loader();
        final BlockingQueue<List<Foo>> resultQueue = new ArrayBlockingQueue<>(1);
        loader.registerListener(0, new Loader.OnLoadCompleteListener<List<Foo>>() {
            @Override
            public void onLoadComplete(Loader<List<Foo>> loader, List<Foo> data) {
                resultQueue.add(data);
                loader.unregisterListener(this);
                loader.stopLoading();
            }
        });
        loader.startLoading();
        final List<Foo> result = resultQueue.poll(5, TimeUnit.SECONDS);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("first", Lists.getFirst(result).getText());
    }

    @Test
    public void testObserve() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        final Loader<List<Foo>> loader = SQLite.where(Foo.class).loader();
        loader.registerListener(0, new Loader.OnLoadCompleteListener<List<Foo>>() {
            @Override
            public void onLoadComplete(Loader<List<Foo>> loader, List<Foo> data) {
                AsyncQueue.invoke(new Runnable() {
                    @Override
                    public void run() {
                        latch.countDown();
                    }
                });
            }
        });
        loader.startLoading();
        SQLite.save(new Foo());
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}