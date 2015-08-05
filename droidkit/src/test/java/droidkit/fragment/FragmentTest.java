package droidkit.fragment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowView;
import org.robolectric.util.FragmentTestUtil;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class FragmentTest {

    private MainFragment mFragment;

    @Before
    public void setUp() throws Exception {
        mFragment = new MainFragment();
        FragmentTestUtil.startVisibleFragment(mFragment);
    }

    @Test
    public void testViewInjected() throws Exception {
        Assert.assertNotNull(mFragment.mText1);
        Assert.assertNotNull(mFragment.mButton1);
    }

    @Test
    public void testButton1Click() throws Exception {
        Assert.assertTrue(ShadowView.clickOn(mFragment.mButton1));
        Assert.assertTrue(mFragment.mButton1Clicked);
    }

    @Test
    public void testCutActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mFragment.getActivity()).clickMenuItem(android.R.id.cut));
        Assert.assertTrue(mFragment.mCutClicked);
    }

    @Test
    public void testCopyActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mFragment.getActivity()).clickMenuItem(android.R.id.copy));
        Assert.assertTrue(mFragment.mCopyClicked);
    }

    @Test
    public void testPasteActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mFragment.getActivity()).clickMenuItem(android.R.id.paste));
        Assert.assertTrue(mFragment.mPasteClicked);
    }

    @Test
    public void testEditActionClick() throws Exception {
        Assert.assertTrue(Shadows.shadowOf(mFragment.getActivity()).clickMenuItem(android.R.id.edit));
        Assert.assertTrue(mFragment.mEditClicked);
    }

}
