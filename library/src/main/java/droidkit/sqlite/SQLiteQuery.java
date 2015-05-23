package droidkit.sqlite;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.util.DynamicException;
import droidkit.util.DynamicMethod;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> {

    //region operators and values
    private static final String EQ = " = ?";

    private static final String NOT_EQ = " <> ?";

    private static final String LT = " < ?";

    private static final String LT_OR_EQ = " <= ?";

    private static final String GT = " > ?";

    private static final String GT_OR_EQ = " >= ?";

    private static final String LIKE = " LIKE ?";

    private static final String BETWEEN = " BETWEEN ? AND ?";

    private static final int TRUE = 1;

    private static final int FALSE = 0;

    private static final String IS_NULL = " IS NULL";

    private static final String NOT_NULL = " NOT NULL";

    private static final String COMMA = ", ";
    //endregion

    private final WeakReference<SQLiteClient> mClient;

    private final Class<T> mType;

    private final StringBuilder mWhere = new StringBuilder();

    private final List<Object> mWhereArgs = new ArrayList<>();

    public SQLiteQuery(@NonNull SQLiteClient client, @NonNull Class<T> type) {
        mClient = new WeakReference<>(client);
        mType = type;
    }

    //region where conditions
    @NonNull
    public SQLiteQuery<T> equalTo(@NonNull String column, @NonNull Object value) {
        return where(column, EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> notEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, NOT_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThan(@NonNull String column, @NonNull Object value) {
        return where(column, LT, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, LT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThan(@NonNull String column, @NonNull Object value) {
        return where(column, GT, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return where(column, GT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> like(@NonNull String column, @NonNull Object value) {
        return where(column, LIKE, DatabaseUtils.sqlEscapeString(String.valueOf(value)));
    }

    @NonNull
    public SQLiteQuery<T> between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return where(column, BETWEEN, lv, rv);
    }

    @NonNull
    public SQLiteQuery<T> isTrue(@NonNull String column) {
        return equalTo(column, TRUE);
    }

    @NonNull
    public SQLiteQuery<T> isFalse(@NonNull String column) {
        return equalTo(column, FALSE);
    }

    @NonNull
    public SQLiteQuery<T> isNull(@NonNull String column) {
        mWhere.append(column).append(IS_NULL);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> notNull(@NonNull String column) {
        mWhere.append(column).append(NOT_NULL);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> appendWhere(@NonNull String where, @NonNull Object... bindArgs) {
        mWhere.append(where);
        Collections.addAll(mWhereArgs, bindArgs);
        return this;
    }
    //endregion

    @Nullable
    public T one() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public List<T> list() {
        return SQLiteResultReference.wrap(new SQLiteResult<>(this, cursor()));
    }

    @NonNull
    public Cursor cursor() {
        return mClient.get().rawQuery(toString(), bindArgsAsString());
    }

    public int remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        String where = null;
        if (mWhere.length() > 0) {
            where = mWhere.toString();
        }
        return SQLiteQueryBuilder.buildQueryString(false, SQLite.tableOf(mType), null, where, null, null, null, null);
    }

    //region package internal
    Object[] bindArgs() {
        if (!mWhereArgs.isEmpty()) {
            return mWhereArgs.toArray(new Object[mWhereArgs.size()]);
        }
        return null;
    }

    String[] bindArgsAsString() {
        if (!mWhereArgs.isEmpty()) {
            final String[] bindArgs = new String[mWhereArgs.size()];
            for (int i = 0; i < mWhereArgs.size(); ++i) {
                bindArgs[0] = String.valueOf(mWhereArgs.get(i));
            }
            return bindArgs;
        }
        return null;
    }

    @NonNull
    T instantiate(@NonNull Cursor cursor) {
        try {
            //noinspection ConstantConditions
            return DynamicMethod.invokeStatic(mType, "of", mClient.get(), cursor);
        } catch (DynamicException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @NonNull
    String getTableName() {
        return SQLite.tableOf(mType);
    }

    @NonNull
    SQLiteClient getClient() {
        return mClient.get();
    }

    private SQLiteQuery<T> where(@NonNull String column, @NonNull String op, @NonNull Object... values) {
        mWhere.append(column).append(op);
        Collections.addAll(mWhereArgs, values);
        return this;
    }
    //endregion

}
