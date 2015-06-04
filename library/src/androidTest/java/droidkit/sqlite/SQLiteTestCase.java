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
public class SQLiteTestCase {

    static final SQLiteUser[] USERS = new SQLiteUser[]{
            SQLiteUser.forTest("Liam", 20, 70.5, new byte[]{0, 0, 0, 0, 0}),
            SQLiteUser.forTest("Olivia", 22, 60.5, new byte[]{0, 1, 0, 0, 0}),
            SQLiteUser.forTest("Jacob", 25, 55.8, new byte[]{0, 0, 1, 0, 0}, SQLiteUser.Role.ADMIN),
            SQLiteUser.forTest("Isabella", 21, 45.0, new byte[]{0, 0, 0, 1, 0}),
            SQLiteUser.forTest("Ethan", 28, 80.75, new byte[]{0, 0, 0, 0, 1}),
            SQLiteUser.forTest("Mia", 23, 50.3, new byte[]{1, 1, 0, 0, 0}),
            SQLiteUser.forTest("Alexander", 30, 66.7, new byte[]{0, 1, 1, 0, 0}),
            SQLiteUser.forTest("Abigail", 19, 45.2, new byte[]{0, 0, 1, 1, 0}),
            SQLiteUser.forTest("James", 15, 40.4, new byte[]{0, 0, 0, 1, 1}),
            SQLiteUser.forTest("Charlotte", 18, 48.1, new byte[]{1, 1, 1, 0, 0}),
    };

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(InstrumentationRegistry.getContext());
        mSQLite.beginTransaction();
        for (final SQLiteUser user : USERS) {
            mSQLite.execSQL("INSERT INTO users(name, age, weight, avatar, enabled, role) VALUES(?, ?, ?, ?, ?, ?)",
                    user.mName, user.mAge, user.mWeight, user.mAvatar, (user.mAge > 18), user.mRole.name());
        }
        mSQLite.endTransaction(true);
    }

    @Test
    public void testPreconditions() throws Exception {
        Assert.assertFalse(mSQLite.where(SQLiteUser.class).list().isEmpty());
    }

    public SQLite getSQLite() {
        return mSQLite;
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}
