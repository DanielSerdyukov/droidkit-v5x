package droidkit.log;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;

import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class LogCatLoggerTest {

    private LogCatLogger mLogger;

    @Before
    public void setUp() throws Exception {
        mLogger = Mockito.spy(new LogCatLogger());
    }

    @Test
    public void testLog() throws Exception {
        mLogger.log(Log.DEBUG, "%s", this);
        Mockito.verify(mLogger).log(Log.DEBUG, "%s", this);
    }

    @Test
    public void testThrowing() throws Exception {
        final IllegalArgumentException exception = new IllegalArgumentException();
        mLogger.throwing(exception);
        Mockito.verify(mLogger).throwing(exception);
    }

    @Test
    public void testPrivateLog() throws Exception {
        final IOException exception = new IOException();
        mLogger.throwing(exception);
        Mockito.verify(mLogger).throwing(exception);
    }

}