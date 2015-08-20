package droidkit.util;

import org.junit.Assert;
import org.junit.Test;

import droidkit.content.StringValue;

/**
 * @author Daniel Serdyukov
 */
public class StringsTest {

    @Test
    public void testNullToEmpty() throws Exception {
        Assert.assertEquals(StringValue.EMPTY, Strings.nullToEmpty(null));
    }

    @Test
    public void testRequireNotNull() throws Exception {
        Assert.assertEquals("ABC", Strings.requireNotNull(null, "ABC"));
        Assert.assertEquals("ABC", Strings.requireNotNull("ABC", "NULL"));
    }

    @Test
    public void testToUnderScope() throws Exception {
        Assert.assertEquals("camel_case", Strings.toUnderScope("CamelCase"));
        Assert.assertEquals("camel_case", Strings.toUnderScope("camelCase"));
    }

    @Test
    public void testCapitalize() throws Exception {
        Assert.assertEquals("Test", Strings.capitalize("test"));
    }

}