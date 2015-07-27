package droidkit.sqlite;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteProviderTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testQuery() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        final Uri insertedUri = mProvider.insert(SQLiteTestEnv.URI, values);
        final Cursor cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals("TEST", Cursors.getString(cursor, "value"));
        cursor.close();
    }

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals("vnd.android.cursor.dir/" + SQLiteTestEnv.TABLE, mProvider.getType(SQLiteTestEnv.URI));
        Assert.assertEquals("vnd.android.cursor.item/" + SQLiteTestEnv.TABLE,
                mProvider.getType(ContentUris.withAppendedId(SQLiteTestEnv.URI, 1)));
    }

    @Test
    public void testInsert() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        Assert.assertEquals(
                ContentUris.withAppendedId(SQLiteTestEnv.URI, 1),
                mProvider.insert(SQLiteTestEnv.URI, values)
        );
    }

    @Test
    public void testDelete() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        final Uri insertedUri = mProvider.insert(SQLiteTestEnv.URI, values);
        Assert.assertEquals(1, mProvider.delete(insertedUri, null, null));
        final Cursor cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        mProvider.insert(SQLiteTestEnv.URI, values);
        values.put("value", "TEST_2");
        Uri insertedUri = mProvider.insert(SQLiteTestEnv.URI, values);

        Cursor cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(1, cursor.getCount());
        Assert.assertEquals("TEST_2", Cursors.getString(cursor, "value"));
        cursor.close();

        values.put("value", "TEST_2M");
        Assert.assertEquals(1, mProvider.update(insertedUri, values, null, null));
        cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(1, cursor.getCount());
        Assert.assertEquals("TEST_2M", Cursors.getString(cursor, "value"));
        cursor.close();
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}