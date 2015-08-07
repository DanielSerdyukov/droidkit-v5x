package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private static final String TAG = "SQLiteClient";

    private static final AtomicBoolean DEBUG = new AtomicBoolean();

    private static final String BINDING = " << ";

    private static final Action2<String, Object[]> LOG = new Action2<String, Object[]>() {
        @Override
        public void call(String sql, Object[] args) {
            if (DEBUG.get()) {
                if (args == null) {
                    Log.d(TAG, sql);
                } else {
                    Log.d(TAG, sql + BINDING + Arrays.toString(args));
                }
            }
        }
    };

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

    public static void setLogEnabled(boolean enabled) {
        DEBUG.compareAndSet(DEBUG.get(), enabled);
    }

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
        LOG.call(sql, bindArgs);
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            clearAndBindValues(stmt, bindArgs);
            stmt.execute();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            clearAndBindValues(stmt, bindArgs);
            stmt.execute();
            IOUtils.closeQuietly(stmt);
        }
    }

    public final long executeInsert(@NonNull String sql, @Nullable Object... bindArgs) {
        LOG.call(sql, bindArgs);
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            clearAndBindValues(stmt, bindArgs);
            return stmt.executeInsert();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            clearAndBindValues(stmt, bindArgs);
            try {
                return stmt.executeInsert();
            } finally {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    public final int executeUpdateDelete(@NonNull String sql, @Nullable Object... bindArgs) {
        LOG.call(sql, bindArgs);
        final SQLiteDb db = getWritableDatabase();
        if (db.inTransaction()) {
            final SQLiteStmt stmt = compileStatement(db, sql);
            clearAndBindValues(stmt, bindArgs);
            return stmt.executeUpdateDelete();
        } else {
            final SQLiteStmt stmt = db.compileStatement(sql);
            clearAndBindValues(stmt, bindArgs);
            try {
                return stmt.executeUpdateDelete();
            } finally {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @NonNull
    public final String queryForString(@NonNull String sql, @Nullable Object... bindArgs) {
        LOG.call(sql, bindArgs);
        final SQLiteDb db = getReadableDatabase();
        final SQLiteStmt stmt = db.compileStatement(sql);
        clearAndBindValues(stmt, bindArgs);
        try {
            return stmt.queryForString();
        } finally {
            IOUtils.closeQuietly(stmt);
        }
    }

    @Override
    public void close() {
        closeStatements();
    }

    protected void onConfigure(@NonNull SQLiteDb db) {

    }

    protected void onCreate(@NonNull final SQLiteDb db) {
        SQLiteSchema.createTables(new Action2<String, String>() {
            @Override
            public void call(@NonNull String table, @NonNull String columns) {
                final String query = "CREATE TABLE IF NOT EXISTS " + table + "(" + columns + ");";
                LOG.call(query, null);
                db.compileStatement(query).execute();
            }
        });
        SQLiteSchema.createIndices(new Action2<String, String>() {
            @Override
            public void call(String table, String column) {
                final String query = "CREATE INDEX IF NOT EXISTS idx_" + table + "_" + column +
                        " ON " + table + "(" + column + ");";
                LOG.call(query, null);
                db.compileStatement(query).execute();
            }
        });
    }

    protected void onUpgrade(@NonNull final SQLiteDb db, int oldVersion, int newVersion) {
        SQLiteSchema.dropTables(new Action1<String>() {
            @Override
            public void call(@NonNull String table) {
                final String query = "DROP TABLE IF EXISTS " + table + ";";
                LOG.call(query, null);
                db.compileStatement(query).execute();
            }
        }, new Func1<String, Boolean>() {
            @Override
            public Boolean call(String table) {
                return true;
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
        LOG.call(sql, bindArgs);
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

    private void clearAndBindValues(@NonNull SQLiteStmt stmt, Object... bindArgs) {
        stmt.clearBindings();
        for (int index = 0; index < bindArgs.length; ++index) {
            bindValue(stmt, index + 1, bindArgs[index]);
        }
    }

    private void bindValue(@NonNull SQLiteStmt stmt, int index, Object value) {
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
            } catch (ClassNotFoundException e) {
                Log.e("DateTimeBinder", e.getMessage(), e);
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
