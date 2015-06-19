package droidkit.sqlite;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteUpdateDeleteTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = new SQLiteProvider();
        final ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.name = SQLiteProvider.class.getName();
        providerInfo.authority = BuildConfig.APPLICATION_ID;
        mProvider.attachInfo(RuntimeEnvironment.application, providerInfo);
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(BuildConfig.APPLICATION_ID, mProvider);
        final ContentValues values = new ContentValues();
        for (int i = 1; i <= 10; ++i) {
            values.clear();
            values.put("name", "User #" + i);
            mProvider.insert(SQLite.uriOf(SQLiteUser.class), values);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        final SQLiteUser entry = SQLite.where(SQLiteUser.class).equalTo("name", "User #2").one();
        Assert.assertNotNull(entry);
        entry.setName("User: 2M");
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, "name = ?",
                new String[]{"User: 2M"}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(entry.getId(), DatabaseUtils.getLong(cursor, "_id"));
        cursor.close();
    }

    @Test
    public void testDelete() throws Exception {
        SQLite.where(SQLiteUser.class)
                .equalTo("name", "User #3")
                .remove();
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertNotEquals("User #3", DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
    }

    @Test
    public void testNotifyChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        RuntimeEnvironment.application.getContentResolver()
                .registerContentObserver(SQLite.uriOf(SQLiteUser.class), true,
                        new ContentObserver(new Handler()) {
                            @Override
                            public void onChange(boolean selfChange, Uri uri) {
                                latch.countDown();
                            }
                        });
        SQLite.save(new SQLiteUser());
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
