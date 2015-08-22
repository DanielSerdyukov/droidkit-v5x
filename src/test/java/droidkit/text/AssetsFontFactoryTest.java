package droidkit.text;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.NoSuchElementException;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.util.Iterables;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class AssetsFontFactoryTest {

    private final Fonts.AssetsFontFactory mFactory = new Fonts.AssetsFontFactory("fonts");

    private Context mContext;

    private AssetManager mAssets;

    private String[] mFonts;

    private Typeface mRobotoThin;

    @Before
    public void setUp() throws Exception {
        mFonts = RuntimeEnvironment.application.getAssets().list("fonts");
        mRobotoThin = Typeface.createFromAsset(RuntimeEnvironment.application.getAssets(), "fonts/Roboto-Thin.ttf");
        mContext = Mockito.spy(RuntimeEnvironment.application);
        mAssets = Mockito.spy(mContext.getAssets());
        Mockito.when(mContext.getAssets()).thenReturn(mAssets);
    }

    @Test
    public void testGetAvailableFontNames() throws Exception {
        Assert.assertArrayEquals(mFonts, Iterables.toArray(mFactory.getAvailableFontNames(mContext), String.class));
        Mockito.doThrow(IOException.class).when(mAssets).list("fonts");
        Assert.assertTrue(mFactory.getAvailableFontNames(mContext).isEmpty());
    }

    @Test
    public void testGetTypeface() throws Exception {
        Assert.assertEquals(mRobotoThin, mFactory.getTypeface(mContext, "Roboto-Thin.ttf"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetTypeface1() throws Exception {
        Assert.assertEquals(Typeface.SANS_SERIF, mFactory.getTypeface(RuntimeEnvironment.application, "sans-serif"));
    }

}
