package droidkit.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public class ListsTest {

    private final List<String> mList = Arrays.asList("first", "second", "last");

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Lists.getFirst(mList));
    }

    @Test
    public void testGetFirstNotNull() throws Exception {
        Assert.assertEquals("first_nn", Lists.getFirst(Collections.emptyList(), "first_nn"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Lists.getLast(mList));
    }

    @Test
    public void testGetLastNotNull() throws Exception {
        Assert.assertEquals("last_nn", Lists.getLast(Collections.emptyList(), "last_nn"));
    }

    @Test
    public void testTransform() throws Exception {
        final String[] expected = {"first", "second", "last"};
        Assert.assertArrayEquals(expected, Lists.toArray(mList, String.class));
    }

    @Test
    public void testToArray() throws Exception {
        final String[] expected = {"t_first", "t_second", "t_last"};
        Assert.assertArrayEquals(expected, Lists.toArray(Lists.transform(mList, new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "t_" + s;
            }
        }), String.class));
    }

}