package droidkit.sqlite;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import droidkit.util.Iterables;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteProvider extends ContentProvider {

    private static final String DATABASE_NAME = "data.db";

    private static final int DATABASE_VERSION = 1;

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
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);
        mClient = new AndroidSQLiteClient(context, getDatabaseName(), getDatabaseVersion(),
                new SQLiteClientCallbacksImpl());
        SQLite.initWithClient(context, mClient, info);
    }

    @Override
    public boolean onCreate() {
        return mClient != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] columns, @Nullable String where,
                        @Nullable String[] whereArgs, @Nullable String orderBy) {
        if (URI_MATCH_ID == matchUri(uri)) {
            where = SQLiteQuery.WHERE_ID_EQ;
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        final String sql = SQLiteQueryBuilder.buildQueryString(false, tableOf(uri), columns, where,
                null, null, orderBy, null);
        final Cursor cursor;
        if (whereArgs == null) {
            cursor = mClient.query(sql);
        } else {
            cursor = mClient.query(sql, whereArgs);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), baseUriOf(uri));
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if (matchUri(uri) == URI_MATCH_ID) {
            return MIME_ITEM + tableOf(uri);
        }
        return MIME_DIR + tableOf(uri);
    }

    @Override
    public Uri insert(@NonNull Uri uri, @NonNull ContentValues values) {
        if (URI_MATCH_ID == matchUri(uri)) {
            values.put(BaseColumns._ID, uri.getLastPathSegment());
        }
        final StringBuilder sql = new StringBuilder()
                .append("INSERT INTO ")
                .append(tableOf(uri))
                .append("(");
        final Object[] bindArgs = new Object[values.size()];
        int i = 0;
        for (final String column : values.keySet()) {
            sql.append((i > 0) ? "," : "");
            sql.append(column);
            bindArgs[i++] = values.get(column);
        }
        sql.append(')');
        sql.append(" VALUES (");
        for (i = 0; i < bindArgs.length; ++i) {
            sql.append((i > 0) ? ", ?" : "?");
        }
        sql.append(");");
        final Uri rowIdUri = ContentUris.withAppendedId(baseUriOf(uri),
                mClient.executeInsert(sql.toString(), bindArgs));
        getContext().getContentResolver().notifyChange(uri, null, shouldSyncToNetwork(rowIdUri));
        return rowIdUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] whereArgs) {
        if (URI_MATCH_ID == matchUri(uri)) {
            where = SQLiteQuery.WHERE_ID_EQ;
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        final StringBuilder sql = new StringBuilder()
                .append("DELETE FROM ")
                .append(tableOf(uri));
        if (where != null) {
            sql.append(" WHERE ").append(where).append(";");
        }
        int affectedRows;
        if (whereArgs == null) {
            affectedRows = mClient.executeUpdateDelete(sql.toString());
        } else {
            affectedRows = mClient.executeUpdateDelete(sql.toString(), whereArgs);
        }
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null, shouldSyncToNetwork(uri));
        }
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @Nullable String where,
                      @Nullable String[] whereArgs) {
        final List<Object> bindArgs = new ArrayList<>(values.size() * 2);
        if (URI_MATCH_ID == matchUri(uri)) {
            where = SQLiteQuery.WHERE_ID_EQ;
            whereArgs = new String[]{uri.getLastPathSegment()};
        }
        final StringBuilder sql = new StringBuilder()
                .append("UPDATE ")
                .append(tableOf(uri))
                .append(" SET ");
        final Iterator<String> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            final String column = iterator.next();
            sql.append(column).append(" = ?");
            if (iterator.hasNext()) {
                sql.append(", ");
            }
            bindArgs.add(values.get(column));
        }
        if (where != null) {
            sql.append(" WHERE ").append(where).append(";");
        }
        if (whereArgs != null) {
            Collections.addAll(bindArgs, whereArgs);
        }
        final int affectedRows = mClient.executeUpdateDelete(sql.toString(),
                bindArgs.toArray(new Object[bindArgs.size()]));
        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(uri, null, shouldSyncToNetwork(uri));
        }
        return affectedRows;
    }

    @Nullable
    protected String getDatabaseName() {
        return DATABASE_NAME;
    }

    protected int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    protected void onDatabaseConfigure(@NonNull SQLiteDatabaseWrapper db) {
    }

    protected void onDatabaseCreate(@NonNull SQLiteDatabaseWrapper db) {
    }

    protected void onDatabaseUpgrade(@NonNull SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
    }

    protected boolean shouldSyncToNetwork(@NonNull Uri uri) {
        return false;
    }

    private class SQLiteClientCallbacksImpl implements SQLiteClient.Callbacks {

        @Override
        public void onConfigure(@NonNull SQLiteDatabaseWrapper db) {
            onDatabaseConfigure(db);
        }

        @Override
        public void onCreate(@NonNull SQLiteDatabaseWrapper db) {
            onDatabaseCreate(db);
        }

        @Override
        public void onUpgrade(@NonNull SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
            onDatabaseUpgrade(db, oldVersion, newVersion);
        }

    }

}
