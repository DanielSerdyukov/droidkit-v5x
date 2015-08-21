package droidkit.sqlite;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

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

    public static final String TABLE = "p_test";

    public static final Uri URI = new Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.APPLICATION_ID)
            .path(TABLE)
            .build();

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        SQLiteTestEnv.registerProvider((mProvider = new SQLiteProvider() {
            @Override
            protected SQLiteClient createClient() {
                return new AndroidSQLiteClient(getContext(), null, 1) {
                    @Override
                    protected void onCreate(@NonNull SQLiteDb db) {
                        db.compileStatement("CREATE TABLE " + TABLE + "(_id INTEGER PRIMARY KEY, value TEXT);")
                                .execute();
                    }
                };
            }
        }));
    }

    @Test
    public void testQuery() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        final Uri insertedUri = mProvider.insert(URI, values);
        final Cursor cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals("TEST", Cursors.getString(cursor, "value"));
        cursor.close();
    }

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals("vnd.android.cursor.dir/" + TABLE, mProvider.getType(URI));
        Assert.assertEquals("vnd.android.cursor.item/" + TABLE, mProvider.getType(ContentUris.withAppendedId(URI, 1)));
    }

    @Test
    public void testInsert() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        Assert.assertEquals(ContentUris.withAppendedId(URI, 1), mProvider.insert(URI, values));
    }

    @Test
    public void testDelete() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        final Uri insertedUri = mProvider.insert(URI, values);
        Assert.assertEquals(1, mProvider.delete(insertedUri, null, null));
        final Cursor cursor = mProvider.query(insertedUri, null, null, null, null);
        Assert.assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("value", "TEST");
        mProvider.insert(URI, values);
        values.put("value", "TEST_2");
        Uri insertedUri = mProvider.insert(URI, values);

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