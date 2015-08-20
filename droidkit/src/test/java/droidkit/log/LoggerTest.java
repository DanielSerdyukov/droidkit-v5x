package droidkit.log;

import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerTest {

    private static final Logger LOGGER;

    static {
        LOGGER = Mockito.spy(Logger.NONE);
        LogManager.get().setGlobalLogger(LOGGER);
    }

    @Test
    public void testDebug() throws Exception {
        Logger.debug(LoggerTest.class, "%d", Log.DEBUG);
        Mockito.verify(LOGGER).log(Log.DEBUG, "%d", Log.DEBUG);
    }

    @Test
    public void testInfo() throws Exception {
        Logger.info(LoggerTest.class, "%d", Log.INFO);
        Mockito.verify(LOGGER).log(Log.INFO, "%d", Log.INFO);
    }

    @Test
    public void testWarn() throws Exception {
        Logger.warn(LoggerTest.class, "%d", Log.WARN);
        Mockito.verify(LOGGER).log(Log.WARN, "%d", Log.WARN);
    }

    @Test
    public void testError() throws Exception {
        Logger.error(LoggerTest.class, "%d", Log.ERROR);
        Mockito.verify(LOGGER).log(Log.ERROR, "%d", Log.ERROR);
    }

    @Test
    public void testThrowing() throws Exception {
        final IOException exception = new IOException();
        Logger.error(LoggerTest.class, exception);
        Mockito.verify(LOGGER).throwing(exception);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethods() throws Exception {
        final IOException exception = new IOException();
        Logger.debug("%d", Log.DEBUG);
        Logger.info("%d", Log.INFO);
        Logger.warn("%d", Log.WARN);
        Logger.error("%d", Log.ERROR);
        Logger.wtf("%d", Log.DEBUG);
        Logger.error(exception);
        Mockito.verify(LOGGER, Mockito.times(3)).log(Log.DEBUG, "%d", Log.DEBUG);
        Mockito.verify(LOGGER, Mockito.times(2)).log(Log.INFO, "%d", Log.INFO);
        Mockito.verify(LOGGER, Mockito.times(2)).log(Log.WARN, "%d", Log.WARN);
        Mockito.verify(LOGGER, Mockito.times(2)).log(Log.ERROR, "%d", Log.ERROR);
        Mockito.verify(LOGGER).throwing(exception);
    }

}