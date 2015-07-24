package droidkit.sqlite;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteProvider extends ContentProvider {

    private static final String APP_DB = "app.db";

    private SQLiteClient mClient;

    @Override
    public boolean onCreate() {
        mClient = createClient();
        return false;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        SQLiteUris.attachInfo(info);
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String where,
                        @Nullable String[] bindArgs, @Nullable String orderBy) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] bindArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String where,
                      @Nullable String[] bindArgs) {
        return 0;
    }

    protected SQLiteClient createClient() {
        return new SQLiteClientAndroid(getContext(), APP_DB, 1);
    }

}
