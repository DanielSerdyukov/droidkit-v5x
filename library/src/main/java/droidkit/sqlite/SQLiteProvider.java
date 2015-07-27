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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;
import droidkit.util.Sets;
import rx.functions.Func1;

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
        throw new SQLiteException("Unknown uri '%s'", uri);
    }

    @Override
    public boolean onCreate() {
        createClientIfNecessary();
        return true;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        createClientIfNecessary();
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
        final String table = SQLiteSchema.tableOf(uri);
        final Cursor cursor;
        Uri notificationUri = uri;
        if (URI_MATCH_ID == matchUri(uri)) {
            cursor = mClient.query(SQLiteQueryBuilder.buildQueryString(false, table, null, SQLiteOp.ID_EQ,
                    null, null, orderBy, null), new String[]{uri.getLastPathSegment()});
            notificationUri = SQLiteSchema.baseUri(uri, table);
        } else {
            cursor = mClient.getReadableDatabase().query(SQLiteQueryBuilder.buildQueryString(false, table, columns,
                    where, null, null, orderBy, null), bindArgs);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), notificationUri);
        return cursor;
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
        final String table = SQLiteSchema.tableOf(uri);
        Uri notificationUri = uri;
        if (URI_MATCH_ID == matchUri(uri)) {
            values.put(BaseColumns._ID, uri.getLastPathSegment());
            notificationUri = SQLiteSchema.baseUri(uri, table);
        }
        final Set<String> columns = values.keySet();
        final long rowId = mClient.executeInsert("INSERT INTO " + table + "(" + TextUtils.join(", ", columns) + ")" +
                        " VALUES" + "(" + TextUtils.join(", ", Collections.nCopies(columns.size(), "?")) + ");",
                Sets.toArray(Sets.transform(values.valueSet(),
                        new Func1<Map.Entry<String, Object>, Object>() {
                            @Override
                            public Object call(Map.Entry<String, Object> entry) {
                                return entry.getValue();
                            }
                        }), Object.class));
        if (shouldNotifyChange(notificationUri)) {
            getContext().getContentResolver().notifyChange(notificationUri, null, shouldSyncToNetwork(notificationUri));
        }
        return ContentUris.withAppendedId(notificationUri, rowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] bindArgs) {
        final String table = SQLiteSchema.tableOf(uri);
        if (URI_MATCH_ID == matchUri(uri)) {
            where = SQLiteOp.ID_EQ;
            bindArgs = new String[]{uri.getLastPathSegment()};
        }
        final StringBuilder sql = new StringBuilder()
                .append("DELETE FROM ")
                .append(table);
        if (where != null) {
            sql.append(" WHERE ").append(where).append(";");
        }
        int affectedRows;
        if (bindArgs == null) {
            affectedRows = mClient.executeUpdateDelete(sql.toString());
        } else {
            affectedRows = mClient.executeUpdateDelete(sql.toString(), (Object[]) bindArgs);
        }
        if (affectedRows > 0 && shouldNotifyChange(uri)) {
            getContext().getContentResolver().notifyChange(uri, null, shouldSyncToNetwork(uri));
        }
        return affectedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @NonNull ContentValues values, @Nullable String where,
                      @Nullable String[] bindArgs) {
        final String table = SQLiteSchema.tableOf(uri);
        if (URI_MATCH_ID == matchUri(uri)) {
            where = SQLiteOp.ID_EQ;
            bindArgs = new String[]{uri.getLastPathSegment()};
        }
        final StringBuilder sql = new StringBuilder()
                .append("UPDATE ")
                .append(table)
                .append(" SET ");
        sql.append(TextUtils.join(", ", Sets.transform(values.keySet(), new Func1<String, String>() {
            @Override
            public String call(String column) {
                return column + " = ?";
            }
        })));
        if (where != null) {
            sql.append(" WHERE ").append(where).append(";");
        }
        final Set<Object> bindValues = Sets.transform(values.valueSet(),
                new Func1<Map.Entry<String, Object>, Object>() {
                    @Override
                    public Object call(Map.Entry<String, Object> entry) {
                        return entry.getValue();
                    }
                });
        if (bindArgs != null) {
            Collections.addAll(bindValues, bindArgs);
        }
        final int affectedRows = mClient.executeUpdateDelete(sql.toString(), Sets.toArray(bindValues, Object.class));
        if (affectedRows > 0 && shouldNotifyChange(uri)) {
            getContext().getContentResolver().notifyChange(uri, null, shouldSyncToNetwork(uri));
        }
        return affectedRows;
    }

    protected SQLiteClient createClient() {
        return new AndroidSQLiteClient(getContext(), APP_DB, 1);
    }

    protected boolean shouldNotifyChange(@NonNull Uri uri) {
        return true;
    }

    protected boolean shouldSyncToNetwork(@NonNull Uri uri) {
        return false;
    }

    private void createClientIfNecessary() {
        if (mClient == null) {
            mClient = createClient();
            mClient.getWritableDatabase();
        }
    }

}
