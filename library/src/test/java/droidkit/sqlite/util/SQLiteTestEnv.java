package droidkit.sqlite.util;

import android.content.pm.ProviderInfo;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowContentResolver;

import droidkit.BuildConfig;
import droidkit.sqlite.SQLiteClient;
import droidkit.sqlite.SQLiteClientAndroid;
import droidkit.sqlite.SQLiteProvider;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteTestEnv {

    private SQLiteTestEnv() {

    }

    public static SQLiteProvider registerProvider() {
        final SQLiteProvider provider = new SQLiteProvider() {
            @Override
            protected SQLiteClient createClient() {
                return new SQLiteClientAndroid(getContext(), null, 1);
            }
        };
        final ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.name = SQLiteProvider.class.getName();
        providerInfo.authority = BuildConfig.APPLICATION_ID;
        provider.attachInfo(RuntimeEnvironment.application, providerInfo);
        provider.onCreate();
        ShadowContentResolver.registerProvider(BuildConfig.APPLICATION_ID, provider);
        return provider;
    }

}
