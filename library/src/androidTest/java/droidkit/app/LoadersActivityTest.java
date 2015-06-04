package droidkit.app;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class LoadersActivityTest {

    @Rule
    public ActivityTestRule<LoadersActivity> mRule = new ActivityTestRule<>(LoadersActivity.class);

    private LoadersActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = mRule.getActivity();
    }

    @Test
    public void testLoader1() throws Exception {
        Loaders.init(mActivity.getLoaderManager(), 1, null, mActivity);
        Assert.assertTrue(mActivity.mLoader1Loaded.await(5, TimeUnit.SECONDS));
        Loaders.destroy(mActivity.getLoaderManager(), 1);
        Assert.assertTrue(mActivity.mLoader1Destroyed);
    }

    @Test
    public void testLoader2() throws Exception {
        Loaders.init(mActivity.getLoaderManager(), 2, null, mActivity);
        Assert.assertTrue(mActivity.mLoader2Loaded.await(5, TimeUnit.SECONDS));
        Loaders.destroy(mActivity.getLoaderManager(), 2);
        Assert.assertTrue(mActivity.mLoader2Destroyed);
    }

    @Test
    public void testLoader3() throws Exception {
        Loaders.init(mActivity.getLoaderManager(), 3, null, mActivity);
        Assert.assertTrue(mActivity.mLoader3Loaded.await(5, TimeUnit.SECONDS));
        Loaders.destroy(mActivity.getLoaderManager(), 3);
        Assert.assertTrue(mActivity.mLoader3Destroyed);
    }

}
