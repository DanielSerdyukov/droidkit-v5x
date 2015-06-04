package droidkit.sqlite;

import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.concurrent.AsyncQueue;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteLoaderTest extends SQLiteTestCase {

    private SQLiteLoader<SQLiteUser> mLoader;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mLoader = new SQLiteLoader<>(InstrumentationRegistry.getContext(), SQLiteUser.class);
    }

    @Test
    public void testOnLoad() throws Exception {
        final List<SQLiteUser> expected = getSQLite().where(SQLiteUser.class).list();
        final List<SQLiteUser> actual = getLoaderResultSync(mLoader);
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); ++i) {
            Assert.assertEquals(expected.get(i), actual.get(i));
        }
    }

    @Test
    public void testOnChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        mLoader.registerListener(0, new Loader.OnLoadCompleteListener<SQLiteResult<SQLiteUser>>() {
            @Override
            public void onLoadComplete(Loader<SQLiteResult<SQLiteUser>> loader, SQLiteResult<SQLiteUser> data) {
                latch.countDown();
            }
        });
        mLoader.startLoading();
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                getSQLite().where(SQLiteUser.class).equalTo("_id", 1).remove();
            }
        }, 1000);
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @After
    @Override
    public void tearDown() throws Exception {
        mLoader.reset();
        super.tearDown();
    }

    private <T> T getLoaderResultSync(@NonNull Loader<T> loader) throws InterruptedException {
        final BlockingQueue<T> queue = new ArrayBlockingQueue<>(1);
        loader.registerListener(0, new Loader.OnLoadCompleteListener<T>() {
            @Override
            public void onLoadComplete(Loader<T> completedLoader, T data) {
                completedLoader.unregisterListener(this);
                completedLoader.stopLoading();
                queue.add(data);
            }
        });
        loader.startLoading();
        return queue.poll(5, TimeUnit.SECONDS);
    }

}
