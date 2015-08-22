package droidkit.content;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class PreferenceDelegateTest {

    private static final String INT_KEY = "int_value";

    private static final String LONG_KEY = "long_value";

    private static final String FLOAT_KEY = "float_value";

    private static final String BOOLEAN_KEY = "boolean_value";

    private static final String STRING_KEY = "string_value";

    private static final String STRING_SET_KEY = "string_set_value";

    private SharedPreferences mPrefs;

    private PreferenceDelegate mDelegate;

    @Before
    public void setUp() throws Exception {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        mDelegate = new PreferenceDelegate(mPrefs);
        final HashSet<String> hashSet = new HashSet<>();
        Collections.addAll(hashSet, "first", "second", "last");
        mPrefs.edit()
                .putInt(INT_KEY, 1)
                .putLong(LONG_KEY, 10L)
                .putFloat(FLOAT_KEY, 0.9f)
                .putBoolean(BOOLEAN_KEY, true)
                .putString(STRING_KEY, "test")
                .putStringSet(STRING_SET_KEY, hashSet)
                .apply();
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(mPrefs.getInt(INT_KEY, IntValue.EMPTY), mDelegate.getInt(INT_KEY, IntValue.EMPTY));
    }

    @Test
    public void testPutInt() throws Exception {
        mDelegate.putInt(INT_KEY, 2);
        Assert.assertEquals(2, mPrefs.getInt(INT_KEY, IntValue.EMPTY));
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals(mPrefs.getString(STRING_KEY, StringValue.EMPTY),
                mDelegate.getString(STRING_KEY, StringValue.EMPTY));
    }

    @Test
    public void testPutString() throws Exception {
        mDelegate.putString(STRING_KEY, "expected");
        Assert.assertEquals("expected", mPrefs.getString(STRING_KEY, StringValue.EMPTY));
    }

    @Test
    public void testGetBoolean() throws Exception {
        Assert.assertEquals(mPrefs.getBoolean(BOOLEAN_KEY, BoolValue.EMPTY),
                mDelegate.getBoolean(BOOLEAN_KEY, BoolValue.EMPTY));
    }

    @Test
    public void testPutBoolean() throws Exception {
        mDelegate.putBoolean(BOOLEAN_KEY, false);
        Assert.assertFalse(mPrefs.getBoolean(BOOLEAN_KEY, true));
    }

    @Test
    public void testGetLong() throws Exception {
        Assert.assertEquals(mPrefs.getLong(LONG_KEY, LongValue.EMPTY),
                mDelegate.getLong(LONG_KEY, LongValue.EMPTY));
    }

    @Test
    public void testPutLong() throws Exception {
        mDelegate.putLong(LONG_KEY, 20L);
        Assert.assertEquals(20L, mPrefs.getLong(LONG_KEY, LongValue.EMPTY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDouble() throws Exception {
        mDelegate.getDouble("double_value", DoubleValue.EMPTY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutDouble() throws Exception {
        mDelegate.putDouble("double_value", DoubleValue.EMPTY);
    }

    @Test
    public void testGetFloat() throws Exception {
        Assert.assertEquals(mPrefs.getFloat(FLOAT_KEY, FloatValue.EMPTY),
                mDelegate.getFloat(FLOAT_KEY, FloatValue.EMPTY), 0f);
    }

    @Test
    public void testPutFloat() throws Exception {
        mDelegate.putFloat(FLOAT_KEY, 9.99f);
        Assert.assertEquals(9.99f, mPrefs.getFloat(FLOAT_KEY, FloatValue.EMPTY), 0f);
    }

    @Test
    public void testGetStringSet() throws Exception {
        Assert.assertEquals(mPrefs.getStringSet(STRING_SET_KEY, StringSetValue.EMPTY),
                mDelegate.getStringSet(STRING_SET_KEY));
    }

    @Test
    public void testPutStringSet() throws Exception {
        final Set<String> strings = new HashSet<>();
        strings.add("expected");
        mDelegate.putStringSet(STRING_SET_KEY, strings);
        Assert.assertEquals(strings, mPrefs.getStringSet(STRING_SET_KEY, StringSetValue.EMPTY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringList() throws Exception {
        mDelegate.getStringList("string_list_value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutStringList() throws Exception {
        mDelegate.putStringList("string_list_value", Collections.<String>emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetParcelable() throws Exception {
        mDelegate.getParcelable("parcelable_value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutParcelable() throws Exception {
        mDelegate.putParcelable("parcelable_value", Bundle.EMPTY);
    }

    @Test
    public void testRemove() throws Exception {
        mDelegate.remove(STRING_KEY);
        Assert.assertFalse(mPrefs.contains(STRING_KEY));
    }

    @Test
    public void testClear() throws Exception {
        mDelegate.clear();
        Assert.assertTrue(mPrefs.getAll().isEmpty());
    }

}