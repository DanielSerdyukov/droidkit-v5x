package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import droidkit.content.StringValue;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class IterablesTest {

    private List<String> mList;

    private Collection<String> mSet;

    private Iterable<String> mIterable;

    @Before
    public void setUp() throws Exception {
        mList = Arrays.asList("first", "second", "last");
        mSet = new LinkedHashSet<>();
        Collections.addAll(mSet, "first", "second", "last");
        mIterable = new IterableImpl("first", "second", "last");
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertFalse(Iterables.isEmpty(mList));
        Assert.assertTrue(Iterables.isEmpty(Collections.emptyList()));
        Assert.assertFalse(Iterables.isEmpty(mSet));
        Assert.assertTrue(Iterables.isEmpty(Collections.emptySet()));
        Assert.assertFalse(Iterables.isEmpty(mIterable));
        Assert.assertTrue(Iterables.isEmpty(new IterableImpl()));
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Iterables.getFirst(mList));
        Assert.assertEquals("first", Iterables.getFirst(mSet));
        Assert.assertEquals("first", Iterables.getFirst(mIterable));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetFirstWithEmptyValue() throws Exception {
        Assert.assertEquals("first", Iterables.getFirst(mList, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getFirst(Collections.emptyList(), StringValue.EMPTY));
        Assert.assertEquals("first", Iterables.getFirst(mSet, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getFirst(Collections.emptySet(), StringValue.EMPTY));
        Assert.assertEquals("first", Iterables.getFirst(mIterable, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getFirst(new IterableImpl(), StringValue.EMPTY));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Iterables.getLast(mList));
        Assert.assertEquals("last", Iterables.getLast(mSet));
        Assert.assertEquals("last", Iterables.getLast(mIterable));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetLastWithEmptyValue() throws Exception {
        Assert.assertEquals("last", Iterables.getLast(mList, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getLast(Collections.emptyList(), StringValue.EMPTY));
        Assert.assertEquals("last", Iterables.getLast(mSet, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getLast(Collections.emptySet(), StringValue.EMPTY));
        Assert.assertEquals("last", Iterables.getLast(mIterable, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Iterables.getLast(new IterableImpl(), StringValue.EMPTY));
    }

    @Test
    public void testToArray() throws Exception {
        final String[] expected = {"first", "second", "last"};
        Assert.assertArrayEquals(expected, Iterables.toArray(mList, String.class));
        Assert.assertArrayEquals(expected, Iterables.toArray(mSet, String.class));
        Assert.assertArrayEquals(expected, Iterables.toArray(mIterable, String.class));
    }

    @Test
    public void testTransform() throws Exception {
        final Func1<String, String> func = new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "t_" + s;
            }
        };
        final String[] expected = {"t_first", "t_second", "t_last"};
        Assert.assertArrayEquals(expected, Iterables.toArray(Iterables.transform(mList, func), String.class));
        Assert.assertArrayEquals(expected, Iterables.toArray(Iterables.transform(mSet, func), String.class));
        Assert.assertArrayEquals(expected, Iterables.toArray(Iterables.transform(mIterable, func), String.class));
    }

    private static class IterableImpl implements Iterable<String> {

        private final List<String> mValues;

        public IterableImpl(String... values) {
            mValues = Arrays.asList(values);
        }

        @Override
        public Iterator<String> iterator() {
            return mValues.iterator();
        }

    }

}