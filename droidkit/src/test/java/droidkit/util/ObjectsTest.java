package droidkit.util;

import org.junit.Assert;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Daniel Serdyukov
 */
public class ObjectsTest {

    @Test
    public void testCompare() throws Exception {
        final String expected = "expected";
        final String actual = "actual";
        Assert.assertEquals(expected.compareTo(actual),
                Objects.compare(expected, actual, new Comparator<String>() {
                    @Override
                    public int compare(String lhs, String rhs) {
                        return lhs.compareTo(rhs);
                    }
                }));
    }

    @Test
    public void testDeepEquals() throws Exception {
        final SecureRandom random = new SecureRandom();
        final int[] expected = new int[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = random.nextInt();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testEqual() throws Exception {
        Assert.assertTrue(Objects.equal(1L, 1L));
        Assert.assertFalse(Objects.equal(1L, 2L));
    }

    @Test
    public void testHash() throws Exception {
        final String expected = "expected";
        Assert.assertEquals(Arrays.hashCode(new Object[]{expected}), Objects.hash(expected));
    }

    @Test
    public void testNotNull() throws Exception {
        Assert.assertEquals("expected", Objects.notNull(null, "expected"));
    }

    @Test(expected = NullPointerException.class)
    public void testRequireNonNull() throws Exception {
        Objects.requireNonNull(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRequireNonNullWithMessage() throws Exception {
        Objects.requireNonNull(null, "npe");
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals("null", Objects.toString(null));
    }

    @Test
    public void testToStringWithNullValue() throws Exception {
        Assert.assertEquals("expected", Objects.toString("expected"));
    }

}