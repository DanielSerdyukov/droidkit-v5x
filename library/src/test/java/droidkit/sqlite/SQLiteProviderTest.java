package droidkit.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteProviderTest extends SQLiteTestCase {

    private static final Uri ALL_USERS = Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/users");

    private static final Uri FIRST_USER = Uri.parse("content://" + BuildConfig.APPLICATION_ID + "/users/1");

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals("vnd.android.cursor.dir/users", getProvider().getType(ALL_USERS));
        Assert.assertEquals("vnd.android.cursor.item/users", getProvider().getType(FIRST_USER));
    }


    @Test
    public void testQuery() throws Exception {
        final Cursor cursor = getProvider().query(ALL_USERS, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals("John", DatabaseUtils.getString(cursor, "name"));
        cursor.close();
    }

    @Test
    public void testInsert() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("name", "Jack");
        Assert.assertEquals("4", getProvider().insert(ALL_USERS, values).getLastPathSegment());
        final Cursor cursor = getProvider().query(ALL_USERS, null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(4, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testDelete() throws Exception {
        Assert.assertEquals(1, getProvider().delete(ALL_USERS, "name = ?", new String[]{"Jane"}));
        Assert.assertEquals(2, getProvider().delete(ALL_USERS, null, null));
    }

    @Test
    public void testUpdate() throws Exception {
        final ContentValues values = new ContentValues();
        values.put("name", "Joe");
        Assert.assertEquals(1, getProvider().update(ALL_USERS, values, "name = ?", new String[]{"Jane"}));
    }

    @Test
    public void testNotifyChange() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        RuntimeEnvironment.application.getContentResolver().registerContentObserver(ALL_USERS, true,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        latch.countDown();
                    }
                });
        final ContentValues values = new ContentValues();
        values.put("name", "Jack");
        getProvider().insert(ALL_USERS, values);
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @NonNull
    @Override
    protected SQLiteProvider createProvider() {
        return new SQLiteProvider() {

            @Nullable
            @Override
            protected String getDatabaseName() {
                return null;
            }

            @Override
            public void onDatabaseCreate(@NonNull SQLiteDatabase db) {
                db.execSQL("CREATE TABLE IF NOT EXISTS users (name TEXT);");
                db.execSQL("INSERT INTO users(name) VALUES(?);", "John");
                db.execSQL("INSERT INTO users(name) VALUES(?);", "Jane");
                db.execSQL("INSERT INTO users(name) VALUES(?);", "Jim");
            }

            @Override
            SQLiteClient createClient(@NonNull Context context) {
                return new SQLiteClientImpl(context, getDatabaseName(), getDatabaseVersion(), this);
            }

        };
    }

}