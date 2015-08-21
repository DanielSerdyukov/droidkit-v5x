package droidkit.material;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class ColorSpecTest {

    private ColorSpec mDarkSpec;

    private ColorSpec mLightSpec;

    @Before
    public void setUp() throws Exception {
        mDarkSpec = ColorSpec.MATERIAL_DARK;
        mLightSpec = ColorSpec.MATERIAL_LIGHT;
    }

    @Test
    public void testPrimary() throws Exception {
        Assert.assertEquals("de000000", Integer.toHexString(mDarkSpec.primaryText()));
        Assert.assertEquals("ffffffff", Integer.toHexString(mLightSpec.primaryText()));
    }

    @Test
    public void testSecondary() throws Exception {
        Assert.assertEquals("8a000000", Integer.toHexString(mDarkSpec.secondaryText()));
        Assert.assertEquals("b3ffffff", Integer.toHexString(mLightSpec.secondaryText()));
    }

    @Test
    public void testHint() throws Exception {
        Assert.assertEquals("61000000", Integer.toHexString(mDarkSpec.hint()));
        Assert.assertEquals("4dffffff", Integer.toHexString(mLightSpec.hint()));
    }

    @Test
    public void testDivider() throws Exception {
        Assert.assertEquals("1f000000", Integer.toHexString(mDarkSpec.divider()));
        Assert.assertEquals("1fffffff", Integer.toHexString(mLightSpec.divider()));
    }

}