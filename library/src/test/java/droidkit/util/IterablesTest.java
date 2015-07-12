package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class IterablesTest {

    private Set<String> mIterable;

    @Before
    public void setUp() throws Exception {
        mIterable = new LinkedHashSet<>(Arrays.asList("first", "middle", "last"));
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Iterables.getFirst(mIterable));
    }

    @Test
    public void testGetFirstWithValue() throws Exception {
        Assert.assertEquals("defaultFirst", Iterables.getFirst(Collections.emptySet(), "defaultFirst"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Iterables.getLast(mIterable));
    }

    @Test
    public void testGetLastWithValue() throws Exception {
        Assert.assertEquals("defaultLast", Iterables.getLast(Collections.emptySet(), "defaultLast"));
    }

    @Test
    public void testTransform() throws Exception {
        final Iterator<String> expected = mIterable.iterator();
        final Iterator<String> actual = Iterables.transform(mIterable, new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "t:" + s;
            }
        }).iterator();
        while (expected.hasNext() && actual.hasNext()) {
            Assert.assertEquals("t:" + expected.next(), actual.next());
        }
    }

}