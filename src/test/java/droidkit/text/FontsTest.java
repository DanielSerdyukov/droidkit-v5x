package droidkit.text;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

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
public class FontsTest {

    private TextView mTextView;

    @Before
    public void setUp() throws Exception {
        final Fonts.FontFactory fontFactory = Mockito.mock(Fonts.FontFactory.class);
        Mockito.when(fontFactory.getAvailableFontNames(RuntimeEnvironment.application))
                .thenReturn(Arrays.asList("serif", "sans-serif"));
        Mockito.when(fontFactory.getTypeface(RuntimeEnvironment.application, "serif"))
                .thenReturn(Typeface.SERIF);
        Mockito.when(fontFactory.getTypeface(RuntimeEnvironment.application, "sans-serif"))
                .thenReturn(Typeface.SANS_SERIF);
        Fonts.setFontFactory(fontFactory);
        mTextView = Mockito.spy(new TextView(RuntimeEnvironment.application));
    }

    @Test
    public void testList() throws Exception {
        Assert.assertTrue(Fonts.list(RuntimeEnvironment.application).contains("sans-serif"));
    }

    @Test
    public void testGetTypeface() throws Exception {
        Assert.assertEquals(Typeface.SERIF, Fonts.getTypeface(RuntimeEnvironment.application, "serif"));
    }

    @Test
    public void testApply() throws Exception {
        final Typeface typeface = Fonts.getTypeface(RuntimeEnvironment.application, "serif");
        Fonts.apply(mTextView, typeface);
        Mockito.verify(mTextView, Mockito.times(1)).setTypeface(typeface);
        Mockito.verify(mTextView, Mockito.times(1)).getPaint();
        Mockito.verify(mTextView, Mockito.times(1)).setPaintFlags(mTextView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Test
    public void testApply1() throws Exception {
        Fonts.apply(mTextView, "sans-serif");
        Mockito.verify(mTextView, Mockito.times(1)).getContext();
    }

}