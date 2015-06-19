package droidkit.sqlite;

import android.content.pm.ProviderInfo;
import android.database.Cursor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest {

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
    }

    @Test
    public void testSave() throws Exception {
        final SQLiteUser entry = new SQLiteUser()
                .setName("User")
                .setLat(99.9);
        SQLite.save(entry);
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(entry.getName(), DatabaseUtils.getString(cursor, "name"));
        Assert.assertEquals(entry.getLat(), DatabaseUtils.getDouble(cursor, "lat"), 0d);
        cursor.close();
    }

    @Test
    public void testSave5K() throws Exception {
        SQLite.beginTransaction();
        for (int i = 1; i <= 5000; ++i) {
            SQLite.save(new SQLiteUser()
                    .setName("User #" + i)
                    .setLat(99.9));
        }
        SQLite.endTransaction(true);
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
