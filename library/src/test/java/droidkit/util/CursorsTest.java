package droidkit.util;

import android.database.MatrixCursor;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class CursorsTest {

    public static final String[] COLUMNS = new String[]{
            "long",
            "int",
            "short",
            "string",
            "boolean",
            "double",
            "float",
            "big_dec",
            "big_int",
            "bytes",
            "enum",
            "date"
    };

    private final MatrixCursor mCursor = new MatrixCursor(COLUMNS);

    private Object[] mValues;

    @Before
    public void setUp() throws Exception {
        final SecureRandom random = new SecureRandom();
        mValues = new Object[]{
                random.nextLong(),
                random.nextInt(),
                (short) random.nextInt(Short.MAX_VALUE / 2),
                "test",
                true,
                random.nextDouble(),
                random.nextFloat(),
                BigDecimal.valueOf(random.nextDouble()),
                BigInteger.valueOf(random.nextLong()),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
                Role.ADMIN,
                DateTime.now().getMillis()
        };
        mCursor.addRow(mValues);
        mCursor.moveToFirst();
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("string")], Cursors.getString(mCursor, "string"));
    }

    @Test
    public void testGetLong() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("long")], Cursors.getLong(mCursor, "long"));
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("int")], Cursors.getInt(mCursor, "int"));
    }

    @Test
    public void testGetShort() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("short")], Cursors.getShort(mCursor, "short"));
    }

    @Test
    public void testGetDouble() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("double")], Cursors.getDouble(mCursor, "double"));
    }

    @Test
    public void testGetFloat() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("float")], Cursors.getFloat(mCursor, "float"));
    }

    @Test
    public void testGetBlob() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("bytes")], Cursors.getBlob(mCursor, "bytes"));
    }

    @Test
    public void testGetBoolean() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("boolean")], Cursors.getBoolean(mCursor, "boolean"));
    }

    @Test
    public void testGetEnum() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("enum")], Cursors.getEnum(mCursor, "enum", Role.class));
    }

    @Test
    public void testGetBigInt() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("big_int")], Cursors.getBigInteger(mCursor, "big_int"));
    }

    @Test
    public void testGetBigDec() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("big_dec")], Cursors.getBigDecimal(mCursor, "big_dec"));
    }

    @Test
    public void testGetDateTime() throws Exception {
        Assert.assertEquals(mValues[mCursor.getColumnIndex("date")], Cursors.getDateTime(mCursor, "date").getMillis());
    }

    @After
    public void tearDown() throws Exception {
        mCursor.close();
    }

    public enum Role {USER, ADMIN}

}