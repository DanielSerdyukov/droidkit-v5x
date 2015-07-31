package droidkit.sqlite;

import android.database.Cursor;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.ActiveBean;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteResultTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        SQLite.beginTransaction();
        for (int i = 0; i < 10; ++i) {
            final ActiveBean bean = new ActiveBean();
            bean.setText("Bean #" + (i + 1));
            SQLite.save(bean);
        }
        SQLite.endTransaction();
    }

    @Test
    public void testGet() throws Exception {
        final List<ActiveBean> beans = SQLite.where(ActiveBean.class).list();
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(ActiveBean.class), null, null, null, null);
        Assert.assertEquals(cursor.getCount(), beans.size());
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertEquals(Cursors.getString(cursor, "text"), beans.get(cursor.getPosition()).getText());
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testAdd() throws Exception {
        final List<ActiveBean> beans = SQLite.where(ActiveBean.class).list();
        final ActiveBean newBean = new ActiveBean();
        newBean.setText("Added Bean");
        beans.add(newBean);
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(ActiveBean.class), null, null, null, null);
        Assert.assertEquals(cursor.getCount(), beans.size());
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertEquals(Cursors.getString(cursor, "text"), beans.get(cursor.getPosition()).getText());
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testRemove() throws Exception {
        final List<ActiveBean> beans = SQLite.where(ActiveBean.class).list();
        beans.remove(5);
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(ActiveBean.class), null, null, null, null);
        Assert.assertEquals(cursor.getCount(), beans.size());
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertEquals(Cursors.getString(cursor, "text"), beans.get(cursor.getPosition()).getText());
        } while (cursor.moveToNext());
        cursor.close();
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}