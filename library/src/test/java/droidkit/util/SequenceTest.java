package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SequenceTest {

    private Sequence mSequence;

    @Before
    public void setUp() throws Exception {
        mSequence = new Sequence(100);
    }

    @Test
    public void testNextLong() throws Exception {
        Assert.assertEquals(101L, mSequence.nextLong());
    }

    @Test
    public void testNextInt() throws Exception {
        Assert.assertEquals(101, mSequence.nextInt());
    }

}
