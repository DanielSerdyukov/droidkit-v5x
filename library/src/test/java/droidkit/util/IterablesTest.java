package droidkit.util;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class IterablesTest {

    private static final List<String> LIST = Arrays.asList("first", "middle", "last");

    private static final Set<String> SET = new LinkedHashSet<>(LIST);

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Iterables.getFirst(LIST));
        Assert.assertEquals("first", Iterables.getFirst(SET));
    }

    @Test
    public void testGetFirstWithDefaultValue() throws Exception {
        Assert.assertEquals("defaultFirst", Iterables.getFirst(Collections.emptyList(), "defaultFirst"));
        Assert.assertEquals("defaultFirst", Iterables.getFirst(Collections.emptySet(), "defaultFirst"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Iterables.getLast(LIST));
        Assert.assertEquals("last", Iterables.getLast(SET));
    }

    @Test
    public void testGetLastWithDefaultValue() throws Exception {
        Assert.assertEquals("defaultLast", Iterables.getLast(Collections.emptyList(), "defaultLast"));
        Assert.assertEquals("defaultLast", Iterables.getLast(Collections.emptySet(), "defaultLast"));
    }

}