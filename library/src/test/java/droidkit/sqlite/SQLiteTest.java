package droidkit.sqlite;

import android.database.Cursor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest extends SQLiteTestCase {

    @Test
    public void testSave() throws Exception {
        final SQLiteUser entry = new SQLiteUser()
                .setName("User")
                .setLat(99.9);
        SQLite.save(entry);
        final Cursor cursor = getProvider().query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(entry.getName(), DatabaseUtils.getString(cursor, "name"));
        Assert.assertEquals(entry.getLat(), DatabaseUtils.getDouble(cursor, "lat"), 0d);
        cursor.close();
    }

    @Test
    public void testSave5K() throws Exception {
        SQLite.beginTransaction();
        for (int i = 1; i <= 5000; ++i) {
            SQLite.save(new SQLiteUser().setName("User #" + i).setLat(99.9));
        }
        SQLite.endTransaction(true);
        final Cursor cursor = getProvider().query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(5000, cursor.getCount());
        cursor.close();
    }

}
