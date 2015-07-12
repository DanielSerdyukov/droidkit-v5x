package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class ListsTest {

    private List<String> mList;

    @Before
    public void setUp() throws Exception {
        mList = Arrays.asList("first", "middle", "last");
    }

    @Test
    public void testGetFirst() throws Exception {
        Assert.assertEquals("first", Lists.getFirst(mList));
    }

    @Test
    public void testGetFirstWithValue() throws Exception {
        Assert.assertEquals("emptyFirst", Lists.getFirst(Collections.emptyList(), "emptyFirst"));
    }

    @Test
    public void testGetLast() throws Exception {
        Assert.assertEquals("last", Lists.getLast(mList));
    }

    @Test
    public void testGetLastWithValue() throws Exception {
        Assert.assertEquals("emptyLast", Lists.getLast(Collections.emptyList(), "emptyLast"));
    }

    @Test
    public void testTransform() throws Exception {
        final List<String> actual = Lists.transform(mList, new Func1<String, String>() {
            @Override
            public String call(String s) {
                return "t:" + s;
            }
        });
        for (int i = 0; i < mList.size(); ++i) {
            Assert.assertEquals("t:" + mList.get(i), actual.get(i));
        }
    }

}