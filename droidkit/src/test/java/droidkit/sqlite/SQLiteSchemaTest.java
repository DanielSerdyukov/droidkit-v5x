package droidkit.sqlite;

import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.Standard;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Lists;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteSchemaTest {

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
    public void testCreateQueries() throws Exception {
        Assert.assertEquals(Standard.COLUMNS_DEF, SQLiteSchema.columnsOf(Standard.TABLE));
    }

    @Test
    public void testIndices() throws Exception {
        Assert.assertArrayEquals(Standard.INDICES, Lists.toArray(SQLiteSchema.indicesOf(Standard.TABLE), String.class));
    }

}