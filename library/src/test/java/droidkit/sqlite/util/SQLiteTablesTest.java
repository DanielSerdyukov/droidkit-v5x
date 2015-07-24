package droidkit.sqlite.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.SQLiteTables;
import droidkit.sqlite.bean.SQLiteBean;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTablesTest {

    @Test
    public void testResolve() throws Exception {
        Assert.assertEquals(SQLiteBean.TABLE, SQLiteTables.resolve(SQLiteBean.class));
    }

}