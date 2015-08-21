package droidkit.view;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class ViewInjectorTest {

    @Test
    public void testViewInjected() throws Exception {
        final MainFrame frame = new MainFrame(RuntimeEnvironment.application);
        Assert.assertNotNull(frame.mText1);
        Assert.assertNotNull(frame.mText2);
    }

}
