package droidkit.sqlite;

import android.database.Cursor;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import droidkit.database.DatabaseUtils;
import droidkit.io.IOUtils;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteResultTest extends SQLiteTestCase {

    @Test
    public void testSize() throws Exception {
        final Cursor cursor = getSQLite().rawQuery("SELECT * FROM users;");
        try {
            Assert.assertEquals(cursor.getCount(), getSQLite().where(SQLiteUser.class).list().size());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testAdd() throws Exception {
        final List<SQLiteUser> users = getSQLite().where(SQLiteUser.class).list();
        Assert.assertEquals(USERS.length, users.size());
        final SQLiteUser user = new SQLiteUser();
        user.setName("Joe");
        user.setAge(40);
        users.add(user);
        Assert.assertEquals(11, users.size());
        final Cursor cursor = getSQLite().rawQuery("SELECT * FROM users;");
        try {
            Assert.assertTrue(cursor.moveToFirst());
            Assert.assertEquals(11, cursor.getCount());
            boolean actual;
            do {
                actual = "Joe".equals(DatabaseUtils.getString(cursor, "name"));
            } while (!actual && cursor.moveToNext());
            Assert.assertTrue(actual);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    @Test
    public void testRemove() throws Exception {
        final List<SQLiteUser> users = getSQLite().where(SQLiteUser.class).list();
        final SQLiteUser removed = users.remove(4);
        Assert.assertEquals("Ethan", removed.getName());
        Assert.assertEquals(9, users.size());
        for (final SQLiteUser user : users) {
            Assert.assertFalse("Ethan".equals(user.getName()));
        }
        final Cursor cursor = getSQLite().rawQuery("SELECT * FROM users;");
        try {
            if (cursor.moveToFirst()) {
                do {
                    Assert.assertFalse("Ethan".equals(DatabaseUtils.getString(cursor, "name")));
                } while (cursor.moveToNext());
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

}