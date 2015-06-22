package droidkit.sqlite;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.database.DatabaseUtils;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteTest {

    private ContentResolver mResolver;

    @Before
    public void setUp() throws Exception {
        mResolver = InstrumentationRegistry.getContext().getContentResolver();
    }

    @Test
    public void testSQLiteVersion() throws Exception {
        final SQLiteClient client = SQLite.obtainClient();
        Assert.assertSame(SQLiteOrgClient.class, client.getClass());
        Assert.assertEquals("3.8.10.2", client.queryForString("SELECT sqlite_version();"));
    }

    @Test
    public void testSave() throws Exception {
        final TestUser user = new TestUser().setName("User").setRole(TestUser.Role.ADMIN);
        SQLite.save(user);
        final Cursor cursor = mResolver.query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(user.getName(), DatabaseUtils.getString(cursor, "name"));
        Assert.assertEquals(user.getRole(), DatabaseUtils.getEnum(cursor, "role", TestUser.Role.class));
        cursor.close();
    }

    @Test
    public void testSave5K() throws Exception {
        SQLite.beginTransaction();
        for (int i = 1; i <= 5000; ++i) {
            SQLite.save(new TestUser().setName("User #" + i));
        }
        SQLite.endTransaction(true);
        final Cursor cursor = mResolver.query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(5000, cursor.getCount());
        cursor.close();
    }

    @After
    public void tearDown() throws Exception {
        mResolver.delete(SQLite.uriOf(TestUser.class), null, null);
    }

}
