package droidkit.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class IterablesTest {

    private Iterable<String> mNonEmpty = Arrays.asList("one", "two", "three");

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(Iterables.isEmpty(Collections.emptySet()));
        Assert.assertFalse(Iterables.isEmpty(mNonEmpty));
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("one", Iterables.getFirst(mNonEmpty));
    }

    @Test
    public void testGetFirst1() throws Exception {
        Assert.assertEquals("empty", Iterables.getFirst(Collections.emptySet(), "empty"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("three", Iterables.getLast(mNonEmpty));
    }

    @Test
    public void testGetLast1() throws Exception {
        Assert.assertEquals("empty", Iterables.getLast(Collections.emptySet(), "empty"));
    }

    @Test
    public void testToArray() throws Exception {
        final String[] strings = new String[3];
        int index = 0;
        for (final String value : mNonEmpty) {
            strings[index++] = value;
        }
        Assert.assertArrayEquals(strings, Iterables.toArray(mNonEmpty, String.class));
    }

    @Test
    public void testTransform() throws Exception {
        final String[] strings = new String[3];
        int index = 0;
        for (final String value : mNonEmpty) {
            strings[index++] = "transformed_" + value;
        }
        Assert.assertArrayEquals(strings, Iterables.toArray(Iterables.transform(mNonEmpty, new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "transformed_" + s;
            }
        }), String.class));
    }

}