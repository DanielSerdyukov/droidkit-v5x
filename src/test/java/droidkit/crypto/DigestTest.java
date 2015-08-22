package droidkit.crypto;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class DigestTest {

    private static final String ORIGINAL_STRING = "com.exzogeni.dk.test.crypto";

    private static final String MD5_STRING = "7dd0d8cf2ac40e5292ae8fc52cc4c0c8";

    private static final String SHA1_STRING = "a51ea9134f52805c384b19600ea2e9e50a975f11";

    private static final String SHA256_STRING = "577a4218999599eec25a0a3e077f0f4578df401d42a159ee8e2b7847abd18d0d";

    @Test
    public void testMd5String() throws Exception {
        Assert.assertEquals(MD5_STRING, Digest.hashString(ORIGINAL_STRING, Digest.MD5));
    }

    @Test
    public void testSha1String() throws Exception {
        Assert.assertEquals(SHA1_STRING, Digest.hashString(ORIGINAL_STRING, Digest.SHA1));
    }

    @Test
    public void testSha256String() throws Exception {
        Assert.assertEquals(SHA256_STRING, Digest.sha256String(ORIGINAL_STRING));
    }

    @Test(expected = DigestException.class)
    public void testUnknownAlg() throws Exception {
        Assert.assertNotNull(Digest.hash(ORIGINAL_STRING.getBytes(), "UNKNOWN"));
    }

}
