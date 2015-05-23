package droidkit.sqlite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest {

    static {
        SQLite.useInMemoryDb();
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(RuntimeEnvironment.application);
    }

    @Test
    public void testEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).equalTo("name", "Joe");
        Assert.assertEquals("SELECT * FROM users WHERE name = ?", query.toString());
        Assert.assertArrayEquals(new String[]{"Joe"}, query.bindArgs());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).notEqualTo("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age <> ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
    }

    @Test
    public void testLessThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).lessThan("weight", 50);
        Assert.assertEquals("SELECT * FROM users WHERE weight < ?", query.toString());
        Assert.assertArrayEquals(new Object[]{50}, query.bindArgs());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).lessThanOrEqualTo("weight", 75.5);
        Assert.assertEquals("SELECT * FROM users WHERE weight <= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{75.5}, query.bindArgs());
    }

    @Test
    public void testGreaterThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).greaterThan("age", 20);
        Assert.assertEquals("SELECT * FROM users WHERE age > ?", query.toString());
        Assert.assertArrayEquals(new Object[]{20}, query.bindArgs());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).greaterThanOrEqualTo("age", 20);
        Assert.assertEquals("SELECT * FROM users WHERE age >= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{20}, query.bindArgs());
    }

    @Test
    public void testLike() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).like("name", "Ja%");
        Assert.assertEquals("SELECT * FROM users WHERE name LIKE ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"'Ja%'"}, query.bindArgs());
    }

    @Test
    public void testBetween() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).between("age", 20, 30);
        Assert.assertEquals("SELECT * FROM users WHERE age BETWEEN ? AND ?", query.toString());
        Assert.assertArrayEquals(new Object[]{20, 30}, query.bindArgs());
    }

    @Test
    public void testIsTrue() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isTrue("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE enabled = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{1}, query.bindArgs());
    }

    @Test
    public void testIsFalse() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isFalse("blocked");
        Assert.assertEquals("SELECT * FROM users WHERE blocked = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{0}, query.bindArgs());
    }

    @Test
    public void testIsNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).isNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name IS NULL", query.toString());
    }

    @Test
    public void testNotNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = mSQLite.where(SQLiteUser.class).notNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name NOT NULL", query.toString());
    }

}