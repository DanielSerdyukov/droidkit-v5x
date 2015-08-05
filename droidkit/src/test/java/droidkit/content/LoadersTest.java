package droidkit.content;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;
import droidkit.app.Loaders;
import droidkit.concurrent.AsyncQueue;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class LoadersTest {

    private Activity mActivity;

    private CountDownLatch mOnLoadFinished;

    private CountDownLatch mOnResetLoader;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.setupActivity(LoaderActivity.class);
        mOnLoadFinished = new CountDownLatch(1);
        mOnResetLoader = new CountDownLatch(1);
    }

    @Test
    public void testLoaderCallbacksImpl() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 0, Bundle.EMPTY, new LoaderManager.LoaderCallbacks<String>() {
            @Override
            public Loader<String> onCreateLoader(int id, Bundle args) {
                return LoadersTest.this.onCreateLoader0();
            }

            @Override
            public void onLoadFinished(Loader<String> loader, String data) {
                LoadersTest.this.onLoadFinished0();
            }

            @Override
            public void onLoaderReset(Loader<String> loader) {
                LoadersTest.this.onResetLoader0();
            }
        });
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 0);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLoaderCallbacks0() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 0, Bundle.EMPTY, this);
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 0);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLoaderCallbacks1_0() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 10, Bundle.EMPTY, this);
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 10);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLoaderCallbacks1_1() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 11, Bundle.EMPTY, this);
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 11);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLoaderCallbacks2_0() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 20, Bundle.EMPTY, this);
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 20);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLoaderCallbacks2_1() throws Exception {
        final LoaderManager lm = mActivity.getLoaderManager();
        Assert.assertNotNull(lm);
        Loaders.init(lm, 21, Bundle.EMPTY, this);
        Assert.assertTrue(mOnLoadFinished.await(5, TimeUnit.SECONDS));
        Loaders.destroy(lm, 21);
        Assert.assertTrue(mOnResetLoader.await(5, TimeUnit.SECONDS));
    }

    @OnCreateLoader(0)
    private Loader<String> onCreateLoader0() {
        return new Loader<String>(RuntimeEnvironment.application) {
            @Override
            protected void onStartLoading() {
                deliverResult(LoadersTest.class.getName());
            }
        };
    }

    @OnLoadFinished(0)
    private void onLoadFinished0() {
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                mOnLoadFinished.countDown();
            }
        });
    }

    @OnResetLoader(0)
    private void onResetLoader0() {
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                mOnResetLoader.countDown();
            }
        });
    }

    @OnCreateLoader(10)
    private Loader<String> onCreateLoader1(int loaderId) {
        Assert.assertEquals(10, loaderId);
        return new Loader<String>(RuntimeEnvironment.application) {
            @Override
            protected void onStartLoading() {
                deliverResult(LoadersTest.class.getName());
            }
        };
    }

    @OnCreateLoader(11)
    private Loader<String> onCreateLoader1(Bundle args) {
        Assert.assertEquals(Bundle.EMPTY, args);
        return new Loader<String>(RuntimeEnvironment.application) {
            @Override
            protected void onStartLoading() {
                deliverResult(LoadersTest.class.getName());
            }
        };
    }

    @OnCreateLoader(20)
    private Loader<String> onCreateLoader2(int loaderId, Bundle args) {
        Assert.assertEquals(20, loaderId);
        Assert.assertEquals(Bundle.EMPTY, args);
        return new Loader<String>(RuntimeEnvironment.application) {
            @Override
            protected void onStartLoading() {
                deliverResult(LoadersTest.class.getName());
            }
        };
    }

    @OnCreateLoader(21)
    private Loader<String> onCreateLoader2(Bundle args, int loaderId) {
        Assert.assertEquals(Bundle.EMPTY, args);
        Assert.assertEquals(21, loaderId);
        return new Loader<String>(RuntimeEnvironment.application) {
            @Override
            protected void onStartLoading() {
                deliverResult(LoadersTest.class.getName());
            }
        };
    }

    @OnLoadFinished({10, 11})
    private void onLoadFinished1(String result) {
        Assert.assertEquals(LoadersTest.class.getName(), result);
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                mOnLoadFinished.countDown();
            }
        });
    }

    @OnLoadFinished({20, 21})
    private void onLoadFinished1(Loader<String> loader, String result) {
        Assert.assertTrue(20 == loader.getId() || 21 == loader.getId());
        Assert.assertEquals(LoadersTest.class.getName(), result);
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                mOnLoadFinished.countDown();
            }
        });
    }

    @OnResetLoader({10, 11, 20, 21})
    private void onResetLoader1(Loader<String> loader) {
        Assert.assertTrue(10 == loader.getId() || 11 == loader.getId()
                || 20 == loader.getId() || 21 == loader.getId());
        AsyncQueue.invoke(new Runnable() {
            @Override
            public void run() {
                mOnResetLoader.countDown();
            }
        });
    }

    private static class LoaderActivity extends Activity {

    }

}
