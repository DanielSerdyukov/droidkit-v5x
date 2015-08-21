package droidkit.os;

import android.os.Handler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class BackgroundThreadTest {

    @Test
    public void testGet() throws Exception {
        final BackgroundThread expected = BackgroundThread.get();
        final BackgroundThread actual = BackgroundThread.get();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGetHandler() throws Exception {
        final Handler expected = BackgroundThread.getHandler();
        final Handler actual = BackgroundThread.getHandler();
        Assert.assertEquals(expected, actual);
    }

}