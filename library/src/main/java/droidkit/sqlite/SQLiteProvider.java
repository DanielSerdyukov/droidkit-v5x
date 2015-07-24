package droidkit.sqlite;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteProvider extends ContentProvider {

    private static final List<Class<?>> HELPERS = new CopyOnWriteArrayList<>();

    private static final String APP_DB = "app.db";

    private static final String MIME_DIR = "vnd.android.cursor.dir/";

    private static final String MIME_ITEM = "vnd.android.cursor.item/";

    private static final int URI_MATCH_ALL = 1;

    private static final int URI_MATCH_ID = 2;

    private SQLiteClient mClient;

    static void attachHelper(@NonNull Class<?> helper) {
        HELPERS.add(helper);
    }

    private static int matchUri(@NonNull Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        final int pathSegmentsSize = pathSegments.size();
        if (pathSegmentsSize == 1) {
            return URI_MATCH_ALL;
        } else if (pathSegmentsSize == 2
                && TextUtils.isDigitsOnly(pathSegments.get(1))) {
            return URI_MATCH_ID;
        }
        throw new SQLiteException("Unknown uri '" + uri + "'");
    }

    @Override
    public boolean onCreate() {

        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        mClient = createClient();
        SQLiteSchema.attachInfo(info);
        for (final Class<?> helper : HELPERS) {
            try {
                MethodLookup.local()
                        .find(helper, "attachInfo", SQLiteClient.class)
                        .invokeStatic(mClient);
            } catch (DynamicException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String where,
                        @Nullable String[] bindArgs, @Nullable String orderBy) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if (matchUri(uri) == URI_MATCH_ID) {
            return MIME_ITEM + SQLiteSchema.tableOf(uri);
        }
        return MIME_DIR + SQLiteSchema.tableOf(uri);
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
