package droidkit.activity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowView;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class ActivityTest {

    private MainActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void testViewInjected() throws Exception {
        Assert.assertNotNull(mActivity.mText1);
        Assert.assertNotNull(mActivity.mButton1);
    }

    @Test
    public void testButton1Click() throws Exception {
        Assert.assertTrue(ShadowView.clickOn(mActivity.findViewById(android.R.id.button1)));
        Assert.assertTrue(mActivity.mButton1Clicked);
    }

    @Test
    public void testButton2Click() throws Exception {
        Assert.assertTrue(ShadowView.clickOn(mActivity.findViewById(android.R.id.button2)));
        Assert.assertTrue(mActivity.mButton2Clicked);
    }

    @Test
    public void testCutActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.cut));
        Assert.assertTrue(mActivity.mCutClicked);
    }

    @Test
    public void testCopyActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.copy));
        Assert.assertTrue(mActivity.mCopyClicked);
    }

    @Test
    public void testPasteActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.paste));
        Assert.assertTrue(mActivity.mPasteClicked);
    }

    @Test
    public void testEditActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.edit));
        Assert.assertTrue(mActivity.mEditClicked);
    }

}
