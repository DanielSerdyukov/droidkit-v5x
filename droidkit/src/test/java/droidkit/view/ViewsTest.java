package droidkit.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.FrameLayout;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class ViewsTest {

    private FrameLayout mFrame;

    @Before
    public void setUp() throws Exception {
        mFrame = new FrameLayout(RuntimeEnvironment.application);
    }

    @Test
    public void testFindById() throws Exception {
        final Activity activity = Mockito.mock(Activity.class);
        Mockito.when(activity.findViewById(android.R.id.content)).thenReturn(mFrame);
        Assert.assertEquals(mFrame, Views.findById(activity, android.R.id.content));
    }

    @Test
    public void testFindById1() throws Exception {
        final View view = Mockito.mock(View.class);
        Mockito.when(view.findViewById(android.R.id.content)).thenReturn(mFrame);
        Assert.assertEquals(mFrame, Views.findById(view, android.R.id.content));
    }

    @Test
    public void testFindById2() throws Exception {
        final Dialog dialog = Mockito.mock(Dialog.class);
        Mockito.when(dialog.findViewById(android.R.id.content)).thenReturn(mFrame);
        Assert.assertEquals(mFrame, Views.findById(dialog, android.R.id.content));
    }

}