package droidkit.util;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.test.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class DynamicFieldTest {

    private MockObject mObject;

    @Before
    public void setUp() throws Exception {
        MockObject.sStaticField = MockObject.EXPECTED_VALUE;
        MockObject.sPrivateStaticField = MockObject.EXPECTED_VALUE;
        mObject = new MockObject();
    }

    @Test
    public void testGetStaticPublic() throws Exception {
        Assert.assertEquals(MockObject.EXPECTED_VALUE, DynamicField.getStatic(MockObject.class, "sStaticField"));
    }

    @Test
    public void testGetStaticPrivate() throws Exception {
        Assert.assertEquals(MockObject.EXPECTED_VALUE, DynamicField.getStatic(MockObject.class, "sPrivateStaticField"));
    }

    @Test
    public void testSetStaticPublic() throws Exception {
        final String expected = "new_expected";
        DynamicField.setStatic(MockObject.class, "sStaticField", expected);
        Assert.assertEquals(expected, MockObject.sStaticField);
    }

    @Test
    public void testSetStaticPrivate() throws Exception {
        final String expected = "new_expected";
        DynamicField.setStatic(MockObject.class, "sPrivateStaticField", expected);
        Assert.assertEquals(expected, MockObject.sPrivateStaticField);
    }

    public void testGetPublic() throws Exception {
        Assert.assertEquals(MockObject.EXPECTED_VALUE, DynamicField.get(mObject, "mInstanceField"));
    }

    @Test
    public void testGetPrivate() throws Exception {
        Assert.assertEquals(MockObject.EXPECTED_VALUE, DynamicField.get(mObject, "mPrivateInstanceField"));
    }

    @Test
    public void testSetPublic() throws Exception {
        final String expected = "new_expected";
        DynamicField.set(mObject, "mInstanceField", expected);
        Assert.assertEquals(expected, mObject.mInstanceField);
    }

    @Test
    public void testSetPrivate() throws Exception {
        final String expected = "new_expected";
        DynamicField.set(mObject, "mPrivateInstanceField", expected);
        Assert.assertEquals(expected, mObject.mPrivateInstanceField);
    }

    private static class MockObject {

        static final String EXPECTED_VALUE = "expected";

        static String sStaticField = EXPECTED_VALUE;

        private static String sPrivateStaticField = EXPECTED_VALUE;

        String mInstanceField;

        private String mPrivateInstanceField;

        private MockObject() {
            mInstanceField = EXPECTED_VALUE;
            mPrivateInstanceField = EXPECTED_VALUE;
        }

    }
}
