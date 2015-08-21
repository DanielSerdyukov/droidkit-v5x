package droidkit.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class SequenceTest {

    @Test
    public void testGet() throws Exception {
        final Sequence sequence1 = Sequence.get();
        final Sequence sequence2 = Sequence.get();
        Assert.assertEquals(sequence1, sequence2);
    }

    @Test
    public void testNextLong() throws Exception {
        final Sequence sequence = new Sequence(10);
        Assert.assertEquals(11L, sequence.nextLong());
    }

    @Test
    public void testNextInt() throws Exception {
        final Sequence sequence = new Sequence(100);
        Assert.assertEquals(101, sequence.nextInt());
    }

}