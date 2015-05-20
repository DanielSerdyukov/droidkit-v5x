package droidkit.test;

import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.io.IOUtils;
import droidkit.sqlite.SQLite;
import droidkit.sqlite.SQLiteConfig;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SQLiteTest {

    public static final String[] TABLES = new String[]{"'users'"};

    static {
        SQLiteConfig.setDatabaseVersion(6);
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(InstrumentationRegistry.getContext());
    }

    @Test
    public void testSchema() throws Exception {
        final Cursor cursor = mSQLite.rawQuery("SELECT name FROM sqlite_master" +
                " WHERE type='table'" +
                " AND name IN(" + TextUtils.join(", ", TABLES) + ")");
        try {
            Assert.assertEquals(TABLES.length, cursor.getCount());
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

}
