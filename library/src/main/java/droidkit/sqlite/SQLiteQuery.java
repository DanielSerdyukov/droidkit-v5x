package droidkit.sqlite;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.content.StringValue;
import rx.Observable;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> implements SQLiteOp {

    private final Class<T> mType;

    private final StringBuilder mWhere = new StringBuilder();

    private final List<Object> mBindArgs = new ArrayList<>();

    private final List<String> mOrderBy = new ArrayList<>();

    private boolean mDistinct;

    private String mGroupBy;

    private String mHaving;

    private String mLimit;

    SQLiteQuery(@NonNull Class<T> type) {
        mType = type;
    }

    //region Conditions
    @NonNull
    public SQLiteQuery<T> distinct() {
        mDistinct = true;
        return this;
    }

    @NonNull
    public SQLiteQuery<T> equalTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> notEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, NOT_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThan(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LT, value);
    }

    @NonNull
    public SQLiteQuery<T> lessThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThan(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, GT, value);
    }

    @NonNull
    public SQLiteQuery<T> greaterThanOrEqualTo(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, GT_OR_EQ, value);
    }

    @NonNull
    public SQLiteQuery<T> like(@NonNull String column, @NonNull Object value) {
        return appendWhere(column, LIKE, value);
    }

    @NonNull
    public SQLiteQuery<T> between(@NonNull String column, @NonNull Object lv, @NonNull Object rv) {
        return appendWhere(column, BETWEEN, lv, rv);
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
        return appendWhere(column, IS_NULL);
    }

    @NonNull
    public SQLiteQuery<T> notNull(@NonNull String column) {
        return appendWhere(column, NOT_NULL);
    }

    @NonNull
    public SQLiteQuery<T> appendWhere(@NonNull String where, @NonNull Object... bindArgs) {
        return appendWhere(where, StringValue.EMPTY, bindArgs);
    }

    @NonNull
    public SQLiteQuery<T> and() {
        mWhere.append(AND);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> or() {
        mWhere.append(OR);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> beginGroup() {
        mWhere.append(LEFT_PARENTHESIS);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> endGroup() {
        mWhere.append(RIGHT_PARENTHESIS);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> groupBy(@NonNull String... columns) {
        mGroupBy = TextUtils.join(COMMA, columns);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> having(@NonNull String having, @NonNull Object... bindArgs) {
        mHaving = having;
        Collections.addAll(mBindArgs, bindArgs);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> orderBy(@NonNull String column) {
        return orderBy(column, true);
    }

    @NonNull
    public SQLiteQuery<T> orderBy(@NonNull String column, boolean ascending) {
        if (ascending) {
            mOrderBy.add(column + ASC);
        } else {
            mOrderBy.add(column + DESC);
        }
        return this;
    }

    @NonNull
    public SQLiteQuery<T> limit(int limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    @NonNull
    public SQLiteQuery<T> offsetLimit(int offset, int limit) {
        mLimit = offset + COMMA + limit;
        return this;
    }

    @Nullable
    public T withId(long id) {
        return equalTo(BaseColumns._ID, id).one();
    }
    //endregion

    @Nullable
    public T one() {
        final List<T> list = limit(1).list();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @NonNull
    public List<T> list() {
        //return new SQLiteResult<>(this, mType, cursor());
        throw new UnsupportedOperationException();
    }

    @NonNull
    public Cursor cursor() {
        /*final Cursor cursor = SQLite.obtainClient().query(SQLiteQueryBuilder.buildQueryString(
                mDistinct,
                SQLite.tableOf(mType),
                null, mWhere.toString(),
                mGroupBy,
                mHaving,
                TextUtils.join(COMMA, mOrderBy),
                mLimit
        ), mBindArgs.toArray(new Object[mBindArgs.size()]));
        cursor.setNotificationUri(SQLite.obtainContext().getContentResolver(), SQLite.uriOf(mType));
        return cursor;*/
        throw new UnsupportedOperationException();
    }

    public int remove() {
        throw new UnsupportedOperationException();
        /*final StringBuilder sql = new StringBuilder("DELETE FROM ").append(SQLite.tableOf(mType));
        if (!TextUtils.isEmpty(mWhere)) {
            sql.append(WHERE).append(mWhere);
        }
        return SQLite.obtainClient().executeUpdateDelete(sql.toString(),
                mBindArgs.toArray(new Object[mBindArgs.size()]));*/
    }

    //region Functions
    @NonNull
    public Number min(@NonNull String column) {
        return applyFunc("MIN", column);
    }

    @NonNull
    public Number max(@NonNull String column) {
        return applyFunc("MAX", column);
    }

    @NonNull
    public Number sum(@NonNull String column) {
        return applyFunc("SUM", column);
    }

    @NonNull
    public Number count(@NonNull String column) {
        return applyFunc("COUNT", column);
    }
    //endregion

    @NonNull
    public Loader<List<T>> loader() {
        throw new UnsupportedOperationException();
        //return new SQLiteLoader<>(SQLite.obtainContext(), this);
    }

    public Observable<List<T>> observable(@NonNull LoaderManager lm, int loaderId) {
        throw new UnsupportedOperationException();
        //return Observable.create(new SQLiteOnSubscribe<>(lm, loader(), loaderId));
    }

    @Override
    public String toString() {
        return WHERE + mWhere.toString();
    }

    /*@NonNull
    Uri getUri() {
        return SQLite.uriOf(mType);
    }*/

    @NonNull
    private SQLiteQuery<T> appendWhere(@NonNull String column, @NonNull String op, @NonNull Object... values) {
        mWhere.append(column).append(op);
        Collections.addAll(mBindArgs, values);
        return this;
    }

    @NonNull
    private Number applyFunc(@NonNull String func, @NonNull String column) {
        final StringBuilder sql = new StringBuilder("SELECT ").append(func)
                .append(LEFT_PARENTHESIS).append(column).append(RIGHT_PARENTHESIS)
                .append(" FROM ")
                .append(SQLiteSchema.resolveTable(mType));
        if (!TextUtils.isEmpty(mWhere)) {
            sql.append(WHERE).append(mWhere);
        }
        throw new UnsupportedOperationException();
        /*return Double.parseDouble(SQLite.obtainClient().queryForString(sql.toString(),
                mBindArgs.toArray(new Object[mBindArgs.size()])));*/
    }

}
