package droidkit.sqlite;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.TestUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteUpdateDeleteTest extends SQLiteTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final ContentValues values = new ContentValues();
        for (int i = 1; i <= 10; ++i) {
            values.clear();
            values.put("name", "User #" + i);
            values.put("role", "ADMIN");
            getProvider().insert(SQLite.uriOf(TestUser.class), values);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        final TestUser entry = SQLite.where(TestUser.class).equalTo("name", "User #2").one();
        Assert.assertNotNull(entry);
        entry.setName("User: 2M");
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, "name = ?",
                new String[]{entry.getName()}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(entry.getId(), DatabaseUtils.getLong(cursor, "_id"));
        cursor.close();
    }

    @Test
    public void testDelete() throws Exception {
        SQLite.where(TestUser.class)
                .equalTo("name", "User #3")
                .remove();
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertNotEquals("User #3", DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
    }

    @Test
    public void testNotifyChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        RuntimeEnvironment.application.getContentResolver()
                .registerContentObserver(SQLite.uriOf(TestUser.class), true,
                        new ContentObserver(new Handler()) {
                            @Override
                            public void onChange(boolean selfChange, Uri uri) {
                                latch.countDown();
                            }
                        });
        SQLite.save(new TestUser());
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

}
