package droidkit.test.sqlite;

import android.database.Cursor;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.io.IOUtils;
import droidkit.sqlite.SQLite;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest {

    public static final String[] TABLES = new String[]{"'users'"};

    static {
        SQLite.useInMemoryDb();
    }

    private SQLite mSQLite;

    @Before
    public void setUp() throws Exception {
        mSQLite = SQLite.of(RuntimeEnvironment.application);
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