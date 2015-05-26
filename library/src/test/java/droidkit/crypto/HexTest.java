package droidkit.crypto;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class HexTest {

    private static final String ORIGINAL_STRING = "com.exzogeni.dk.test.crypto";

    private static final byte[] ORIGINAL_STRING_BYTES = ORIGINAL_STRING.getBytes();

    private static final String HEX_STRING = "636f6d2e65787a6f67656e692e646b2e746573742e63727970746f";

    @Test
    public void testToHexString() throws Exception {
        Assert.assertEquals(HEX_STRING, Hex.toHexString(ORIGINAL_STRING));
        Assert.assertEquals(HEX_STRING.toUpperCase(), Hex.toHexString(ORIGINAL_STRING, true));
    }

    @Test
    public void testFromHexString() throws Exception {
        Assert.assertEquals(ORIGINAL_STRING, new String(Hex.fromHexString(HEX_STRING)));
        Assert.assertTrue(Arrays.equals(ORIGINAL_STRING_BYTES, Hex.fromHexString(HEX_STRING)));
    }

}
