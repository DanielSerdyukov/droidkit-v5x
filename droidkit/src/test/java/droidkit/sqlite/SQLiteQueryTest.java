package droidkit.sqlite;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.Standard;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest {

    @Test
    public void testEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id = ?", new SQLiteQuery<>(Standard.class)
                .equalTo("_id", 123)
                .toString());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id <> ?", new SQLiteQuery<>(Standard.class)
                .notEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(" WHERE _id < ?", new SQLiteQuery<>(Standard.class)
                .lessThan("_id", 123)
                .toString());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id <= ?", new SQLiteQuery<>(Standard.class)
                .lessThanOrEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(" WHERE _id > ?", new SQLiteQuery<>(Standard.class)
                .greaterThan("_id", 123)
                .toString());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE _id >= ?", new SQLiteQuery<>(Standard.class)
                .greaterThanOrEqualTo("_id", 123)
                .toString());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(" WHERE _id LIKE ?", new SQLiteQuery<>(Standard.class)
                .like("_id", "assert%")
                .toString());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(" WHERE _id BETWEEN ? AND ?", new SQLiteQuery<>(Standard.class)
                .between("_id", 1, 2)
                .toString());
    }

    @Test
    public void testIsNull() throws Exception {
        Assert.assertEquals(" WHERE _id IS NULL", new SQLiteQuery<>(Standard.class)
                .isNull("_id")
                .toString());
    }

    @Test
    public void testNotNull() throws Exception {
        Assert.assertEquals(" WHERE _id NOT NULL", new SQLiteQuery<>(Standard.class)
                .notNull("_id")
                .toString());
    }

    @Test
    public void testAnd() throws Exception {
        Assert.assertEquals(" WHERE _id = ? AND name <> ?", new SQLiteQuery<>(Standard.class)
                .equalTo("_id", 1)
                .and()
                .notEqualTo("name", "test")
                .toString());
    }

    @Test
    public void testOr() throws Exception {
        Assert.assertEquals(" WHERE _id = ? OR name <> ?", new SQLiteQuery<>(Standard.class)
                .equalTo("_id", 1)
                .or()
                .notEqualTo("name", "test")
                .toString());
    }

    @Test
    public void testGroupWhere() throws Exception {
        Assert.assertEquals(" WHERE (_id = ? AND name <> ?) OR (_id > ? AND name = ?)",
                new SQLiteQuery<>(Standard.class)
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