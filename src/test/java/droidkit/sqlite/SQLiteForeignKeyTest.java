package droidkit.sqlite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.Bar;
import droidkit.sqlite.bean.Baz;
import droidkit.sqlite.bean.Foo;
import droidkit.sqlite.bean.Qux;
import droidkit.sqlite.util.SQLiteTestEnv;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteForeignKeyTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
    }

    @Test
    public void testInsertNonStrict() throws Exception {
        final Bar expectedBar = new Bar();
        SQLite.save(expectedBar);
        final Baz expectedBaz = new Baz();
        expectedBar.setBaz(expectedBaz);
        expectedBaz.setBarId(expectedBar.getId());
        final Bar bar = SQLite.where(Bar.class).withId(1);
        Assert.assertNotNull(bar);
        Assert.assertNotNull(bar.getBaz());
        Assert.assertEquals(bar.getBaz().getBarId(), bar.getId());
    }

    @Test
    public void testRemoveNonStrict() throws Exception {
        final Bar expectedBar = new Bar();
        SQLite.save(expectedBar);
        final Baz expectedBaz = new Baz();
        expectedBaz.setBarId(expectedBar.getId());
        expectedBar.setBaz(expectedBaz);
        Assert.assertEquals(1, SQLite.where(Bar.class).count().intValue());
        Assert.assertEquals(1, SQLite.where(Baz.class).count().intValue());
        SQLite.removeAll(Bar.class);
        Assert.assertEquals(0, SQLite.where(Bar.class).count().intValue());
        Assert.assertEquals(0, SQLite.where(Baz.class).count().intValue());
    }

    @Test
    public void testUpdateNonStrict() throws Exception {
        final Bar expectedBar = new Bar();
        SQLite.save(expectedBar);
        final Baz expectedBaz = new Baz();
        expectedBaz.setBarId(expectedBar.getId());
        expectedBar.setBaz(expectedBaz);
        SQLite.obtainClient().executeUpdateDelete("UPDATE bar SET _id = ? WHERE _id =?", 20, expectedBar.getId());
        final Bar bar = SQLite.where(Bar.class).withId(20);
        Assert.assertNotNull(bar);
        Assert.assertNotNull(bar.getBaz());
        Assert.assertEquals(bar.getId(), bar.getBaz().getBarId());
    }

    @Test(expected = SQLiteException.class)
    public void testInsertStrictError() throws Exception {
        SQLite.save(new Qux());
    }

    @Test
    public void testInsertStrict() throws Exception {
        final Foo foo = new Foo();
        SQLite.save(foo);
        final Qux qux = new Qux();
        qux.setFooId(foo.getId());
        SQLite.save(qux);
        Assert.assertFalse(SQLite.where(Qux.class).equalTo("foo_id", foo.getId()).list().isEmpty());
    }

    @Test
    public void testUpdateCascade() throws Exception {
        final Foo foo = new Foo();
        SQLite.save(foo);
        final Qux qux = new Qux();
        qux.setFooId(foo.getId());
        SQLite.save(qux);
        SQLite.obtainClient().executeUpdateDelete("UPDATE foo SET _id = ? WHERE _id =?", 20, foo.getId());
        Assert.assertFalse(SQLite.where(Qux.class).equalTo("foo_id", 20).list().isEmpty());
    }

    @Test
    public void testRemoveCascade() throws Exception {
        final Foo foo = new Foo();
        SQLite.save(foo);
        final Qux qux = new Qux();
        qux.setFooId(foo.getId());
        SQLite.save(qux);
        SQLite.remove(foo);
        Assert.assertTrue(SQLite.where(Qux.class).equalTo("foo_id", 1).list().isEmpty());
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

}
