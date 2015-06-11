package droidkit.sqlite;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteTest {

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(InstrumentationRegistry.getContext());
    }

    @Test
    public void testUriOf() throws Exception {

    }

    @Test
    public void testSQLiteVersion() throws Exception {
        Assert.assertEquals("3.8.10.2", mSQLite.getClient().simpleQueryForString("SELECT sqlite_version();"));
    }

    @Test
    public void testSchema() throws Exception {
        Assert.assertEquals("users", mSQLite.getClient().simpleQueryForString("SELECT name FROM sqlite_master" +
                " WHERE type='table'" +
                " AND name='users';"));
    }

    @Test
    public void testInsertSelect() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('John');");
        Assert.assertEquals("John", mSQLite.getClient().simpleQueryForString("SELECT name FROM users;"));
    }

    @Test
    public void testUnicode() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('Вася Пупкин');");
        mSQLite.getClient().simpleQueryForString("SELECT name FROM users WHERE name LIKE 'ва%';");
    }

    @Test
    public void testUnicodeLower() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('Вася Пупкин');");
        Assert.assertEquals("Вася Пупкин", mSQLite.getClient().simpleQueryForString(
                "SELECT name FROM users WHERE LOWER(name) LIKE 'ва%';"));
    }

    @Test
    public void testCreate() throws Exception {
        final SQLiteUser user = mSQLite.create(SQLiteUser.class, true);
        user.setName("John");
        Assert.assertEquals("John", mSQLite.getClient()
                .simpleQueryForString("SELECT name FROM users WHERE _id = ?", user.mId));
    }

    @Test
    public void testSave() throws Exception {
        final SQLiteUser expected = new SQLiteUser();
        expected.setName("John");
        expected.setAge(25);
        Log.e("", "testSave " + expected);
        mSQLite.save(expected, true);
        final SQLiteUser actual = mSQLite.where(SQLiteUser.class)
                .withId(expected.mId);
        Assert.assertNotNull(actual);
        Assert.assertEquals("John", actual.getName());
        Assert.assertEquals(25, actual.getAge());
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}