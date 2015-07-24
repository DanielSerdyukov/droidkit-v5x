package droidkit.dynamic;

import android.support.annotation.NonNull;

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
public class ConstructorHandleTest {

    @Test
    public void testInstantiate() throws Exception {
        final ConstructorLookup lookup = ConstructorLookup.local();
        final Foo foo = lookup.find(Foo.class).instantiate();
        Assert.assertTrue(foo.isFoo());
        final Bar bar = lookup.find(Bar.class, String.class).instantiate("test");
        Assert.assertTrue(bar.isFoo());
        Assert.assertEquals("test", bar.getKey());
    }

    static class Foo {

        private boolean mFoo;

        public Foo() {
            mFoo = true;
        }

        public boolean isFoo() {
            return mFoo;
        }

    }


    static class Bar extends Foo {

        private final String mKey;

        public Bar(@NonNull String key) {
            super();
            mKey = key;
        }

        @NonNull
        public String getKey() {
            return mKey;
        }

    }

}