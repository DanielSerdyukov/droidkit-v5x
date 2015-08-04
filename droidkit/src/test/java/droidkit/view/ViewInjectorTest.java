package droidkit.view;

import android.content.Context;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;

/**
 * @author Daniel Serdyukov
 */
@Config(constants = BuildConfig.class)
@RunWith(DroidkitTestRunner.class)
public class ViewInjectorTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testViewInjected() throws Exception {

    }

    private static class ShadowFrame extends FrameLayout {

        public ShadowFrame(Context context) {
            super(context);
            ViewInjector.inject(this, this);
        }

    }

}
