package droidkit.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.math.BigInteger;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.TestUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest extends SQLiteTestCase {

    @Test
    public void testCreate() throws Exception {
        final DateTime lastModified = DateTime.now();
        final ContentValues values = new ContentValues();
        values.put("name", "John");
        values.put("weight", 79.9);
        values.put("big_int", BigInteger.valueOf(1000L).longValue());
        values.put("big_dec", BigDecimal.valueOf(999.9).doubleValue());
        values.put("role", TestUser.Role.USER.name());
        values.put("last_modified", lastModified.getMillis());
        getProvider().insert(SQLite.uriOf(TestUser.class), values);
        final TestUser user = SQLite.where(TestUser.class).one();
        Assert.assertNotNull(user);
        Assert.assertEquals(79.9, user.getWeight(), 0.0);
        Assert.assertEquals(BigInteger.valueOf(1000L), user.getBigInt());
        Assert.assertEquals(BigDecimal.valueOf(999.9), user.getBigDec());
        Assert.assertEquals(TestUser.Role.USER, user.getRole());
        Assert.assertEquals(lastModified, user.getLastModified());
    }

    @Test
    public void testSave() throws Exception {
        final TestUser entry = new TestUser()
                .setName("John")
                .setWeight(79.9)
                .setBigInt(BigInteger.valueOf(100L))
                .setBigDec(BigDecimal.valueOf(99.9))
                .setRole(TestUser.Role.ADMIN)
                .setLastModified(DateTime.now());
        SQLite.save(entry);
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(entry.getName(), DatabaseUtils.getString(cursor, "name"));
        Assert.assertEquals(entry.getWeight(), DatabaseUtils.getDouble(cursor, "weight"), 0d);
        Assert.assertEquals(entry.getBigInt(), DatabaseUtils.getBigInt(cursor, "big_int"));
        Assert.assertEquals(entry.getBigDec(), DatabaseUtils.getBigDec(cursor, "big_dec"));
        Assert.assertEquals(entry.getRole(), DatabaseUtils.getEnum(cursor, "role", TestUser.Role.class));
        Assert.assertEquals(entry.getLastModified(), DatabaseUtils.getDateTime(cursor, "last_modified"));
        cursor.close();
    }

    @Test
    public void testSave5K() throws Exception {
        SQLite.beginTransaction();
        for (int i = 1; i <= 5000; ++i) {
            SQLite.save(new TestUser().setName("User #" + i));
        }
        SQLite.endTransaction(true);
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(5000, cursor.getCount());
        cursor.close();
    }

}
