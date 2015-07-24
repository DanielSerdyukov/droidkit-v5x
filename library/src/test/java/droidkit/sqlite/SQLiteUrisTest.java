package droidkit.sqlite;

import android.net.Uri;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.SQLiteBean;
import droidkit.sqlite.util.SQLiteTestEnv;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteUrisTest {

    @Before
    public void setUp() throws Exception {
        SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testResolve() throws Exception {
        final Uri expected = new Uri.Builder()
                .scheme("content")
                .authority(BuildConfig.APPLICATION_ID)
                .path(SQLiteBean.TABLE)
                .build();
        final Uri actual = SQLiteUris.resolve(SQLiteBean.class);
        Assert.assertEquals(expected, actual);
    }

}