package droidkit.sqlite;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.After;
import org.junit.Before;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowContentResolver;

import droidkit.BuildConfig;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteTestCase {

    private SQLiteProvider mProvider;

    @Before
    public void setUp() throws Exception {
        mProvider = createProvider();
        final ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.name = SQLiteProvider.class.getName();
        providerInfo.authority = BuildConfig.APPLICATION_ID;
        mProvider.attachInfo(RuntimeEnvironment.application, providerInfo);
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(BuildConfig.APPLICATION_ID, mProvider);
    }

    @After
    public void tearDown() throws Exception {
        mProvider.shutdown();
    }

    @NonNull
    protected SQLiteProvider getProvider() {
        return mProvider;
    }

    @NonNull
    protected SQLiteProvider createProvider() {
        return new SQLiteProvider() {
            @Nullable
            @Override
            protected String getDatabaseName() {
                return null;
            }

            @Override
            SQLiteClient createClient(@NonNull Context context) {
                return new SQLiteClientImpl(context, getDatabaseName(), getDatabaseVersion(), this);
            }
        };
    }

}
