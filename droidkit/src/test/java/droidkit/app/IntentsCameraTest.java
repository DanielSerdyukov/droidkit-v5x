package droidkit.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;

import java.util.Arrays;

import droidkit.DroidkitTestRunner;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class IntentsCameraTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new CapturePhoto(),
            new CaptureVideo()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testCameraCapturePhoto() throws Exception {
        final Intent intent = Intents.Camera.capturePhoto(null);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testCameraCaptureVideo() throws Exception {
        final Intent intent = Intents.Camera.captureVideo(null);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class CapturePhoto implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(MediaStore.ACTION_IMAGE_CAPTURE, intent.getAction());
        }

    }

    private static class CaptureVideo implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(MediaStore.ACTION_VIDEO_CAPTURE, intent.getAction());
        }

    }
    //endregion

}
