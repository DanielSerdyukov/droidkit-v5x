package droidkit.sqlite;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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

    static {
        SQLite.useInMemoryDb();
        SQLite.useCaseSensitiveLike();
    }

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
        Assert.assertEquals("3.8.10.2", mSQLite.simpleQueryForString("SELECT sqlite_version();"));
    }

    @Test
    public void testSchema() throws Exception {
        Assert.assertEquals("users", mSQLite.simpleQueryForString("SELECT name FROM sqlite_master" +
                " WHERE type='table'" +
                " AND name='users';"));
    }

    @Test
    public void testInsertSelect() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('John');");
        Assert.assertEquals("John", mSQLite.simpleQueryForString("SELECT name FROM users;"));
    }

    @Test
    public void testInsertUnicode() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('Вася Пупкин');");
        Assert.assertEquals("Вася Пупкин", mSQLite.simpleQueryForString(
                "SELECT name FROM users WHERE name LIKE 'Ва%';"));
    }

    @Test
    public void testInsertUnicodeLower() throws Exception {
        mSQLite.execSQL("INSERT INTO users(name) VALUES('Вася Пупкин');");
        Assert.assertEquals("Вася Пупкин", mSQLite.simpleQueryForString(
                "SELECT name FROM users WHERE LOWER(name) LIKE 'ва%';"));
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}