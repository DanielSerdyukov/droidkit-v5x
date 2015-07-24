package droidkit.sqlite;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.joda.time.DateTime;
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
import droidkit.dynamic.MethodLookup;
import droidkit.sqlite.bean.SQLiteBean;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTypesTest {

    private Object[] mObjects;

    private SQLiteBean mInstance;

    @Before
    public void setUp() throws Exception {
        final SecureRandom random = new SecureRandom();
        mObjects = new Object[]{
                random.nextLong(),
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
                SQLiteBean.Role.ADMIN,
                DateTime.now().getMillis()
        };
        final MatrixCursor cursor = new MatrixCursor(SQLiteBean.COLUMNS);
        cursor.addRow(mObjects);
        cursor.moveToFirst();
        mInstance = MethodLookup.local()
                .find(SQLiteBean.class.getName() + "$SQLite", "instantiate", Cursor.class)
                .invokeStatic(cursor);
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(mObjects[0], mInstance.getId());
    }

    @Test
    public void testGetLong() throws Exception {
        Assert.assertEquals(mObjects[1], mInstance.getLong());
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(mObjects[2], mInstance.getInt());
    }

    @Test
    public void testGetShort() throws Exception {
        Assert.assertEquals(mObjects[3], mInstance.getShort());
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals(mObjects[4], mInstance.getString());
    }

    @Test
    public void testIsBoolean() throws Exception {
        Assert.assertEquals(mObjects[5], mInstance.isBoolean());
    }

    @Test
    public void testGetDouble() throws Exception {
        Assert.assertEquals(mObjects[6], mInstance.getDouble());
    }

    @Test
    public void testGetFloat() throws Exception {
        Assert.assertEquals(mObjects[7], mInstance.getFloat());
    }

    @Test
    public void testGetBigDecimal() throws Exception {
        Assert.assertEquals(mObjects[8], mInstance.getBigDecimal());
    }

    @Test
    public void testGetBigInteger() throws Exception {
        Assert.assertEquals(mObjects[9], mInstance.getBigInteger());
    }

    @Test
    public void testGetByteArray() throws Exception {
        Assert.assertEquals(mObjects[10], mInstance.getByteArray());
    }

    @Test
    public void testGetRole() throws Exception {
        Assert.assertEquals(mObjects[11], mInstance.getRole());
    }

    @Test
    public void testDateTime() throws Exception {
        Assert.assertEquals(mObjects[12], mInstance.getDateTime().getMillis());
    }

}
