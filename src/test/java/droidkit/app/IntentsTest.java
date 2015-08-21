package droidkit.app;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class IntentsTest {

    private static final Answer<ResolveInfo> ANSWER = new ResolveInfoAnswer(
            Collections.<Func1<Intent, Boolean>>singletonList(
                    new Func1<Intent, Boolean>() {
                        @Override
                        public Boolean call(Intent intent) {
                            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction());
                        }
                    }
            ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testHasResolution() throws Exception {
        final Intent intent = Intents.Camera.capturePhoto(null);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertFalse(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testStartActivity() throws Exception {
        final Intent intent = Intents.Common.openUrl("http://google.com");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Mockito.doNothing().when(mContext).startActivity(intent);
        Intents.startActivity(mContext, intent, null);
        Mockito.verify(mContext, Mockito.times(1)).startActivity(intent);
    }

    @Test(expected = ActivityNotFoundException.class)
    public void testStartNonExistingActivity() throws Exception {
        final Intent intent = Intents.Pick.contact();
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenReturn(null);
        Mockito.doThrow(ActivityNotFoundException.class).when(mContext)
                .startActivity(Intent.createChooser(intent, null));
        Intents.startActivity(mContext, intent, null);
    }

    @Test
    public void testStartService() throws Exception {
        final Intent intent = new Intent(mContext, Service.class);
        final ResolveInfo resolveInfo = new ResolveInfo();
        final ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.packageName = BuildConfig.APPLICATION_ID;
        serviceInfo.name = "MockService";
        resolveInfo.serviceInfo = serviceInfo;
        Mockito.when(mContext.getPackageManager().resolveService(intent, 0)).thenReturn(resolveInfo);
        Intents.startService(mContext, Service.class);
        Mockito.verify(mContext, Mockito.times(1)).startService(intent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartNonExistingService() throws Exception {
        final Intent intent = new Intent(mContext, Service.class);
        Mockito.when(mContext.getPackageManager().resolveService(intent, 0)).thenReturn(null);
        Intents.startService(mContext, Service.class);
    }

    static class ResolveInfoAnswer implements Answer<ResolveInfo> {

        private final List<Func1<Intent, Boolean>> mResolutions = new ArrayList<>();

        public ResolveInfoAnswer(List<Func1<Intent, Boolean>> resolutions) {
            mResolutions.addAll(resolutions);
        }

        @Override
        public ResolveInfo answer(InvocationOnMock invocation) throws Throwable {
            for (final Func1<Intent, Boolean> resolution : mResolutions) {
                if (resolution.call(invocation.getArgumentAt(0, Intent.class))) {
                    final ResolveInfo resolveInfo = new ResolveInfo();
                    final ActivityInfo activityInfo = new ActivityInfo();
                    activityInfo.packageName = BuildConfig.APPLICATION_ID;
                    activityInfo.name = resolution.getClass().getSimpleName();
                    resolveInfo.activityInfo = activityInfo;
                    return resolveInfo;
                }
            }
            return null;
        }

    }

}