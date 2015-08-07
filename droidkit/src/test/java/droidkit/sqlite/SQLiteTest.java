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
import droidkit.sqlite.bean.AllTypesBean;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest {

    private SQLiteProvider mProvider;

    private AllTypesBean mAllTypes;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        mAllTypes = new AllTypesBean();
        mAllTypes.setString("assert");
        SQLite.save(mAllTypes);
    }

    @Test
    public void testSave() throws Exception {
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(AllTypesBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mAllTypes.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mAllTypes.getString(), Cursors.getString(cursor, "string"));
        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {
        mAllTypes.setString("updated");
        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(AllTypesBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mAllTypes.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertNotEquals(mAllTypes.getString(), Cursors.getString(cursor, "text"));
        cursor.close();

        SQLite.update(mAllTypes);
        cursor = mProvider.query(SQLiteSchema.resolveUri(AllTypesBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mAllTypes.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mAllTypes.getString(), Cursors.getString(cursor, "text"));
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
        final AllTypesBean simpleBean = new AllTypesBean();
        simpleBean.setString("should be removed");
        SQLite.save(simpleBean);

        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(AllTypesBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(simpleBean.getString(), Cursors.getString(cursor, "text"));
        cursor.close();

        SQLite.remove(simpleBean);
        cursor = mProvider.query(SQLiteSchema.resolveUri(AllTypesBean.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testExecute() throws Exception {
        Assert.assertEquals("assert", SQLite.execute(new Func1<SQLiteClient, String>() {
            @Override
            public String call(SQLiteClient client) {
                return client.queryForString("SELECT text FROM " + SQLiteSchema.resolveTable(AllTypesBean.class));
            }
        }));
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
