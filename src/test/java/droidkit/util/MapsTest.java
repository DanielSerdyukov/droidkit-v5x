package droidkit.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
public class MapsTest {

    private Map<String, String> mMap;

    @Before
    public void setUp() throws Exception {
        mMap = new HashMap<>();
    }

    @Test
    public void testGetNonNull() throws Exception {
        Assert.assertEquals("value_nn", Maps.getNonNull(mMap, "key", "value_nn"));
    }

    @Test
    public void testPutIfAbsent() throws Exception {
        Assert.assertNull(Maps.putIfAbsent(mMap, "key", "value"));
        Assert.assertEquals("value", Maps.putIfAbsent(mMap, "key", "new_value"));
    }

}