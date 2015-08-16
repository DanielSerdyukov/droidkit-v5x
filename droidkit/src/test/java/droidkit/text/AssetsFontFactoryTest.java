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

import java.util.NoSuchElementException;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class AssetsFontFactoryTest {

    private final Fonts.AssetsFontFactory mFactory = new Fonts.AssetsFontFactory("fonts");

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final AssetManager assets = Mockito.spy(RuntimeEnvironment.application.getAssets());
        Mockito.when(assets.list("fonts")).thenReturn(new String[]{"serif", "sans-serif"});
        Mockito.when(mContext.getAssets()).thenReturn(assets);
    }

    @Test
    public void testGetAvailableFontNames() throws Exception {
        Assert.assertTrue(mFactory.getAvailableFontNames(mContext).contains("serif"));
        Assert.assertTrue(mFactory.getAvailableFontNames(RuntimeEnvironment.application).isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testGetTypeface() throws Exception {
        Assert.assertEquals(Typeface.SANS_SERIF, mFactory.getTypeface(mContext, "sans-serif"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetTypeface1() throws Exception {
        Assert.assertEquals(Typeface.SANS_SERIF, mFactory.getTypeface(RuntimeEnvironment.application, "sans-serif"));
    }

}
