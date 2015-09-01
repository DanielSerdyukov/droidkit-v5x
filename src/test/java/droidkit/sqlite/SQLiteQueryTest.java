package droidkit.sqlite;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.abc.Bar;
import droidkit.sqlite.util.SQLiteTestEnv;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest {

    @Before
    public void setUp() throws Exception {
        SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id = ?", new SQLiteQuery<>(Bar.class)
                .equalTo("_id", 123)
                .toString());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id <> ?", new SQLiteQuery<>(Bar.class)
                .notEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(" WHERE _id < ?", new SQLiteQuery<>(Bar.class)
                .lessThan("_id", 123)
                .toString());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id <= ?", new SQLiteQuery<>(Bar.class)
                .lessThanOrEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(" WHERE _id > ?", new SQLiteQuery<>(Bar.class)
                .greaterThan("_id", 123)
                .toString());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id >= ?", new SQLiteQuery<>(Bar.class)
                .greaterThanOrEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(" WHERE _id LIKE ?", new SQLiteQuery<>(Bar.class)
                .like("_id", "assert%")
                .toString());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(" WHERE _id BETWEEN ? AND ?", new SQLiteQuery<>(Bar.class)
                .between("_id", 1, 2)
                .toString());
    }

    @Test
    public void testIsNull() throws Exception {
        Assert.assertEquals(" WHERE _id IS NULL", new SQLiteQuery<>(Bar.class)
                .isNull("_id")
                .toString());
    }

    @Test
    public void testNotNull() throws Exception {
        Assert.assertEquals(" WHERE _id NOT NULL", new SQLiteQuery<>(Bar.class)
                .notNull("_id")
                .toString());
    }

    @Test
    public void testInSelect() throws Exception {
        Assert.assertEquals(" WHERE _id IN(SELECT id FROM bar)", new SQLiteQuery<>(Bar.class)
                .inSelect("_id", "SELECT id FROM bar")
                .toString());
        Assert.assertEquals(" WHERE _id IN(?, ?)", new SQLiteQuery<>(Bar.class)
                .inSelect("_id", Arrays.asList("one", "two"))
                .toString());
    }

    @Test
    public void testAnd() throws Exception {
        Assert.assertEquals(" WHERE _id = ? AND name <> ?", new SQLiteQuery<>(Bar.class)
                .equalTo("_id", 1)
                .and()
                .notEqualTo("name", "test")
                .toString());
    }

    @Test
    public void testOr() throws Exception {
        Assert.assertEquals(" WHERE _id = ? OR name <> ?", new SQLiteQuery<>(Bar.class)
                .equalTo("_id", 1)
                .or()
                .notEqualTo("name", "test")
                .toString());
    }

    @Test
    public void testGroupWhere() throws Exception {
        Assert.assertEquals(" WHERE (_id = ? AND name <> ?) OR (_id > ? AND name = ?)", new SQLiteQuery<>(Bar.class)
                .beginGroup()
                .equalTo("_id", 1)
                .and()
                .notEqualTo("name", "test")
                .endGroup()
                .or()
                .beginGroup()
                .greaterThan("_id", 1)
                .and()
                .equalTo("name", "assert")
                .endGroup()
                .toString());
    }

}