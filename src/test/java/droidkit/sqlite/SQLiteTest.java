package droidkit.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.xyz.Foo;
import droidkit.sqlite.bean.Standard;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Cursors;
import droidkit.util.Lists;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteTest {

    private SQLiteProvider mProvider;

    private Standard mStandard;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        mStandard = new Standard();
        mStandard.setString("assert");
        SQLite.save(mStandard);
    }

    @Test
    public void testRawQuery() throws Exception {
        final List<Standard> list = SQLite.rawQuery(Standard.class, "SELECT * FROM " + Standard.TABLE + ";");
        Assert.assertFalse(list.isEmpty());
        Assert.assertEquals("assert", Lists.getFirst(list).getString());
    }

    @Test
    public void testSave() throws Exception {
        final Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(Standard.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mStandard.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mStandard.getString(), Cursors.getString(cursor, "string"));
        cursor.close();
    }

    @Test
    public void testUpdate() throws Exception {
        mStandard.setString("updated");
        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(Standard.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mStandard.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertNotEquals(mStandard.getString(), Cursors.getString(cursor, "string"));
        cursor.close();

        SQLite.update(mStandard);
        cursor = mProvider.query(SQLiteSchema.resolveUri(Standard.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(mStandard.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(mStandard.getString(), Cursors.getString(cursor, "string"));
        cursor.close();
    }

    @Test
    public void testFooUpdate() throws Exception {
        final Foo bean = new Foo();
        bean.setText("assert");
        SQLite.save(bean);
        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(Foo.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(bean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(bean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();

        bean.setText("updated");
        cursor = mProvider.query(SQLiteSchema.resolveUri(Foo.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(bean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(bean.getText(), Cursors.getString(cursor, "text"));
        cursor.close();
    }

    @Test
    public void testRemove() throws Exception {
        final Standard simpleBean = new Standard();
        simpleBean.setString("should be removed");
        SQLite.save(simpleBean);

        Cursor cursor = mProvider.query(SQLiteSchema.resolveUri(Standard.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(simpleBean.getString(), Cursors.getString(cursor, "string"));
        cursor.close();

        SQLite.remove(simpleBean);
        cursor = mProvider.query(SQLiteSchema.resolveUri(Standard.class), null,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(simpleBean.getId())}, null);
        Assert.assertFalse(cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testExecute() throws Exception {
        Assert.assertEquals("assert", SQLite.execute(new Func1<SQLiteClient, String>() {
            @Override
            public String call(SQLiteClient client) {
                return client.queryForString("SELECT string FROM " + SQLiteSchema.resolveTable(Standard.class));
            }
        }));
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
