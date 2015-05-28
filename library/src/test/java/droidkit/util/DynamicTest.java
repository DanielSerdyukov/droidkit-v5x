package droidkit.util;

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
public class DynamicTest {

    @Test
    public void testGetCaller() throws Exception {
        final StackTraceElement ste = getCallerInternal();
        Assert.assertEquals("testGetCaller", ste.getMethodName());
    }

    @Test
    public void testForName() throws Exception {
        Assert.assertEquals(Sequence.class, Dynamic.<Sequence>forName("droidkit.util.Sequence"));
    }

    @Test
    public void testInitByClass() throws Exception {
        final MockObject instance = Dynamic.init(MockObject.class, "John", 25);
        Assert.assertNotNull(instance);
        Assert.assertEquals("John", instance.mName);
        Assert.assertEquals(25, instance.mAge);
    }

    @Test
    public void testInitByClassName() throws Exception {
        final MockObject instance = Dynamic.init(MockObject.class.getName(), "John", 25);
        Assert.assertNotNull(instance);
        Assert.assertEquals("John", instance.mName);
        Assert.assertEquals(25, instance.mAge);
    }

    @NonNull
    private StackTraceElement getCallerInternal() {
        return Dynamic.getCaller();
    }

    private static final class MockObject {

        private final String mName;

        private final int mAge;

        private MockObject(String name, int age) {
            mName = name;
            mAge = age;
        }

    }

}
