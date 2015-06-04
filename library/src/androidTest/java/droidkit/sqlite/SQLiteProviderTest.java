package droidkit.sqlite;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.database.DatabaseUtils;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteProviderTest extends SQLiteTestCase {

    private static final Uri ALL_USERS = Uri.parse("content://droidkit.sqlite/users");

    private static final Uri FIRST_USER = Uri.parse("content://droidkit.sqlite/users/1");

    private ContentResolver mResolver;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mResolver = InstrumentationRegistry.getContext().getContentResolver();
    }

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals("vnd.android.cursor.dir/users", mResolver.getType(ALL_USERS));
        Assert.assertEquals("vnd.android.cursor.item/users", mResolver.getType(FIRST_USER));
    }

    @Test
    public void testQuery() throws Exception {
        final Cursor cursor = mResolver.query(ALL_USERS, null, null, null, null);
        try {
            Assert.assertTrue(cursor.moveToFirst());
            Assert.assertEquals(SQLiteQueryTest.USERS.length, cursor.getCount());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testQueryById() throws Exception {
        final Cursor cursor = mResolver.query(ContentUris.withAppendedId(ALL_USERS, 4), null, null, null, null);
        try {
            Assert.assertTrue(cursor.moveToFirst());
            Assert.assertEquals(1, cursor.getCount());
            Assert.assertEquals(SQLiteQueryTest.USERS[3].getName(), DatabaseUtils.getString(cursor, "name"));
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testInsert() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("name", "John");
        mResolver.insert(ALL_USERS, values);
        final Cursor cursor = mResolver.query(ALL_USERS, null, "name = ?", new String[]{"John"}, null);
        try {
            Assert.assertTrue(cursor.moveToFirst());
            Assert.assertEquals(1, cursor.getCount());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("age", 25);
        Assert.assertEquals(1, mResolver.update(ALL_USERS, values, "name = ?", new String[]{"Mia"}));
        final Cursor cursor = mResolver.query(ALL_USERS, null, "name = ?", new String[]{"Mia"}, null);
        try {
            Assert.assertTrue(cursor.moveToFirst());
            Assert.assertEquals(25, DatabaseUtils.getInt(cursor, "age"));
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testDelete() throws Exception {
        Assert.assertEquals(1, mResolver.delete(ALL_USERS, "name = ?", new String[]{"Mia"}));
        final Cursor cursor = mResolver.query(ALL_USERS, null, "name = ?", new String[]{"Mia"}, null);
        try {
            Assert.assertFalse(cursor.moveToFirst());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testNotifyChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final Handler handler = new Handler(Looper.getMainLooper());
        mResolver.registerContentObserver(ALL_USERS, true, new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                latch.countDown();
            }
        });
        mResolver.delete(FIRST_USER, null, null);
        Assert.assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

}
