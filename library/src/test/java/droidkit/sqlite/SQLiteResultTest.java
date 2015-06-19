package droidkit.sqlite;

import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.database.DatabaseUtils;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import unit.test.mock.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteResultTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = new SQLiteProvider();
        final ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.name = SQLiteProvider.class.getName();
        providerInfo.authority = BuildConfig.APPLICATION_ID;
        mProvider.attachInfo(RuntimeEnvironment.application, providerInfo);
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(BuildConfig.APPLICATION_ID, mProvider);
        final ContentValues values = new ContentValues();
        for (int i = 1; i <= 10; ++i) {
            values.clear();
            values.put("name", "User #" + i);
            values.put("lat", 50.5 + i);
            mProvider.insert(SQLite.uriOf(SQLiteUser.class), values);
        }
    }

    @Test
    public void testList() throws Exception {
        final List<SQLiteUser> list = SQLite.where(SQLiteUser.class).list();
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(list.size(), cursor.getCount());
        do {
            Assert.assertEquals(list.get(cursor.getPosition()).getName(),
                    DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testAdd() throws Exception {
        final List<SQLiteUser> list = SQLite.where(SQLiteUser.class).list();
        final SQLiteUser newEntry = new SQLiteUser().setName("Added Entry");
        Assert.assertTrue(list.add(newEntry));
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, "name = ?",
                new String[]{newEntry.getName()}, null);
        Assert.assertTrue(cursor.moveToFirst());
        Assert.assertEquals(newEntry.getId(), DatabaseUtils.getLong(cursor, "_id"));
        cursor.close();
    }

    @Test
    public void testRemove() throws Exception {
        final List<SQLiteUser> list = SQLite.where(SQLiteUser.class).list();
        final SQLiteUser removed = list.remove(5);
        Assert.assertEquals("User #6", removed.getName());
        final Cursor cursor = mProvider.query(SQLite.uriOf(SQLiteUser.class), null, null, null, null);
        Assert.assertTrue(cursor.moveToFirst());
        do {
            Assert.assertNotEquals(removed.getName(), DatabaseUtils.getString(cursor, "name"));
        } while (cursor.moveToNext());
        cursor.close();
    }

    @Test
    public void testObservable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        SQLite.where(SQLiteUser.class).observable()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<List<SQLiteUser>>() {
                    @Override
                    public void call(@NonNull List<SQLiteUser> entries) {
                        Assert.assertEquals(10, entries.size());
                        latch.countDown();
                    }
                });
        Assert.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testEqualTo() throws Exception {
        Assert.assertEquals(1, SQLite.where(SQLiteUser.class)
                .equalTo("name", "User #3")
                .list().size());
    }

    @Test
    public void testNotEqualTo() throws Exception {
        Assert.assertEquals(9, SQLite.where(SQLiteUser.class)
                .notEqualTo("name", "User #3")
                .list().size());
    }

    @Test
    public void testLessThan() throws Exception {
        Assert.assertEquals(9, SQLite.where(SQLiteUser.class)
                .lessThan("lat", 60.5)
                .list().size());
    }

    @Test
    public void testLessThanOrEqualTo() throws Exception {
        Assert.assertEquals(5, SQLite.where(SQLiteUser.class)
                .lessThanOrEqualTo("lat", 55.5)
                .list().size());
    }

    @Test
    public void testGreaterThan() throws Exception {
        Assert.assertEquals(5, SQLite.where(SQLiteUser.class)
                .greaterThan("lat", 55.5)
                .list().size());
    }

    @Test
    public void testGreaterThanOrEqualTo() throws Exception {
        Assert.assertEquals(1, SQLite.where(SQLiteUser.class)
                .greaterThanOrEqualTo("lat", 60.5)
                .list().size());
    }

    @Test
    public void testLike() throws Exception {
        Assert.assertEquals(1, SQLite.where(SQLiteUser.class)
                .like("name", "%#6")
                .list().size());
    }

    @Test
    public void testBetween() throws Exception {
        Assert.assertEquals(5, SQLite.where(SQLiteUser.class)
                .between("lat", 56.5, 60.5)
                .list().size());
    }

    @Test
    public void testWhereGroup() throws Exception {
        Assert.assertEquals(3, SQLite.where(SQLiteUser.class)
                .equalTo("name", "User #2")
                .or()
                .beginGroup()
                .equalTo("name", "User #3")
                .or()
                .equalTo("name", "User #4")
                .endGroup()
                .list().size());
    }

    @Test
    public void testMin() throws Exception {
        Assert.assertEquals(51.5, SQLite.where(SQLiteUser.class).min("lat").doubleValue(), 0d);
    }

    @Test
    public void testMax() throws Exception {
        Assert.assertEquals(60.5, SQLite.where(SQLiteUser.class).max("lat").doubleValue(), 0d);
    }

    @Test
    public void testSum() throws Exception {
        Assert.assertEquals(560, SQLite.where(SQLiteUser.class).sum("lat").intValue());
    }

    @Test
    public void testCount() throws Exception {
        Assert.assertEquals(4, SQLite.where(SQLiteUser.class)
                .lessThan("lat", 55.5)
                .count("lat")
                .intValue());
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
