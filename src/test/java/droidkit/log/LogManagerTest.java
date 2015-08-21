package droidkit.log;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class LogManagerTest {

    private LogManager mLm;

    @Before
    public void setUp() throws Exception {
        mLm = Mockito.spy(LogManager.get());
    }

    @Test
    public void testGet() throws Exception {
        Assert.assertEquals(LogManager.get(), LogManager.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLogger() throws Exception {
        Mockito.doThrow(IllegalArgumentException.class)
                .when(mLm)
                .addLogger(LogManagerTest.class, Logger.NONE);
        mLm.addLogger(LogManagerTest.class, Logger.NONE);
    }

    @Test
    public void testGetLogger() throws Exception {
        mLm.addLogger(LogManagerTest.class, Logger.NONE);
        Assert.assertEquals(Logger.NONE, mLm.getLogger(LogManagerTest.class));
    }

}