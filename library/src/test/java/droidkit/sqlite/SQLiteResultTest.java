package droidkit.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import unit.test.mock.TestUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteResultTest extends SQLiteTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final ContentValues values = new ContentValues();
        for (int i = 1; i <= 10; ++i) {
            values.clear();
            values.put("name", "User #" + i);
            values.put("weight", 50.5 + i);
            getProvider().insert(SQLite.uriOf(TestUser.class), values);
        }
    }

    @Test
    public void testList() throws Exception {
        final List<TestUser> list = SQLite.where(TestUser.class).list();
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(list.size(), cursor.getCount());
        do {
            Assert.assertEquals(list.get(cursor.getPosition()).getName(),
                    DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testAdd() throws Exception {
        final List<TestUser> list = SQLite.where(TestUser.class).list();
        final TestUser newEntry = new TestUser().setName("Added Entry");
        Assert.assertTrue(list.add(newEntry));
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, "name = ?",
                new String[]{newEntry.getName()}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(newEntry.getId(), DatabaseUtils.getLong(cursor, "_id"));
        cursor.close();
    }

    @Test
    public void testRemove() throws Exception {
        final List<TestUser> list = SQLite.where(TestUser.class).list();
        for (int i = 0; i < list.size(); ++i) {
            Assert.assertEquals("User #" + (i + 1), list.get(i).getName());
        }
        final TestUser removed = list.remove(5);
        Assert.assertEquals("User #6", removed.getName());
        final Cursor cursor = getProvider().query(SQLite.uriOf(TestUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertNotEquals(removed.getName(), DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testEqualTo() throws Exception {
        Assert.assertEquals(1, SQLite.where(TestUser.class)
                .equalTo("name", "User #3")
                .list().size());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(9, SQLite.where(TestUser.class)
                .notEqualTo("name", "User #3")
                .list().size());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(9, SQLite.where(TestUser.class)
                .lessThan("weight", 60.5)
                .list().size());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(5, SQLite.where(TestUser.class)
                .lessThanOrEqualTo("weight", 55.5)
                .list().size());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(5, SQLite.where(TestUser.class)
                .greaterThan("weight", 55.5)
                .list().size());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(1, SQLite.where(TestUser.class)
                .greaterThanOrEqualTo("weight", 60.5)
                .list().size());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(1, SQLite.where(TestUser.class)
                .like("name", "%#6")
                .list().size());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(5, SQLite.where(TestUser.class)
                .between("weight", 56.5, 60.5)
                .list().size());
    }

    @Test
    public void testWhereGroup() throws Exception {
        Assert.assertEquals(3, SQLite.where(TestUser.class)
                .equalTo("name", "User #2")
                .or()
                .beginGroup()
                .equalTo("name", "User #3")
                .or()
                .equalTo("name", "User #4")
                .endGroup()
                .list().size());
    }

    @Test
    public void testMin() throws Exception {
        Assert.assertEquals(51.5, SQLite.where(TestUser.class).min("weight").doubleValue(), 0.0);
    }

    @Test
    public void testMax() throws Exception {
        Assert.assertEquals(60.5, SQLite.where(TestUser.class).max("weight").doubleValue(), 0.0);
    }

    @Test
    public void testSum() throws Exception {
        Assert.assertEquals(560, SQLite.where(TestUser.class).sum("weight").intValue());
    }

    @Test
    public void testCount() throws Exception {
        Assert.assertEquals(4, SQLite.where(TestUser.class)
                .lessThan("weight", 55.5)
                .count("weight")
                .intValue());
    }

}
