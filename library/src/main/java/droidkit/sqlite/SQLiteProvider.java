package droidkit.sqlite;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import droidkit.util.Iterables;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteProvider extends ContentProvider {

    private static final int URI_MATCH_ALL = 1;

    private static final int URI_MATCH_ID = 2;

    private static final String MIME_DIR = "vnd.android.cursor.dir/";

    private static final String MIME_ITEM = "vnd.android.cursor.item/";

    private SQLiteClient mClient;

    private static int matchUri(@NonNull Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        final int pathSegmentsSize = pathSegments.size();
        if (pathSegmentsSize == 1) {
            return URI_MATCH_ALL;
        } else if (pathSegmentsSize == 2 && TextUtils.isDigitsOnly(pathSegments.get(1))) {
            return URI_MATCH_ID;
        }
        throw new SQLiteException("Unknown uri '" + uri + "'");
    }

    @NonNull
    private static String tableOf(@NonNull Uri uri) {
        return Iterables.getFirst(uri.getPathSegments());
    }

    @NonNull
    private static Uri baseUriOf(@NonNull Uri uri) {
        return uri.buildUpon().path(tableOf(uri)).build();
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        SQLite.attach(info.authority);
    }

    @Override
    public boolean onCreate() {
        mClient = SQLite.of(getContext()).getClient();
        return true;
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String where,
                        @Nullable String[] bindArgs, @Nullable String orderBy) {
        if (matchUri(uri) == URI_MATCH_ID) {
            final Cursor cursor = mClient.query(tableOf(uri), columns, SQLiteQuery.WHERE_ID_EQ,
                    new String[]{uri.getLastPathSegment()}, orderBy);
            cursor.setNotificationUri(getContext().getContentResolver(), baseUriOf(uri));
            return cursor;
        }
        final Cursor cursor = mClient.query(tableOf(uri), columns, where, bindArgs, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    @Override
    public String getType(@NonNull Uri uri) {
        if (matchUri(uri) == URI_MATCH_ID) {
            return MIME_ITEM + tableOf(uri);
        }
        return MIME_DIR + tableOf(uri);
    }

    @NonNull
    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {
        Uri baseUri = uri;
        if (matchUri(uri) == URI_MATCH_ID) {
            values.put(BaseColumns._ID, uri.getLastPathSegment());
            baseUri = baseUriOf(uri);
        }
        final long rowId = mClient.insert(tableOf(uri), values);
        getContext().getContentResolver().notifyChange(baseUri, null, shouldSyncToNetwork(baseUri));
        return ContentUris.withAppendedId(baseUri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] bindArgs) {
        Uri baseUri = uri;
        int affectedRows;
        if (matchUri(uri) == URI_MATCH_ID) {
            affectedRows = mClient.delete(tableOf(uri), SQLiteQuery.WHERE_ID_EQ,
                    new String[]{uri.getLastPathSegment()});
            baseUri = baseUriOf(uri);
        } else {
            affectedRows = mClient.delete(tableOf(uri), where, bindArgs);
        }
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(baseUri, null, shouldSyncToNetwork(baseUri));
        }
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @Nullable String where,
                      @Nullable String[] bindArgs) {
        Uri baseUri = uri;
        int affectedRows;
        if (matchUri(uri) == URI_MATCH_ID) {
            affectedRows = mClient.update(tableOf(uri), values, SQLiteQuery.WHERE_ID_EQ,
                    new String[]{uri.getLastPathSegment()});
            baseUri = baseUriOf(uri);
        } else {
            affectedRows = mClient.update(tableOf(uri), values, where, bindArgs);
        }
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(baseUri, null, shouldSyncToNetwork(baseUri));
        }
        return affectedRows;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] bulkValues) {
        Uri baseUri = uri;
        if (matchUri(uri) == URI_MATCH_ID) {
            baseUri = baseUriOf(uri);
        }
        mClient.beginTransaction();
        final String tableName = tableOf(uri);
        for (final ContentValues values : bulkValues) {
            mClient.insert(tableName, values);
        }
        mClient.endTransaction(true);
        getContext().getContentResolver().notifyChange(baseUri, null, shouldSyncToNetwork(baseUri));
        return bulkValues.length;
    }

    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        return super.applyBatch(operations);
    }

    protected boolean shouldSyncToNetwork(@NonNull Uri uri) {
        return false;
    }

}
