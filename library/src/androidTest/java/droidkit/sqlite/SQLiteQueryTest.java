package droidkit.sqlite;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQueryTest extends SQLiteTestCase {

    @Test
    public void testDistinct() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).
                distinct();
        Assert.assertEquals("SELECT DISTINCT * FROM users", query.toString());
        Assert.assertEquals(USERS.length, query.list().size());
    }

    @Test
    public void testEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class)
                .equalTo("name", "James");
        Assert.assertEquals("SELECT * FROM users WHERE name = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"James"}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("James", users.get(0).getName());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).notEqualTo("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age <> ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertNotSame(25, user.getAge());
        }
    }

    @Test
    public void testLessThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).lessThan("weight", 50);
        Assert.assertEquals("SELECT * FROM users WHERE weight < ?", query.toString());
        Assert.assertArrayEquals(new Object[]{50}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(4, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getWeight() < 50);
        }
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).lessThanOrEqualTo("weight", 70.5);
        Assert.assertEquals("SELECT * FROM users WHERE weight <= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{70.5}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getWeight() <= 70.5);
        }
    }

    @Test
    public void testGreaterThan() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).greaterThan("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age > ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getAge() > 25);
        }
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).greaterThanOrEqualTo("age", 25);
        Assert.assertEquals("SELECT * FROM users WHERE age >= ?", query.toString());
        Assert.assertArrayEquals(new Object[]{25}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(3, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getAge() >= 25);
        }
    }

    @Test
    public void testLike() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).like("name", "Ja%");
        Assert.assertEquals("SELECT * FROM users WHERE name LIKE ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"Ja%"}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertTrue(user.getName().startsWith("Ja"));
        }
    }

    @Test
    public void testBetween() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).between("age", 22, 28);
        Assert.assertEquals("SELECT * FROM users WHERE age BETWEEN ? AND ?", query.toString());
        Assert.assertArrayEquals(new Object[]{22, 28}, query.bindArgs());
        Assert.assertEquals(4, query.list().size());
    }

    @Test
    public void testIsTrue() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).isTrue("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE enabled = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{1}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(8, users.size());
        Log.e("=====", users.toString());
        for (final SQLiteUser user : users) {
            Log.e("=====", user.toString());
            Assert.assertTrue(user.isEnabled());
        }
    }

    @Test
    public void testIsFalse() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).isFalse("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE enabled = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{0}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertFalse(user.isEnabled());
        }
    }

    @Test
    public void testIsNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).isNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name IS NULL", query.toString());
    }

    @Test
    public void testNotNull() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).notNull("name");
        Assert.assertEquals("SELECT * FROM users WHERE name NOT NULL", query.toString());
    }

    @Test
    public void testAppendWhere() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).appendWhere("_id IN(?, ?, ?)", 1, 2, 3);
        Assert.assertEquals("SELECT * FROM users WHERE _id IN(?, ?, ?)", query.toString());
        Assert.assertArrayEquals(new Object[]{1, 2, 3}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(3, users.size());
    }

    @Test
    public void testGroupBy() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).groupBy("_id");
        Assert.assertEquals("SELECT * FROM users GROUP BY _id", query.toString());
        getSQLite().rawQuery(query.toString());
    }

    @Test
    public void testHaving() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class)
                .groupBy("name")
                .having("age > ?", 20);
        Assert.assertEquals("SELECT * FROM users GROUP BY name HAVING age > ?", query.toString());
        Assert.assertArrayEquals(new Object[]{20}, query.bindArgs());
        getSQLite().rawQuery(query.toString());
    }

    @Test
    public void testOrderBy() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).orderBy("name");
        Assert.assertEquals("SELECT * FROM users ORDER BY name ASC", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(USERS.length, users.size());
        Assert.assertEquals("Abigail", users.get(0).getName());
    }

    @Test
    public void testOrderByDesc() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).orderBy("name", false);
        Assert.assertEquals("SELECT * FROM users ORDER BY name DESC", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(USERS.length, users.size());
        Assert.assertEquals("Olivia", users.get(0).getName());
    }

    @Test
    public void testLimit() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).limit(5);
        Assert.assertEquals("SELECT * FROM users LIMIT 5", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(5, users.size());
        Assert.assertEquals("Liam", users.get(0).getName());
        Assert.assertEquals("Ethan", users.get(4).getName());
    }

    @Test
    public void testLimitOffset() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class).offsetLimit(5, 2);
        Assert.assertEquals("SELECT * FROM users LIMIT 5, 2", query.toString());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("Mia", users.get(0).getName());
        Assert.assertEquals("Alexander", users.get(1).getName());
    }

    @Test
    public void testAnd() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class)
                .equalTo("name", "Mia")
                .and()
                .equalTo("age", 23);
        Assert.assertEquals("SELECT * FROM users WHERE name = ? AND age = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"Mia", 23}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(1, users.size());
        Assert.assertEquals("Mia", users.get(0).getName());
    }

    @Test
    public void testOr() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class)
                .equalTo("name", "Mia")
                .or()
                .equalTo("age", 30);
        Assert.assertEquals("SELECT * FROM users WHERE name = ? OR age = ?", query.toString());
        Assert.assertArrayEquals(new Object[]{"Mia", 30}, query.bindArgs());
        final List<SQLiteUser> users = query.list();
        Assert.assertEquals(2, users.size());
        Assert.assertEquals("Mia", users.get(0).getName());
        Assert.assertEquals("Alexander", users.get(1).getName());
    }

    @Test
    public void testComplexWhere() throws Exception {
        final SQLiteQuery<SQLiteUser> query = getSQLite().where(SQLiteUser.class)
                .equalTo("name", "Mia")
                .and()
                .beginGroup()
                .equalTo("age", 20)
                .and()
                .lessThan("weight", 50d)
                .endGroup()
                .or()
                .isTrue("enabled");
        Assert.assertEquals("SELECT * FROM users WHERE name = ? AND (age = ? AND weight < ?) OR enabled = ?",
                query.toString());
        Assert.assertArrayEquals(new Object[]{"Mia", 20, 50d, 1}, query.bindArgs());
        query.list();
    }

    @Test
    public void testList() throws Exception {
        final List<SQLiteUser> users = getSQLite().where(SQLiteUser.class).list();
        Assert.assertEquals(USERS.length, users.size());
        for (int i = 0; i < USERS.length; ++i) {
            Assert.assertEquals(USERS[i].mName, users.get(i).getName());
        }
    }

    @Test
    public void testOne() throws Exception {
        final SQLiteUser user = getSQLite().where(SQLiteUser.class).one();
        Assert.assertNotNull(user);
        Assert.assertEquals("Liam", user.getName());
    }

    @Test
    public void testRemove() throws Exception {
        final int affectedRows = getSQLite().where(SQLiteUser.class)
                .equalTo("name", "Mia")
                .remove();
        Assert.assertEquals(1, affectedRows);
        final List<SQLiteUser> users = getSQLite().where(SQLiteUser.class).list();
        for (final SQLiteUser user : users) {
            Assert.assertFalse("Mia".equals(user.getName()));
        }
    }

    @Test
    public void testMin() throws Exception {
        Assert.assertEquals(40.4, getSQLite().where(SQLiteUser.class).min("weight").doubleValue(), .01d);
    }

    @Test
    public void testMax() throws Exception {
        Assert.assertEquals(80.75, getSQLite().where(SQLiteUser.class).max("weight").doubleValue(), .01d);
    }

    @Test
    public void testSum() throws Exception {
        double sum = 0;
        for (final SQLiteUser user : USERS) {
            sum += user.getWeight();
        }
        Assert.assertEquals(sum, getSQLite().where(SQLiteUser.class).sum("weight").doubleValue(), .01d);
    }

    @Test
    public void testCount() throws Exception {
        Assert.assertEquals(2, getSQLite().where(SQLiteUser.class).isFalse("enabled").count("_id").intValue());
    }

    @Test
    public void testSetter() throws Exception {
        final SQLiteUser user = getSQLite().where(SQLiteUser.class).equalTo("name", "Mia").one();
        Assert.assertNotNull(user);
        user.setName("Maya");
        Assert.assertEquals("Maya", user.getName());
        Assert.assertEquals("Maya", getSQLite().getClient()
                .simpleQueryForString("SELECT name FROM users WHERE _id = ?", user.mId));
    }

}