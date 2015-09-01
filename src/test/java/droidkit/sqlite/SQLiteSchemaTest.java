package droidkit.sqlite;

import android.database.Cursor;
import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.io.IOUtils;
import droidkit.sqlite.bean.Standard;
import droidkit.sqlite.util.SQLiteTestEnv;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteSchemaTest {

    private static final Map<String, String> SCHEMA = new HashMap<>();

    static {
        SCHEMA.put("android_metadata", "CREATE TABLE android_metadata (locale TEXT)");
        SCHEMA.put("standard", "CREATE TABLE standard(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                " long INTEGER, int INTEGER, short INTEGER, string TEXT, boolean INTEGER," +
                " double REAL, float REAL, big_decimal REAL, big_integer INTEGER, bytes BLOB," +
                " role TEXT NOT NULL, date INTEGER, UNIQUE(string, boolean) ON CONFLICT REPLACE)");
        SCHEMA.put("idx_standard_role", "CREATE INDEX idx_standard_role ON standard(role)");
        SCHEMA.put("idx_standard_date", "CREATE INDEX idx_standard_date ON standard(date)");
        SCHEMA.put("foo", "CREATE TABLE foo(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, text TEXT)");
        SCHEMA.put("bar", "CREATE TABLE bar(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, text TEXT)");
        SCHEMA.put("baz", "CREATE TABLE baz(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, text TEXT," +
                " bar_id INTEGER)");
        SCHEMA.put("delete_baz_after_bar", "CREATE TRIGGER delete_baz_after_bar" +
                " AFTER DELETE ON bar" +
                " FOR EACH ROW" +
                " BEGIN" +
                " DELETE FROM baz WHERE bar_id = OLD._id;" +
                " END");
        SCHEMA.put("update_baz_after_bar", "CREATE TRIGGER update_baz_after_bar" +
                " AFTER UPDATE ON bar" +
                " FOR EACH ROW" +
                " BEGIN" +
                " UPDATE baz SET bar_id = NEW._id WHERE bar_id = OLD._id;" +
                " END");
        SCHEMA.put("idx_baz_bar_id", "CREATE INDEX idx_baz_bar_id ON baz(bar_id)");
        SCHEMA.put("foo_bar", "CREATE TABLE foo_bar(foo_id INTEGER REFERENCES foo(_id)" +
                " ON DELETE CASCADE ON UPDATE CASCADE, bar_id INTEGER REFERENCES bar(_id)" +
                " ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (foo_id, bar_id) ON CONFLICT IGNORE)");
        SCHEMA.put("bar_baz", "CREATE TABLE bar_baz(bar_id INTEGER REFERENCES bar(_id)" +
                " ON DELETE CASCADE ON UPDATE CASCADE, baz_id INTEGER REFERENCES baz(_id)" +
                " ON DELETE CASCADE ON UPDATE CASCADE, UNIQUE (bar_id, baz_id) ON CONFLICT IGNORE)");
        SCHEMA.put("qux", "CREATE TABLE qux(_id INTEGER PRIMARY KEY ON CONFLICT REPLACE, text TEXT," +
                " foo_id INTEGER REFERENCES foo(_id) ON DELETE CASCADE ON UPDATE CASCADE)");
    }

    @Before
    public void setUp() throws Exception {
        SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testResolveTable() throws Exception {
        Assert.assertEquals(Standard.TABLE, SQLiteSchema.resolveTable(Standard.class));
    }

    @Test
    public void testResolveUri() throws Exception {
        final Uri expected = new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.APPLICATION_ID)
                .path(Standard.TABLE)
                .build();
        final Uri actual = SQLiteSchema.resolveUri(Standard.class);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSchema() throws Exception {
        SQLite.execute(new Func1<SQLiteClient, Void>() {
            @Override
            public Void call(SQLiteClient client) {
                final Cursor cursor = client.query("SELECT name, sql FROM sqlite_master WHERE sql NOT NULL;");
                Assert.assertTrue(cursor.moveToFirst());
                Assert.assertEquals(SCHEMA.size(), cursor.getCount());
                do {
                    Assert.assertEquals(SCHEMA.get(cursor.getString(cursor.getColumnIndex("name"))),
                            cursor.getString(cursor.getColumnIndex("sql")));
                } while (cursor.moveToNext());
                IOUtils.closeQuietly(cursor);
                return null;
            }
        });
    }

}