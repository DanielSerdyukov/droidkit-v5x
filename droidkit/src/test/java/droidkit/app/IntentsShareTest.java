package droidkit.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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
public class IntentsShareTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new ShareText(),
            new ShareImage(),
            new ShareVideo(),
            new ShareAll()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testText() throws Exception {
        final Intent intent = Intents.Share.text("hello share");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testImage() throws Exception {
        final Intent intent = Intents.Share.image(Uri.parse("file://fake_path/cat.jpg"));
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testVideo() throws Exception {
        final Intent intent = Intents.Share.video(Uri.parse("file://fake_path/cat.mpg"));
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testShare() throws Exception {
        final Intent intent = Intents.Share.share("funny cat", Uri.parse("file://fake_path/funny_cat.mp4"));
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class ShareText implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_SEND_MULTIPLE, intent.getAction())
                    && TextUtils.equals("text/*", intent.getType())
                    && !intent.getCharSequenceArrayListExtra(Intent.EXTRA_TEXT).isEmpty();
        }

    }

    private static class ShareImage implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_SEND_MULTIPLE, intent.getAction())
                    && TextUtils.equals("image/*", intent.getType())
                    && !intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM).isEmpty();
        }

    }

    private static class ShareVideo implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_SEND_MULTIPLE, intent.getAction())
                    && TextUtils.equals("video/*", intent.getType())
                    && !intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM).isEmpty();
        }

    }

    private static class ShareAll implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_SEND_MULTIPLE, intent.getAction())
                    && TextUtils.equals("*/*", intent.getType())
                    && !intent.getCharSequenceArrayListExtra(Intent.EXTRA_TEXT).isEmpty()
                    && !intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM).isEmpty();
        }

    }
    //endregion

}
