package droidkit.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.ContactsContract;
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
import droidkit.util.Objects;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class IntentsPickTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new PickContact(),
            new PickFile(),
            new PickImage(),
            new PickVideo()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testPickContact() throws Exception {
        final Intent intent = Intents.Pick.contact();
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testPickFile() throws Exception {
        final Intent intent = Intents.Pick.file("*/*");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testPickImage() throws Exception {
        final Intent intent = Intents.Pick.image();
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testPickVideo() throws Exception {
        final Intent intent = Intents.Pick.video();
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class PickContact implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_PICK, intent.getAction()) &&
                    Objects.equal(ContactsContract.Contacts.CONTENT_URI, intent.getData());
        }

    }

    private static class PickFile implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_GET_CONTENT, intent.getAction()) &&
                    TextUtils.equals("*/*", intent.getType());
        }

    }

    private static class PickImage implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_PICK, intent.getAction()) &&
                    TextUtils.equals(MediaStore.Images.Media.CONTENT_TYPE, intent.getType());
        }

    }

    private static class PickVideo implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_PICK, intent.getAction()) &&
                    TextUtils.equals(MediaStore.Video.Media.CONTENT_TYPE, intent.getType());
        }

    }
    //endregion

}
