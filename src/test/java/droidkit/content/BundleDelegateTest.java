package droidkit.content;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class BundleDelegateTest {

    private static final String INT_KEY = "int_key";

    private static final String LONG_KEY = "long_key";

    private static final String FLOAT_KEY = "float_key";

    private static final String DOUBLE_KEY = "double_key";

    private static final String BOOLEAN_KEY = "boolean_key";

    private static final String STRING_KEY = "string_key";

    private static final String PARCEL_KEY = "parcel_key";

    private static final String STRING_LIST_KEY = "string_list_key";

    private Bundle mBundle;

    private BundleDelegate mDelegate;

    @Before
    public void setUp() throws Exception {
        mBundle = new Bundle();
        mDelegate = new BundleDelegate(mBundle);
        mBundle.putInt(INT_KEY, 1);
        mBundle.putLong(LONG_KEY, 10L);
        mBundle.putFloat(FLOAT_KEY, 0.9f);
        mBundle.putBoolean(BOOLEAN_KEY, true);
        mBundle.putString(STRING_KEY, "test");
        mBundle.putParcelable(PARCEL_KEY, Bundle.EMPTY);
        final ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, "first", "second");
        mBundle.putStringArrayList(STRING_LIST_KEY, list);
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(mBundle.getInt(INT_KEY, IntValue.EMPTY), mDelegate.getInt(INT_KEY, IntValue.EMPTY));
    }

    @Test
    public void testPutInt() throws Exception {
        mDelegate.putInt(INT_KEY, 2);
        Assert.assertEquals(2, mBundle.getInt(INT_KEY, IntValue.EMPTY));
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals(mBundle.getString(STRING_KEY, StringValue.EMPTY),
                mDelegate.getString(STRING_KEY, StringValue.EMPTY));
    }

    @Test
    public void testPutString() throws Exception {
        mDelegate.putString(STRING_KEY, "expected");
        Assert.assertEquals("expected", mBundle.getString(STRING_KEY, StringValue.EMPTY));
    }

    @Test
    public void testGetBoolean() throws Exception {
        Assert.assertEquals(mBundle.getBoolean(BOOLEAN_KEY, BoolValue.EMPTY),
                mDelegate.getBoolean(BOOLEAN_KEY, BoolValue.EMPTY));
    }

    @Test
    public void testPutBoolean() throws Exception {
        mDelegate.putBoolean(BOOLEAN_KEY, false);
        Assert.assertFalse(mBundle.getBoolean(BOOLEAN_KEY, true));
    }

    @Test
    public void testGetLong() throws Exception {
        Assert.assertEquals(mBundle.getLong(LONG_KEY, LongValue.EMPTY),
                mDelegate.getLong(LONG_KEY, LongValue.EMPTY));
    }

    @Test
    public void testPutLong() throws Exception {
        mDelegate.putLong(LONG_KEY, 20L);
        Assert.assertEquals(20L, mBundle.getLong(LONG_KEY, LongValue.EMPTY));
    }

    @Test
    public void testGetDouble() throws Exception {
        Assert.assertEquals(mBundle.getDouble(DOUBLE_KEY, DoubleValue.EMPTY),
                mDelegate.getDouble(DOUBLE_KEY, DoubleValue.EMPTY), 0d);
    }

    @Test
    public void testPutDouble() throws Exception {
        mDelegate.putDouble(DOUBLE_KEY, 99.50f);
        Assert.assertEquals(99.50f, mBundle.getDouble(DOUBLE_KEY, DoubleValue.EMPTY), 0d);
    }

    @Test
    public void testGetFloat() throws Exception {
        Assert.assertEquals(mBundle.getFloat(FLOAT_KEY, FloatValue.EMPTY),
                mDelegate.getFloat(FLOAT_KEY, FloatValue.EMPTY), 0f);
    }

    @Test
    public void testPutFloat() throws Exception {
        mDelegate.putFloat(FLOAT_KEY, 9.99f);
        Assert.assertEquals(9.99f, mBundle.getFloat(FLOAT_KEY, FloatValue.EMPTY), 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringSet() throws Exception {
        mDelegate.getStringSet(STRING_LIST_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutStringSet() throws Exception {
        mDelegate.putStringSet(STRING_LIST_KEY, Collections.<String>emptySet());
    }

    @Test
    public void testGetStringList() throws Exception {
        Assert.assertEquals(mBundle.getStringArrayList(STRING_LIST_KEY), mDelegate.getStringList(STRING_LIST_KEY));
    }

    @Test
    public void testPutStringList() throws Exception {
        final ArrayList<String> strings = new ArrayList<>();
        strings.add("expected");
        mDelegate.putStringList(STRING_LIST_KEY, strings);
        Assert.assertEquals(strings, mBundle.getStringArrayList(STRING_LIST_KEY));
    }

    @Test
    public void testGetParcelable() throws Exception {
        Assert.assertEquals(mBundle.getParcelable(PARCEL_KEY), mDelegate.getParcelable(PARCEL_KEY));
    }

    @Test
    public void testPutParcelable() throws Exception {
        final Bundle bundle = new Bundle();
        bundle.putLong(LONG_KEY, 100L);
        mDelegate.putParcelable(PARCEL_KEY, bundle);
        Assert.assertEquals(bundle, mBundle.getParcelable(PARCEL_KEY));
    }

    @Test
    public void testRemove() throws Exception {
        mDelegate.remove(STRING_KEY);
        Assert.assertFalse(mBundle.containsKey(STRING_KEY));
    }

    @Test
    public void testClear() throws Exception {
        mDelegate.clear();
        Assert.assertTrue(mBundle.isEmpty());
    }

}