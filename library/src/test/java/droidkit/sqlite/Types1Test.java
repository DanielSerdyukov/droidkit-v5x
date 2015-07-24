package droidkit.sqlite;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class Types1Test {

    private static final String[] COLUMNS = ("_id, long_type, int_type, short_type, string_type, boolean_type, " +
            "double_type, float_type, big_decimal, big_integer, date_time, byte_array, role").split(", ");

    private Object[] mObjects;

    @Before
    public void setUp() throws Exception {
        final SecureRandom random = new SecureRandom();
        mObjects = new Object[]{
                random.nextLong(),
                random.nextLong(),
                random.nextInt(),
                (short) random.nextInt(Short.MAX_VALUE / 2),
                "test",
                random.nextBoolean(),
                random.nextDouble(),
                random.nextFloat(),
                BigDecimal.valueOf(random.nextDouble()),
                BigInteger.valueOf(random.nextLong()),
                DateTime.now(),
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9},
                Types1.Role.ADMIN
        };
    }

    @Test
    public void testId() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

}
