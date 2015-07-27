package droidkit.sqlite.util;

import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowContentResolver;

import droidkit.BuildConfig;
import droidkit.sqlite.AndroidSQLiteClient;
import droidkit.sqlite.SQLiteClient;
import droidkit.sqlite.SQLiteDb;
import droidkit.sqlite.SQLiteProvider;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteTestEnv {

    public static final String TABLE = "provider_test";

    public static final Uri URI = new Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.APPLICATION_ID)
            .path(TABLE)
            .build();

    private SQLiteTestEnv() {

    }

    public static SQLiteProvider registerProvider() {
        final SQLiteProvider provider = new SQLiteProvider() {
            @Override
            protected SQLiteClient createClient() {
                return new AndroidSQLiteClient(getContext(), null, 1) {
                    @Override
                    protected void onCreate(@NonNull SQLiteDb db) {
                        super.onCreate(db);
                        db.compileStatement("CREATE TABLE " + TABLE + "(_id INTEGER PRIMARY KEY, value TEXT);")
                                .execute();
                    }
                };
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
