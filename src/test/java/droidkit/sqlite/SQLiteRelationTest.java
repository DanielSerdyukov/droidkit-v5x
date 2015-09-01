package droidkit.sqlite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import droidkit.sqlite.bean.abc.Bar;
import droidkit.sqlite.bean.abc.Baz;
import droidkit.sqlite.bean.xyz.Foo;
import droidkit.sqlite.util.SQLiteTestEnv;
import droidkit.util.Lists;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class SQLiteRelationTest {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = SQLiteTestEnv.registerProvider();
        final Foo foo = new Foo();
        foo.setText("Foo #1");
        for (int i = 1; i <= 5; ++i) {
            final Bar bar = new Bar();
            bar.setText("Bar #" + i + " for Foo #1");
            final Baz baz = new Baz();
            baz.setText("Baz for Bar #" + i);
            bar.setBaz(baz);
            foo.getBars().add(bar);
        }
        SQLite.beginTransaction();
        SQLite.save(foo);
        SQLite.endTransaction();
    }

    @Test
    public void testRelation() throws Exception {
        final Foo foo = SQLite.where(Foo.class).withId(1);
        Assert.assertNotNull(foo);
        Assert.assertEquals("Foo #1", foo.getText());
        final List<Bar> bars = foo.getBars();
        Assert.assertFalse(bars.isEmpty());
        for (int i = 0; i < bars.size(); ++i) {
            final Bar bar = bars.get(i);
            Assert.assertEquals("Bar #" + (i + 1) + " for Foo #1", bar.getText());
            final Baz baz = bar.getBaz();
            Assert.assertNotNull(baz);
            Assert.assertEquals("Baz for Bar #" + (i + 1), baz.getText());
        }
    }

    @Test
    public void testAddOneToMany() throws Exception {
        final Foo foo = SQLite.where(Foo.class).withId(1);
        Assert.assertNotNull(foo);
        final Bar addedBar = new Bar();
        addedBar.setText("Added Bar for Foo #1");
        foo.getBars().add(addedBar);
        final Bar bar = SQLite.where(Bar.class).withId(addedBar.getId());
        Assert.assertNotNull(bar);
        Assert.assertEquals(addedBar.getText(), bar.getText());
    }

    @Test
    public void testSetupOneToOne() throws Exception {
        final Foo foo = new Foo();
        foo.setText("Added Foo");
        SQLite.save(foo);
        final Bar addedBar = new Bar();
        addedBar.setText("Added Bar");
        foo.getBars().add(addedBar);
        final Bar bar = SQLite.where(Bar.class).withId(addedBar.getId());
        Assert.assertNotNull(bar);
        Assert.assertEquals(addedBar.getText(), bar.getText());
        Assert.assertEquals(Integer.valueOf(0), SQLite.execute(new BarBazFunc(bar.getId())));
        final Baz addedBaz = new Baz();
        addedBaz.setText("Added Baz");
        bar.setBaz(addedBaz);
        final Baz baz = SQLite.where(Baz.class).withId(addedBaz.getId());
        Assert.assertNotNull(baz);
        Assert.assertEquals(addedBaz.getText(), baz.getText());
        Assert.assertEquals(Integer.valueOf(1), SQLite.execute(new BarBazFunc(bar.getId())));
    }

    @Test
    public void testUpdateOneToOne() throws Exception {
        final Foo foo = SQLite.where(Foo.class).withId(1);
        Assert.assertNotNull(foo);
        final Bar bar = Lists.getFirst(foo.getBars());
        Assert.assertNotNull(bar);
        final Integer bazCount = SQLite.execute(new BarBazFunc(bar.getId()));
        final Baz addedBaz = new Baz();
        addedBaz.setText("Added Baz");
        bar.setBaz(addedBaz);
        final Baz baz = SQLite.where(Baz.class).withId(addedBaz.getId());
        Assert.assertNotNull(baz);
        Assert.assertEquals(addedBaz.getText(), baz.getText());
        Assert.assertEquals(bazCount, SQLite.execute(new BarBazFunc(bar.getId())));
    }

    @Test
    public void testRemoveParent() throws Exception {
        Assert.assertTrue(SQLite.execute(new FooBarFunc()) > 0);
        Assert.assertTrue(SQLite.execute(new BarFunc()) > 0);
        SQLite.removeAll(Foo.class);
        Assert.assertFalse(SQLite.execute(new FooBarFunc()) > 0);
        Assert.assertTrue(SQLite.execute(new BarFunc()) > 0);
    }

    @Test
    public void testRemoveChild() throws Exception {
        Assert.assertTrue(SQLite.execute(new FooBarFunc()) > 0);
        Assert.assertTrue(SQLite.execute(new BarFunc()) > 0);
        final Foo foo = SQLite.where(Foo.class).withId(1);
        Assert.assertNotNull(foo);
        foo.getBars().clear();
        Assert.assertFalse(SQLite.execute(new FooBarFunc()) > 0);
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

    private static class FooBarFunc implements Func1<SQLiteClient, Integer> {
        @Override
        public Integer call(SQLiteClient client) {
            return client.query("SELECT * FROM foo_bar;").getCount();
        }
    }

    private static class BarFunc implements Func1<SQLiteClient, Integer> {
        @Override
        public Integer call(SQLiteClient client) {
            return client.query("SELECT * FROM bar;").getCount();
        }
    }

    private static class BarBazFunc implements Func1<SQLiteClient, Integer> {

        private final long mBarId;

        public BarBazFunc(long barId) {
            mBarId = barId;
        }

        @Override
        public Integer call(SQLiteClient client) {
            return client.query("SELECT * FROM bar_baz WHERE bar_id = ?;", mBarId).getCount();
        }

    }

}
