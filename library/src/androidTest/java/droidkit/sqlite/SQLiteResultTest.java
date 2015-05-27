package droidkit.sqlite;

import android.database.Cursor;
import android.os.StrictMode;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import droidkit.database.CursorUtils;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteResultTest {

    static {
        SQLite.useInMemoryDb();
        SQLite.useCaseSensitiveLike();
        StrictMode.enableDefaults();
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(InstrumentationRegistry.getContext());
        mSQLite.beginTransaction();
        for (final SQLiteUser user : SQLiteQueryTest.USERS) {
            mSQLite.execSQL("INSERT INTO users(name, age, weight, avatar) VALUES(?, ?, ?, ?)",
                    user.mName, user.mAge, user.mWeight, user.mAvatar);
        }
        mSQLite.endTransaction(true);
    }

    @Test
    public void testSize() throws Exception {
        final Cursor cursor = mSQLite.rawQuery("SELECT * FROM users;");
        try {
            Assert.assertEquals(cursor.getCount(), mSQLite.where(SQLiteUser.class).list().size());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testRemove() throws Exception {
        final List<SQLiteUser> users = mSQLite.where(SQLiteUser.class).list();
        final SQLiteUser removed = users.remove(4);
        Assert.assertEquals("Ethan", removed.getName());
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertFalse("Ethan".equals(user.getName()));
        }
        final Cursor cursor = mSQLite.rawQuery("SELECT * FROM users;");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Assert.assertFalse("Ethan".equals(CursorUtils.getString(cursor, "name")));
                } while (cursor.moveToNext());
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @After
    public void tearDown() throws Exception {
        mSQLite.execSQL("DELETE FROM users;");
    }

}