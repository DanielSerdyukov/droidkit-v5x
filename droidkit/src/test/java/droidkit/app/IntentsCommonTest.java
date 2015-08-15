package droidkit.app;

import android.app.SearchManager;
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
public class IntentsCommonTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new OpenUrl(),
            new OpenDialer(),
            new WebSearch(),
            new SendMail(),
            new SendSms(),
            new OpenContent()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testOpenDialer() throws Exception {
        final Intent intent = Intents.Common.openDialer("+1234567890");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testSearch() throws Exception {
        final Intent intent = Intents.Common.search("android");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testSendEmail() throws Exception {
        final Intent intent = Intents.Common.sendEmail(new String[]{"foo@bar.baz"}, "foo", "bar",
                Uri.parse("file://fake_path"));
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testOpenUrl() throws Exception {
        final Intent intent = Intents.Common.openUrl("https://google.com");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testSendSms() throws Exception {
        final Intent intent = Intents.Common.sendSms("+1234567890", "mock sms");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testOpenContent() throws Exception {
        final Intent intent = Intents.Common.openContent(Uri.parse("content://fake_path/cat.jpg"));
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class OpenUrl implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals("https", intent.getData().getScheme());
        }

    }

    private static class OpenDialer implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_DIAL, intent.getAction())
                    && TextUtils.equals("tel", intent.getData().getScheme());
        }

    }

    private static class WebSearch implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_WEB_SEARCH, intent.getAction())
                    && intent.getExtras().containsKey(SearchManager.QUERY);
        }

    }

    private static class SendMail implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_SEND_MULTIPLE, intent.getAction())
                    && TextUtils.equals("message/rfc822", intent.getType())
                    && intent.getExtras().containsKey(Intent.EXTRA_EMAIL)
                    && intent.getExtras().containsKey(Intent.EXTRA_SUBJECT)
                    && intent.getExtras().containsKey(Intent.EXTRA_TEXT);
        }

    }

    private static class SendSms implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals("sms", intent.getData().getScheme())
                    && intent.getExtras().containsKey("sms_body");
        }

    }

    private static class OpenContent implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals("image/jpeg", intent.getType());
        }

    }
    //endregion

}
