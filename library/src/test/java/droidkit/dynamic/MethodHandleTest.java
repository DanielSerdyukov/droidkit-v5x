package droidkit.dynamic;

import org.junit.Assert;
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
@SuppressWarnings("ConstantConditions")
public class MethodHandleTest {

    @Test
    public void testInvokeStatic() throws Exception {
        final MethodLookup lookup = MethodLookup.local();
        Assert.assertTrue(lookup.find(Bar.class, "foo").<Boolean>invokeStatic());
        Assert.assertEquals(10.3, lookup.find(Bar.class, "bar", Double.TYPE)
                .<Double>invokeStatic(10.3), 0d);

    }

    @Test
    public void testInvokeVirtual() throws Exception {
        final Foo foo = new Bar();
        final MethodLookup lookup = MethodLookup.local();
        Assert.assertEquals(Integer.valueOf(10), lookup.find(Bar.class, "foo", Integer.TYPE)
                .<Integer>invokeVirtual(foo, 10));
        Assert.assertFalse(lookup.find(Bar.class, "bar", Boolean.TYPE)
                .<Boolean>invokeVirtual(foo, false));
    }

    static class Foo {

        public static boolean foo() {
            return true;
        }

        public int foo(int value) {
            return value;
        }

    }


    static class Bar extends Foo {

        private static double bar(double value) {
            return value;
        }

        private boolean bar(boolean value) {
            return value;
        }

    }

}
