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

import droidkit.BuildConfig;
import droidkit.DroidkitTestRunner;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
@RunWith(DroidkitTestRunner.class)
public class IntentsPlayStoreTest {

    private static final Answer<ResolveInfo> ANSWER = new IntentsTest.ResolveInfoAnswer(Arrays.asList(
            new DetailsByContext(),
            new DetailsByPackage(),
            new Publisher(),
            new Search(),
            new Apps(),
            new Movies(),
            new Music(),
            new Books()
    ));

    private Context mContext;

    @Before
    public void setUp() throws Exception {
        mContext = Mockito.spy(RuntimeEnvironment.application);
        final PackageManager pm = Mockito.spy(RuntimeEnvironment.application.getPackageManager());
        Mockito.when(mContext.getPackageManager()).thenReturn(pm);
        Mockito.when(mContext.getPackageName()).thenReturn(BuildConfig.APPLICATION_ID);
    }

    @Test
    public void testDetailsByContext() throws Exception {
        final Intent intent = Intents.PlayStore.details(mContext);
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testDetailsByPackage() throws Exception {
        final Intent intent = Intents.PlayStore.details("com.android.chrome");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testPublisher() throws Exception {
        final Intent intent = Intents.PlayStore.publisher("Mozilla");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testSearch() throws Exception {
        final Intent intent = Intents.PlayStore.search("hang");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testApps() throws Exception {
        final Intent intent = Intents.PlayStore.apps("inbox");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMovies() throws Exception {
        final Intent intent = Intents.PlayStore.movies("furious");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testMusic() throws Exception {
        final Intent intent = Intents.PlayStore.music("metallica");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    @Test
    public void testBooks() throws Exception {
        final Intent intent = Intents.PlayStore.books("alice");
        Mockito.when(mContext.getPackageManager().resolveActivity(intent, 0)).thenAnswer(ANSWER);
        Assert.assertTrue(Intents.hasResolution(mContext, intent));
    }

    //region resolutions
    private static class DetailsByContext implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.DETAILS, uri.getAuthority())
                    && TextUtils.equals(BuildConfig.APPLICATION_ID, uri.getQueryParameter(Intents.PlayStore.ID));
        }

    }

    private static class DetailsByPackage implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.DETAILS, uri.getAuthority())
                    && TextUtils.equals("com.android.chrome", uri.getQueryParameter(Intents.PlayStore.ID));
        }

    }

    private static class Publisher implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("pub:Mozilla", uri.getQueryParameter(Intents.PlayStore.Q));
        }

    }

    private static class Search implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("hang", uri.getQueryParameter(Intents.PlayStore.Q));
        }

    }

    private static class Apps implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("inbox", uri.getQueryParameter(Intents.PlayStore.Q))
                    && TextUtils.equals(Intents.PlayStore.APPS, uri.getQueryParameter(Intents.PlayStore.C));
        }

    }

    private static class Movies implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("furious", uri.getQueryParameter(Intents.PlayStore.Q))
                    && TextUtils.equals(Intents.PlayStore.MOVIES, uri.getQueryParameter(Intents.PlayStore.C));
        }

    }

    private static class Music implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("metallica", uri.getQueryParameter(Intents.PlayStore.Q))
                    && TextUtils.equals(Intents.PlayStore.MUSIC, uri.getQueryParameter(Intents.PlayStore.C));
        }

    }

    private static class Books implements Func1<Intent, Boolean> {

        @Override
        public Boolean call(Intent intent) {
            final Uri uri = intent.getData();
            return uri != null && TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())
                    && TextUtils.equals(Intents.PlayStore.MARKET, uri.getScheme())
                    && TextUtils.equals(Intents.PlayStore.SEARCH, uri.getAuthority())
                    && TextUtils.equals("alice", uri.getQueryParameter(Intents.PlayStore.Q))
                    && TextUtils.equals(Intents.PlayStore.BOOKS, uri.getQueryParameter(Intents.PlayStore.C));
        }

    }
    //endregion

}
