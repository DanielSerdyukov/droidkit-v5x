package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.io.IOUtils;
import droidkit.util.Arrays2;
import droidkit.util.Objects;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteClient implements Closeable {

    private static final boolean JODA_TIME_SUPPORT;

    private static final List<ValueBinder> BINDERS = Arrays.asList(
            new NullBinder(),
            new DoubleBinder(),
            new LongBinder(),
            new BooleanBinder(),
            new StringBinder(),
            new BlobBinder(),
            new EnumBinder(),
            new DateTimeBinder()
    );

    static {
        JODA_TIME_SUPPORT = DateTimeBinder.isSupported();
    }

    private final ConcurrentMap<String, SQLiteStmt> mStatements = new ConcurrentHashMap<>();

    public final void beginTransaction() {
        final SQLiteDb db = getWritableDatabase();
        if (!db.inTransaction()) {
            db.beginTransactionNonExclusive();
        }
    }

    public final void endTransaction() {
        closeStatements();
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    public final void rollbackTransaction() {
        closeStatements();
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            db.endTransaction();
        }
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public final Cursor query(@NonNull String sql, @Nullable Object... bindArgs) {
        return query(sql, Arrays2.transform(bindArgs, new Func1<Object, String>() {
            @Override
            public String call(Object arg) {
                return arg == null ? null : arg.toString();
            }
        }, String.class));
    }

    public final void execute(@NonNull String sql, @Nullable Object... bindArgs) {
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            bindStatementValues(stmt, bindArgs);
            stmt.execute();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            bindStatementValues(stmt, bindArgs);
            stmt.execute();
            IOUtils.closeQuietly(stmt);
        }
    }

    public final long executeInsert(@NonNull String sql, @Nullable Object... bindArgs) {
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            bindStatementValues(stmt, bindArgs);
            return stmt.executeInsert();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            bindStatementValues(stmt, bindArgs);
            final long rowId = stmt.executeInsert();
            IOUtils.closeQuietly(stmt);
            return rowId;
        }
    }

    public final int executeUpdateDelete(@NonNull String sql, @Nullable Object... bindArgs) {
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            bindStatementValues(stmt, bindArgs);
            return stmt.executeUpdateDelete();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            bindStatementValues(stmt, bindArgs);
            final int affectedRows = stmt.executeUpdateDelete();
            IOUtils.closeQuietly(stmt);
            return affectedRows;
        }
    }

    public void close() {
        closeStatements();
    }

    protected void onConfigure(@NonNull SQLiteDb db) {

    }

    protected void onCreate(@NonNull final SQLiteDb db) {
        SQLiteSchema.createTables(new Action2<String, String>() {
            @Override
            public void call(@NonNull String table, @NonNull String columns) {
                db.compileStatement("CREATE TABLE IF NOT EXISTS " + table + "(" + columns + ");").execute();
            }
        });
    }

    protected void onUpgrade(@NonNull final SQLiteDb db, int oldVersion, int newVersion) {
        SQLiteSchema.dropTables(new Action1<String>() {
            @Override
            public void call(@NonNull String table) {
                db.compileStatement("CREATE TABLE IF NOT EXISTS " + table + ";").execute();
            }
        });
        onCreate(db);
    }

    @NonNull
    protected abstract SQLiteDb getReadableDatabase();

    @NonNull
    protected abstract SQLiteDb getWritableDatabase();

    @NonNull
    @SuppressWarnings("ConstantConditions")
    Cursor query(@NonNull String sql, @Nullable String[] bindArgs) {
        return getReadableDatabase().query(sql, bindArgs);
    }

    @NonNull
    private SQLiteStmt compileStatement(@NonNull SQLiteDb db, @NonNull String sql) {
        SQLiteStmt stmt = mStatements.get(sql);
        if (stmt == null) {
            final SQLiteStmt newStmt = db.compileStatement(sql);
            stmt = mStatements.putIfAbsent(sql, newStmt);
            if (stmt == null) {
                stmt = newStmt;
            } else {
                IOUtils.closeQuietly(stmt);
            }
        }
        return stmt;
    }

    private void bindStatementValues(@NonNull SQLiteStmt stmt, Object... bindArgs) {
        for (int index = 0; index < bindArgs.length; ++index) {
            bindStatementValue(stmt, index + 1, bindArgs[index]);
        }
    }

    private void bindStatementValue(@NonNull SQLiteStmt stmt, int index, Object value) {
        for (final ValueBinder binder : BINDERS) {
            if (binder.canBind(value)) {
                binder.bind(stmt, index, value);
                return;
            }
        }
        throw new SQLiteException("Unsupported sqlite type: " + Objects.requireNonNull(value).getClass());
    }

    private void closeStatements() {
        for (final SQLiteStmt stmt : mStatements.values()) {
            IOUtils.closeQuietly(stmt);
        }
        mStatements.clear();
    }

    //region value binders
    private interface ValueBinder {

        boolean canBind(@Nullable Object value);

        void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value);

    }

    private static class NullBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value == null;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindNull(index);
        }

    }

    private static class LongBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Number;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindLong(index, Objects.requireNonNull((Number) value).longValue());
        }

    }

    private static class DoubleBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Double
                    || value instanceof Float
                    || value instanceof BigDecimal;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindDouble(index, Objects.requireNonNull((Number) value).doubleValue());
        }

    }

    private static class BooleanBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Boolean;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            final boolean bool = Objects.requireNonNull((Boolean) value);
            if (bool) {
                stmt.bindLong(index, 1);
            } else {
                stmt.bindLong(index, 0);
            }
        }

    }

    private static class BlobBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof byte[];
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindBlob(index, Objects.requireNonNull((byte[]) value));
        }

    }

    private static class StringBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof String;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((String) value));
        }

    }

    private static class EnumBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Enum;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((Enum) value).name());
        }

    }

    private static class DateTimeBinder implements ValueBinder {

        static boolean isSupported() {
            try {
                Class.forName("org.joda.time.DateTime");
                return true;
            } catch (ClassNotFoundException ignored) {
                return false;
            }
        }

        @Override
        public boolean canBind(@Nullable Object value) {
            return JODA_TIME_SUPPORT && value instanceof DateTime;
        }

        @Override
        public void bind(@NonNull SQLiteStmt stmt, int index, @Nullable Object value) {
            stmt.bindLong(index, Objects.requireNonNull((DateTime) value).getMillis());
        }

    }
    //endregion

}
