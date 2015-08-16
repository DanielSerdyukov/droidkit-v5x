package droidkit.text;

import android.graphics.Typeface;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class FontFactoryTest {

    private Fonts.FontFactory mFactory;

    @Before
    public void setUp() throws Exception {
        mFactory = Mockito.mock(Fonts.FontFactory.class);
        Mockito.when(mFactory.getAvailableFontNames(RuntimeEnvironment.application))
                .thenReturn(Arrays.asList("serif", "sans-serif", "monospace"));
        Mockito.when(mFactory.getTypeface(RuntimeEnvironment.application, "sans-serif"))
                .thenReturn(Typeface.SANS_SERIF);
    }

    @Test
    public void testGetAvailableFontNames() throws Exception {
        Assert.assertTrue(mFactory.getAvailableFontNames(RuntimeEnvironment.application).contains("serif"));
    }

    @Test
    public void testGetTypeface() throws Exception {
        Assert.assertEquals(Typeface.SANS_SERIF, mFactory.getTypeface(RuntimeEnvironment.application, "sans-serif"));
    }

}
