package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class IterablesTest {

    private List<String> mList;

    private Collection<String> mCollection;

    private Iterable<String> mIterable;

    @Before
    public void setUp() throws Exception {
        mList = Arrays.asList("first", "second", "last");
        mCollection = new LinkedHashSet<>();
        Collections.addAll(mCollection, "first", "second", "last");
        mIterable = new IterableImpl("first", "second", "last");
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertFalse(Iterables.isEmpty(mList));
        Assert.assertFalse(Iterables.isEmpty(mCollection));
        Assert.assertFalse(Iterables.isEmpty(mList));
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Iterables.getFirst(mList));
        Assert.assertEquals("first", Iterables.getFirst(mCollection));
        Assert.assertEquals("first", Iterables.getFirst(mIterable));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetFirstNotNull() throws Exception {
        Assert.assertEquals("first_nn", Iterables.getFirst(Collections.emptyList(), "first_nn"));
        Assert.assertEquals("first_nn", Iterables.getFirst(Collections.emptySet(), "first_nn"));
        final Iterable<String> iterable = Mockito.spy(mIterable);
        final Iterator<String> iterator = Mockito.mock(Iterator.class);
        Mockito.when(iterable.iterator()).thenReturn(iterator);
        Mockito.when(iterator.hasNext()).thenReturn(false);
        Assert.assertEquals("first_nn", Iterables.getFirst(iterable, "first_nn"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Iterables.getLast(mList));
        Assert.assertEquals("last", Iterables.getLast(mCollection));
        Assert.assertEquals("last", Iterables.getLast(mIterable));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetLastNotNull() throws Exception {
        Assert.assertEquals("last_nn", Iterables.getLast(Collections.emptyList(), "last_nn"));
        Assert.assertEquals("last_nn", Iterables.getLast(Collections.emptySet(), "last_nn"));
        final Iterable<String> iterable = Mockito.spy(mIterable);
        final Iterator<String> iterator = Mockito.mock(Iterator.class);
        Mockito.when(iterable.iterator()).thenReturn(iterator);
        Mockito.when(iterator.hasNext()).thenReturn(false);
        Assert.assertEquals("last_nn", Iterables.getLast(iterable, "last_nn"));
    }

    @Test
    public void testToArray() throws Exception {
        final String[] expected = {"first", "second", "last"};
        Assert.assertArrayEquals(expected, Iterables.toArray(mList, String.class));
        Assert.assertArrayEquals(expected, Iterables.toArray(mCollection, String.class));
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
        Assert.assertArrayEquals(expected, Iterables.toArray(Iterables.transform(mCollection, func), String.class));
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