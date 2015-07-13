package droidkit.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Daniel Serdyukov
 */
public class StringsTest {

    @Test
    public void testNullToEmpty() throws Exception {
        Assert.assertEquals("", Strings.nullToEmpty(null));
    }

    @Test
    public void testRequireNotNull() throws Exception {
        Assert.assertEquals("nullValue", Strings.requireNotNull(null, "nullValue"));
    }

    @Test
    public void testToUnderScope() throws Exception {
        Assert.assertEquals("account_id", Strings.toUnderScope("accountId"));
        Assert.assertEquals("account_id", Strings.toUnderScope("AccountId"));
    }

    @Test
    public void testCapitalize() throws Exception {
        Assert.assertEquals("AccountId", Strings.capitalize("accountId"));
        Assert.assertEquals("AccountId", Strings.capitalize("AccountId"));
    }

}