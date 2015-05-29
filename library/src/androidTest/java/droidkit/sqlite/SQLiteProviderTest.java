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
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.database.CursorUtils;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteProviderTest {

    private static final Uri ALL_USERS = Uri.parse("content://droidkit.sqlite/users");

    private static final Uri FIRST_USER = Uri.parse("content://droidkit.sqlite/users/1");

    private ContentResolver mResolver;

    @Before
    public void setUp() throws Exception {
        mResolver = InstrumentationRegistry.getContext().getContentResolver();
        final SQLite sqlite = SQLite.of(InstrumentationRegistry.getContext());
        sqlite.beginTransaction();
        for (int i = 0; i < SQLiteQueryTest.USERS.length; ++i) {
            final SQLiteUser user = SQLiteQueryTest.USERS[i];
            sqlite.execSQL("INSERT INTO users(_id, name, age, weight, avatar, enabled) VALUES(?, ?, ?, ?, ?, ?)",
                    (i + 1), user.mName, user.mAge, user.mWeight, user.mAvatar, (user.mAge > 18));
        }
        sqlite.endTransaction(true);
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
            Assert.assertEquals(SQLiteQueryTest.USERS[3].getName(), CursorUtils.getString(cursor, "name"));
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
            Assert.assertEquals(25, CursorUtils.getInt(cursor, "age"));
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

    @After
    public void tearDown() throws Exception {
        SQLite.of(InstrumentationRegistry.getContext()).execSQL("DELETE FROM users;");
    }

}
