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
    public void testButton2Click() throws Exception {
        Assert.assertTrue(ShadowView.clickOn(mActivity.findViewById(android.R.id.button2)));
    }

    @Test
    public void testCutActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.cut));
    }

    @Test
    public void testCopyActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.copy));
    }

    @Test
    public void testPasteActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.paste));
    }

    @Test
    public void testEditActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mActivity).clickMenuItem(android.R.id.edit));
    }

}
