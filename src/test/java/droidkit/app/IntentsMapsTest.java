package droidkit.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
public class IntentsMapsTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new OpenMaps(),
            new OpenMapsInPoint(),
            new RouteTo(),
            new RouteFromTo(),
            new Search()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
    }

    @Test
    public void testMapsOpenMaps() throws Exception {
        final Intent intent = Intents.Maps.openMaps();
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMapsOpenMapsInPoint() throws Exception {
        final Intent intent = Intents.Maps.openMaps(60.4923, 30.7765);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMapsRouteTo() throws Exception {
        final Intent intent = Intents.Maps.route(60d, 30d);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMapsRouteFromTo() throws Exception {
        final Intent intent = Intents.Maps.route(60.40d, 40.60d, 50.50d, 35.70d);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMapsSearch() throws Exception {
        final Intent intent = Intents.Maps.search("Saint-Petersburg");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class OpenMaps implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && Objects.equal(Intents.Maps.MAPS_URI, intent.getData());
        }

    }

    private static class OpenMapsInPoint implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals(Intents.Maps.formatLatLng(60.4923, 30.7765),
                    intent.getData().getQueryParameter("q"));
        }

    }

    private static class RouteTo implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals(Intents.Maps.formatLatLng(60d, 30d),
                    intent.getData().getQueryParameter("daddr"));
        }

    }

    private static class RouteFromTo implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals(Intents.Maps.formatLatLng(60.40d, 40.60d),
                    intent.getData().getQueryParameter("saddr"))
                    && TextUtils.equals(Intents.Maps.formatLatLng(50.50d, 35.70d),
                    intent.getData().getQueryParameter("daddr"));
        }

    }

    private static class Search implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            return TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && intent.getData() != null
                    && TextUtils.equals("Saint-Petersburg", intent.getData().getQueryParameter("q"));
        }

    }
    //endregion

}
