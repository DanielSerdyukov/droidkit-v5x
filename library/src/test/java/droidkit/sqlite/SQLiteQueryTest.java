package droidkit.sqlite;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import unit.test.mock.TestUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest extends SQLiteTestCase {

    @Test
    public void testEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(TestUser.class)
                .equalTo("a", "John").toString());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a <> ?", SQLite.where(TestUser.class)
                .notEqualTo("a", "John").toString());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(" WHERE a < ?", SQLite.where(TestUser.class)
                .lessThan("a", 100).toString());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a <= ?", SQLite.where(TestUser.class)
                .lessThanOrEqualTo("a", 100).toString());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(" WHERE a > ?", SQLite.where(TestUser.class)
                .greaterThan("a", 100).toString());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a >= ?", SQLite.where(TestUser.class)
                .greaterThanOrEqualTo("a", 100).toString());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(" WHERE a LIKE ?", SQLite.where(TestUser.class)
                .like("a", "Jo%").toString());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(" WHERE a BETWEEN ? AND ?", SQLite.where(TestUser.class)
                .between("a", 100, 200).toString());
    }

    @Test
    public void testIsTrue() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(TestUser.class)
                .isTrue("a").toString());
    }

    @Test
    public void testIsFalse() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(TestUser.class)
                .isFalse("a").toString());
    }

    @Test
    public void testIsNull() throws Exception {
        Assert.assertEquals(" WHERE a IS NULL", SQLite.where(TestUser.class)
                .isNull("a").toString());
    }

    @Test
    public void testNotNull() throws Exception {
        Assert.assertEquals(" WHERE a NOT NULL", SQLite.where(TestUser.class)
                .notNull("a").toString());
    }

    @Test
    public void testAppendWhere() throws Exception {
        Assert.assertEquals(" WHERE a IN SELECT rowid FROM b", SQLite.where(TestUser.class)
                .appendWhere("a IN SELECT rowid FROM b").toString());
    }

    @Test
    public void testAnd() throws Exception {
        Assert.assertEquals(" WHERE a = ? AND b <> ?", SQLite.where(TestUser.class)
                .equalTo("a", 1)
                .and()
                .notEqualTo("b", 2)
                .toString());
    }

    @Test
    public void testOr() throws Exception {
        Assert.assertEquals(" WHERE a = ? OR b <> ?", SQLite.where(TestUser.class)
                .equalTo("a", 1)
                .or()
                .notEqualTo("b", 2)
                .toString());
    }

    @Test
    public void testWhereGroup() throws Exception {
        Assert.assertEquals(" WHERE a = ? AND (b <> ? OR c > ?)", SQLite.where(TestUser.class)
                .equalTo("a", 1)
                .and()
                .beginGroup()
                .notEqualTo("b", 2)
                .or()
                .greaterThan("c", 3)
                .endGroup()
                .toString());
    }

}