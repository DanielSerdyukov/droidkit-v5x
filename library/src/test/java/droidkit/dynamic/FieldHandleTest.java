package droidkit.dynamic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class FieldHandleTest {

    private FieldLookup mLookup;

    @Before
    public void setUp() throws Exception {
        mLookup = FieldLookup.global();
    }

    @Test
    public void testStaticGet() throws Exception {
        Assert.assertEquals("FOO", mLookup.find(Bar.class, "FOO").<String>getStatic());
        Assert.assertEquals(Integer.valueOf(1), mLookup.find(Bar.class, "sFoo").<Integer>getStatic());
    }

    @Test
    public void testStaticSet() throws Exception {
        mLookup.find(Bar.class, "sFoo").setStatic(2);
        Assert.assertEquals(2, Bar.sFoo);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testVirtualGet() throws Exception {
        final Foo bar = new Bar();
        Assert.assertEquals(42.2, mLookup.find(Bar.class, "mBar").<Double>getVirtual(bar), 0d);
        Assert.assertTrue(mLookup.find(Bar.class, "mBar2").<Boolean>getVirtual(bar));
    }

    @Test
    public void testVirtualSet() throws Exception {
        final Bar bar = new Bar();
        mLookup.find(Bar.class, "mBar").setVirtual(bar, 45.2);
        mLookup.find(Bar.class, "mBar2").setVirtual(bar, false);
        Assert.assertEquals(45.2, bar.mBar, 0d);
        Assert.assertFalse(bar.mBar2);
    }

    static class Foo {

        public static final String FOO = "FOO";

        public static int sFoo = 1;

    }


    static class Bar extends Foo {

        public double mBar = 42.2;

        private boolean mBar2 = true;

    }

}