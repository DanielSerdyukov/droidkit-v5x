package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.ActiveBean;
import droidkit.sqlite.bean.SimpleBean;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest {

    private SQLiteProvider mProvider;

    private SimpleBean mSimpleBean;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        mSimpleBean = new SimpleBean();
        mSimpleBean.setText("assert");
        SQLite.save(mSimpleBean);
    }

    @Test
    public void testSave() throws Exception {
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(SimpleBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mSimpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mSimpleBean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {
        mSimpleBean.setText("updated");
        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(SimpleBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mSimpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertNotEquals(mSimpleBean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();

        SQLite.update(mSimpleBean);
        cursor = mProvider.query(SQLiteSchema.resolveUri(SimpleBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mSimpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mSimpleBean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();
    }

    @Test
    public void testActiveUpdate() throws Exception {
        final ActiveBean bean = new ActiveBean();
        bean.setText("assert");
        SQLite.save(bean);
        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(ActiveBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(bean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(bean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();

        bean.setText("updated");
        cursor = mProvider.query(SQLiteSchema.resolveUri(ActiveBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(bean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(bean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();
    }

    @Test
    public void testRemove() throws Exception {
        final SimpleBean simpleBean = new SimpleBean();
        simpleBean.setText("should be removed");
        SQLite.save(simpleBean);

        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(SimpleBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(simpleBean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();

        SQLite.remove(simpleBean);
        cursor = mProvider.query(SQLiteSchema.resolveUri(SimpleBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
