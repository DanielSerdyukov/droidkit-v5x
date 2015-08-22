package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import droidkit.content.StringValue;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class SetsTest {

    private Set<String> mSet;

    @Before
    public void setUp() throws Exception {
        mSet = new LinkedHashSet<>();
        Collections.addAll(mSet, "first", "second", "last");
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Sets.getFirst(mSet));
    }

    @Test
    public void testGetFirstWithEmptyValue() throws Exception {
        Assert.assertEquals("first", Sets.getFirst(mSet, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Sets.getFirst(Collections.emptySet(), StringValue.EMPTY));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Sets.getLast(mSet));
    }

    @Test
    public void testGetLastWithEmptyValue() throws Exception {
        Assert.assertEquals("last", Sets.getLast(mSet, StringValue.EMPTY));
        Assert.assertEquals(StringValue.EMPTY, Sets.getLast(Collections.emptySet(), StringValue.EMPTY));
    }

    @Test
    public void testTransform() throws Exception {
        final String[] expected = {"first", "second", "last"};
        Assert.assertArrayEquals(expected, Sets.toArray(mSet, String.class));
    }

    @Test
    public void testToArray() throws Exception {
        final String[] expected = {"t_first", "t_second", "t_last"};
        Assert.assertArrayEquals(expected, Sets.toArray(Sets.transform(mSet, new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "t_" + s;
            }
        }), String.class));
    }

    @Test(expected = NoSuchElementException.class)
    public void testCheckNotEmpty() throws Exception {
        Sets.checkNotEmpty(Collections.emptySet());
    }

}