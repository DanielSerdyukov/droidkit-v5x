package droidkit.sqlite;

import android.content.pm.ProviderInfo;

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
import unit.test.mock.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteQueryTest {

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
    public void testEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(SQLiteUser.class)
                .equalTo("a", "John").toString());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a <> ?", SQLite.where(SQLiteUser.class)
                .notEqualTo("a", "John").toString());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(" WHERE a < ?", SQLite.where(SQLiteUser.class)
                .lessThan("a", 100).toString());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a <= ?", SQLite.where(SQLiteUser.class)
                .lessThanOrEqualTo("a", 100).toString());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(" WHERE a > ?", SQLite.where(SQLiteUser.class)
                .greaterThan("a", 100).toString());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(" WHERE a >= ?", SQLite.where(SQLiteUser.class)
                .greaterThanOrEqualTo("a", 100).toString());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(" WHERE a LIKE ?", SQLite.where(SQLiteUser.class)
                .like("a", "Jo%").toString());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(" WHERE a BETWEEN ? AND ?", SQLite.where(SQLiteUser.class)
                .between("a", 100, 200).toString());
    }

    @Test
    public void testIsTrue() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(SQLiteUser.class)
                .isTrue("a").toString());
    }

    @Test
    public void testIsFalse() throws Exception {
        Assert.assertEquals(" WHERE a = ?", SQLite.where(SQLiteUser.class)
                .isFalse("a").toString());
    }

    @Test
    public void testIsNull() throws Exception {
        Assert.assertEquals(" WHERE a IS NULL", SQLite.where(SQLiteUser.class)
                .isNull("a").toString());
    }

    @Test
    public void testNotNull() throws Exception {
        Assert.assertEquals(" WHERE a NOT NULL", SQLite.where(SQLiteUser.class)
                .notNull("a").toString());
    }

    @Test
    public void testAppendWhere() throws Exception {
        Assert.assertEquals(" WHERE a IN SELECT rowid FROM b", SQLite.where(SQLiteUser.class)
                .appendWhere("a IN SELECT rowid FROM b").toString());
    }

    @Test
    public void testAnd() throws Exception {
        Assert.assertEquals(" WHERE a = ? AND b <> ?", SQLite.where(SQLiteUser.class)
                .equalTo("a", 1)
                .and()
                .notEqualTo("b", 2)
                .toString());
    }

    @Test
    public void testOr() throws Exception {
        Assert.assertEquals(" WHERE a = ? OR b <> ?", SQLite.where(SQLiteUser.class)
                .equalTo("a", 1)
                .or()
                .notEqualTo("b", 2)
                .toString());
    }

    @Test
    public void testWhereGroup() throws Exception {
        Assert.assertEquals(" WHERE a = ? AND (b <> ? OR c > ?)", SQLite.where(SQLiteUser.class)
                .equalTo("a", 1)
                .and()
                .beginGroup()
                .notEqualTo("b", 2)
                .or()
                .greaterThan("c", 3)
                .endGroup()
                .toString());
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}