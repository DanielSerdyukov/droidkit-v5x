package droidkit.util;

import android.database.Cursor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class CursorsTest {

    private final SecureRandom mRandom = new SecureRandom();

    private Cursor mCursor;

    @Before
    public void setUp() throws Exception {
        mCursor = Mockito.mock(Cursor.class);
    }

    @Test
    public void testGetString() throws Exception {
        Mockito.when(mCursor.getColumnIndex("text")).thenReturn(1);
        Mockito.when(mCursor.getString(1)).thenReturn("expected");
        Assert.assertEquals("expected", Cursors.getString(mCursor, "text"));
    }

    @Test
    public void testGetLong() throws Exception {
        final long expected = mRandom.nextLong();
        Mockito.when(mCursor.getColumnIndex("timestamp")).thenReturn(1);
        Mockito.when(mCursor.getLong(1)).thenReturn(expected);
        Assert.assertEquals(expected, Cursors.getLong(mCursor, "timestamp"));
    }

    @Test
    public void testGetInt() throws Exception {
        final int expected = mRandom.nextInt();
        Mockito.when(mCursor.getColumnIndex("count")).thenReturn(1);
        Mockito.when(mCursor.getInt(1)).thenReturn(expected);
        Assert.assertEquals(expected, Cursors.getInt(mCursor, "count"));
    }

    @Test
    public void testGetShort() throws Exception {
        final short expected = (short) mRandom.nextInt();
        Mockito.when(mCursor.getColumnIndex("age")).thenReturn(1);
        Mockito.when(mCursor.getShort(1)).thenReturn(expected);
        Assert.assertEquals(expected, Cursors.getShort(mCursor, "age"));
    }

    @Test
    public void testGetDouble() throws Exception {
        final double expected = mRandom.nextDouble();
        Mockito.when(mCursor.getColumnIndex("lat")).thenReturn(1);
        Mockito.when(mCursor.getDouble(1)).thenReturn(expected);
        Assert.assertEquals(expected, Cursors.getDouble(mCursor, "lat"), 0.0d);
    }

    @Test
    public void testGetFloat() throws Exception {
        final float expected = mRandom.nextFloat();
        Mockito.when(mCursor.getColumnIndex("radius")).thenReturn(1);
        Mockito.when(mCursor.getFloat(1)).thenReturn(expected);
        Assert.assertEquals(expected, Cursors.getFloat(mCursor, "radius"), 0.0d);
    }

    @Test
    public void testGetBlob() throws Exception {
        final byte[] expected = new byte[10];
        mRandom.nextBytes(expected);
        Mockito.when(mCursor.getColumnIndex("avatar")).thenReturn(1);
        Mockito.when(mCursor.getBlob(1)).thenReturn(expected);
        Assert.assertArrayEquals(expected, Cursors.getBlob(mCursor, "avatar"));
    }

    @Test
    public void testGetBoolean() throws Exception {
        final boolean expected = mRandom.nextBoolean();
        Mockito.when(mCursor.getColumnIndex("enabled")).thenReturn(1);
        Mockito.when(mCursor.getLong(1)).thenReturn(expected ? 1L : 0L);
        Assert.assertEquals(expected, Cursors.getBoolean(mCursor, "enabled"));
    }

    @Test
    public void testGetEnum() throws Exception {
        final Role expected = Role.USER;
        Mockito.when(mCursor.getColumnIndex("role")).thenReturn(1);
        Mockito.when(mCursor.getString(1)).thenReturn("USER");
        Assert.assertEquals(expected, Cursors.getEnum(mCursor, "role", Role.class));
    }

    @Test
    public void testGetBigInteger() throws Exception {
        final BigInteger expected = BigInteger.TEN;
        Mockito.when(mCursor.getColumnIndex("big_int")).thenReturn(1);
        Mockito.when(mCursor.getLong(1)).thenReturn(expected.longValue());
        Assert.assertEquals(expected, Cursors.getBigInteger(mCursor, "big_int"));
    }

    @Test
    public void testGetBigDecimal() throws Exception {
        final BigDecimal expected = BigDecimal.TEN;
        Mockito.when(mCursor.getColumnIndex("big_dec")).thenReturn(1);
        Mockito.when(mCursor.getDouble(1)).thenReturn(expected.doubleValue());
        Assert.assertEquals(expected.doubleValue(), Cursors.getBigDecimal(mCursor, "big_dec").doubleValue(), 0.0d);
    }

    @After
    public void tearDown() throws Exception {
        mCursor.close();
    }

    public enum Role {USER}

}