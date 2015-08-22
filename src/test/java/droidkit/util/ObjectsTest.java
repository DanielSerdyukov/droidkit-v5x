package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import droidkit.content.StringValue;

/**
 * @author Daniel Serdyukov
 */
public class ObjectsTest {

    private Random mRandom;

    @Before
    public void setUp() throws Exception {
        mRandom = new SecureRandom();
    }

    @Test
    public void testCompare() throws Exception {
        final String expected = "expected";
        final String actual = "actual";
        final Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        };
        Assert.assertEquals(expected.compareTo(actual), Objects.compare(expected, actual, comparator));
        Assert.assertEquals(0, Objects.compare("a", "a", comparator));
    }

    @Test
    public void testObjectDeepEquals() throws Exception {
        final Object[] expected = new Object[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = new Object();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testBooleanDeepEquals() throws Exception {
        final boolean[] expected = new boolean[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = mRandom.nextBoolean();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testByteDeepEquals() throws Exception {
        final byte[] expected = new byte[10];
        mRandom.nextBytes(expected);
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testCharDeepEquals() throws Exception {
        final String abc = "ABCDEFGHIJ";
        final char[] expected = new char[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = abc.charAt(mRandom.nextInt(abc.length()));
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testDoubleDeepEquals() throws Exception {
        final double[] expected = new double[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = mRandom.nextDouble();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testFloatDeepEquals() throws Exception {
        final float[] expected = new float[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = mRandom.nextFloat();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }

    @Test
    public void testIntDeepEquals() throws Exception {
        final int[] expected = new int[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = mRandom.nextInt();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }


    @Test
    public void testLongDeepEquals() throws Exception {
        final long[] expected = new long[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = mRandom.nextLong();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }


    @Test
    public void testShortDeepEquals() throws Exception {
        final short[] expected = new short[10];
        for (int i = 0; i < expected.length; ++i) {
            expected[i] = (short) mRandom.nextInt();
        }
        Assert.assertTrue(Objects.deepEquals(expected, Arrays.copyOf(expected, expected.length)));
    }


    @Test
    public void testEqual() throws Exception {
        Assert.assertTrue(Objects.equal(1L, 1L));
        Assert.assertFalse(Objects.equal(1L, 2L));
        Assert.assertTrue(Objects.equal(null, null));
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
        Assert.assertEquals("expected", Objects.toString("expected"));
    }

    @Test
    public void testToStringWithNullValue() throws Exception {
        Assert.assertEquals("expected", Objects.toString("expected", StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Objects.toString(null, StringValue.EMPTY));
    }

}