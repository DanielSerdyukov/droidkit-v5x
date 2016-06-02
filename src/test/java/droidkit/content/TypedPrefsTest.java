package droidkit.content;

import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class TypedPrefsTest {

    private SharedPreferences mPrefs;

    private Settings mSettings;

    @Before
    public void setUp() throws Exception {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        mSettings = TypedPrefs.from(RuntimeEnvironment.application, Settings.class);
    }

    @Test
    public void testSetupDefaults() throws Exception {
        TypedPrefs.setupDefaults(RuntimeEnvironment.application, Preconditions.class);
        final Preconditions prefs = TypedPrefs.from(RuntimeEnvironment.application, Preconditions.class);
        Assert.assertEquals(10, prefs.version().get());
        Assert.assertEquals("test", prefs.name().get());
        Assert.assertTrue(prefs.enabled().get());
        Assert.assertEquals(100L, prefs.time().get());
        Assert.assertEquals(200.5f, prefs.distance().get(), 0f);
    }

    @Test
    public void testIntValue() throws Exception {
        final IntValue version = mSettings.version();
        Assert.assertNotNull(version);
        Assert.assertEquals(IntValue.EMPTY, version.get());
        version.set(123);
        Assert.assertEquals(123, version.get());
        Assert.assertEquals(mPrefs.getInt("version", IntValue.EMPTY), version.get());
    }

    @Test
    public void testIntWithParameters() throws Exception {
        final IntValue value1 = mSettings.increment("type1");
        final IntValue value2 = mSettings.increment("type2");
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);
        Assert.assertEquals(IntValue.EMPTY, value1.get());
        Assert.assertEquals(IntValue.EMPTY, value2.get());
        value1.set(100);
        Assert.assertEquals(100, value1.get());
        Assert.assertEquals(IntValue.EMPTY, value2.get());
    }

    @Test
    public void testStringValue() throws Exception {
        final StringValue name = mSettings.name();
        Assert.assertNotNull(name);
        Assert.assertEquals(StringValue.EMPTY, name.get());
        Assert.assertEquals("def", name.get("def"));
        name.set("John");
        Assert.assertEquals("John", name.get());
        Assert.assertEquals(mPrefs.getString("name", StringValue.EMPTY), name.get());
    }

    @Test
    public void testStringWithParameters() throws Exception {
        final StringValue value1 = mSettings.value("type1");
        final StringValue value2 = mSettings.value("type2");
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);
        Assert.assertEquals(StringValue.EMPTY, value1.get());
        Assert.assertEquals(StringValue.EMPTY, value2.get());
        value1.set("value");
        Assert.assertEquals("value", value1.get());
        Assert.assertEquals(StringValue.EMPTY, value2.get());
    }

    @Test
    public void testBoolValue() throws Exception {
        final BoolValue enabled = mSettings.enabled();
        Assert.assertNotNull(enabled);
        Assert.assertEquals(BoolValue.EMPTY, enabled.get());
        enabled.set(true);
        Assert.assertTrue(enabled.get());
        Assert.assertEquals(mPrefs.getBoolean("enabled", BoolValue.EMPTY), enabled.get());
        Assert.assertTrue(enabled.toggle());
        Assert.assertFalse(enabled.get());
        Assert.assertEquals(mPrefs.getBoolean("enabled", BoolValue.EMPTY), enabled.get());
    }

    @Test
    public void testBoolWithParameters() throws Exception {
        final BoolValue value1 = mSettings.flag(0);
        final BoolValue value2 = mSettings.flag(1);
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);
        Assert.assertEquals(BoolValue.EMPTY, value1.get());
        Assert.assertEquals(BoolValue.EMPTY, value2.get());
        value1.set(true);
        Assert.assertEquals(true, value1.get());
        Assert.assertEquals(BoolValue.EMPTY, value2.get());
    }

    @Test
    public void testLongValue() throws Exception {
        final LongValue time = mSettings.time();
        Assert.assertNotNull(time);
        Assert.assertEquals(LongValue.EMPTY, time.get());
        final long expected = SystemClock.uptimeMillis();
        time.set(expected);
        Assert.assertEquals(expected, time.get());
        Assert.assertEquals(mPrefs.getLong("time", LongValue.EMPTY), time.get());
    }

    @Test
    public void testLongWithParameters() throws Exception {
        final LongValue value1 = mSettings.cash("Mike");
        final LongValue value2 = mSettings.cash("Josh");
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);
        Assert.assertEquals(LongValue.EMPTY, value1.get());
        Assert.assertEquals(LongValue.EMPTY, value2.get());
        value1.set(100L);
        Assert.assertEquals(100L, value1.get());
        Assert.assertEquals(LongValue.EMPTY, value2.get());
    }

    @Test
    public void testFloatValue() throws Exception {
        final FloatValue distance = mSettings.distance();
        Assert.assertNotNull(distance);
        Assert.assertEquals(FloatValue.EMPTY, distance.get(), 0f);
        distance.set(100f);
        Assert.assertEquals(100f, distance.get(), 0f);
        Assert.assertEquals(mPrefs.getFloat("distance", FloatValue.EMPTY), distance.get(), 0f);
    }

    @Test
    public void testFloatWithParameters() throws Exception {
        final FloatValue value1 = mSettings.probability(true);
        final FloatValue value2 = mSettings.probability(false);
        Assert.assertNotNull(value1);
        Assert.assertNotNull(value2);
        Assert.assertEquals(FloatValue.EMPTY, value1.get(), 0f);
        Assert.assertEquals(FloatValue.EMPTY, value2.get(), 0f);
        value1.set(100f);
        Assert.assertEquals(100f, value1.get(), 0f);
        Assert.assertEquals(FloatValue.EMPTY, value2.get(), 0f);
    }

    @Test
    public void testStringSetValue() throws Exception {
        final StringSetValue lines = mSettings.lines();
        Assert.assertNotNull(lines);
        final Set<String> newLines = new HashSet<>();
        newLines.add("1");
        newLines.add("2");
        newLines.add("3");
        lines.set(newLines);
        Assert.assertEquals(newLines, lines.get());
        Assert.assertEquals(mPrefs.getStringSet("lines", Collections.<String>emptySet()), lines.get());
    }

    @After
    public void tearDown() throws Exception {
        mPrefs.edit().clear().apply();
    }

    private interface Settings {

        IntValue version();

        StringValue name();

        BoolValue enabled();

        LongValue time();

        FloatValue distance();

        StringSetValue lines();

        IntValue increment(String type);

        StringValue value(String type);

        BoolValue flag(int id);

        LongValue cash(String user);

        FloatValue probability(boolean colored);
    }

    private interface Preconditions {

        @Value(intValue = 10)
        IntValue version();

        @Value(stringValue = "test")
        StringValue name();

        @Value(boolValue = true)
        BoolValue enabled();

        @Value(longValue = 100L)
        LongValue time();

        @Value(floatValue = 200.5f)
        FloatValue distance();

    }

}
