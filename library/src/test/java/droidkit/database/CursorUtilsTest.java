package droidkit.database;

import android.database.Cursor;
import android.database.MatrixCursor;

import org.junit.Assert;
import org.junit.Before;
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
public class CursorUtilsTest {

    private Cursor mCursor;

    @Before
    public void setUp() throws Exception {
        final MatrixCursor cursor = new MatrixCursor(new String[]{
                "long_field",
                "int_field",
                "sort_field",
                "double_field",
                "float_field",
                "string_field",
                "blob_field"
        });
        cursor.addRow(new Object[]{1000L, 100, (short) 10, 99.99, 66.66f, "test", new byte[]{1, 2, 3}});
        mCursor = cursor;
        mCursor.moveToFirst();
    }

    @Test
    public void testGetLong() throws Exception {
        Assert.assertEquals(mCursor.getLong(mCursor.getColumnIndex("long_field")),
                CursorUtils.getLong(mCursor, "long_field"));
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(mCursor.getInt(mCursor.getColumnIndex("int_field")),
                CursorUtils.getInt(mCursor, "int_field"));
    }

    @Test
    public void testGetShort() throws Exception {
        Assert.assertEquals(mCursor.getShort(mCursor.getColumnIndex("sort_field")),
                CursorUtils.getShort(mCursor, "sort_field"));
    }

    @Test
    public void testGetDouble() throws Exception {
        Assert.assertEquals(mCursor.getDouble(mCursor.getColumnIndex("double_field")),
                CursorUtils.getDouble(mCursor, "double_field"), 0.0);
    }

    @Test
    public void testGetFloat() throws Exception {
        Assert.assertEquals(mCursor.getFloat(mCursor.getColumnIndex("float_field")),
                CursorUtils.getFloat(mCursor, "float_field"), 0.0);
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals(mCursor.getString(mCursor.getColumnIndex("string_field")),
                CursorUtils.getString(mCursor, "string_field"));
    }

    @Test
    public void testGetBlob() throws Exception {
        Assert.assertArrayEquals(mCursor.getBlob(mCursor.getColumnIndex("blob_field")),
                CursorUtils.getBlob(mCursor, "blob_field")
        );
    }

}
