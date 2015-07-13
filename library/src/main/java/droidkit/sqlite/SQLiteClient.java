package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.io.IOUtils;
import droidkit.util.Dynamic;
import droidkit.util.Lists;
import droidkit.util.Objects;
import rx.functions.Func1;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteClient {

    private static final boolean JODA_TIME_SUPPORT = Dynamic.inClasspath("org.joda.time.DateTime");

    private static final List<ValueBinder> BINDERS = Arrays.asList(
            new NullBinder(),
            new DoubleBinder(),
            new LongBinder(),
            new BooleanBinder(),
            new BlobBinder(),
            new EnumBinder(),
            new DateTimeBinder(),
            new StringBinder()
    );

    private final ConcurrentMap<String, SQLiteStatement> mStmtCache = new ConcurrentHashMap<>();

    static void bindObjectToStatement(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
        for (final ValueBinder binder : BINDERS) {
            if (binder.canBind(value)) {
                binder.bind(stmt, index, value);
                return;
            }
        }
        throw new SQLiteException("Unsupported sqlite type: " + Objects.requireNonNull(value).getClass());
    }

    public void beginTransaction() {
        final SQLiteDatabase db = getWritableDatabase();
        if (!db.inTransaction()) {
            db.beginTransaction();
        }
    }

    public void endTransaction(boolean successful) {
        final SQLiteDatabase db = getWritableDatabase();
        if (db.inTransaction()) {
            db.endTransaction(successful);
            for (final SQLiteStatement stmt : mStmtCache.values()) {
                IOUtils.closeQuietly(stmt);
            }
            mStmtCache.clear();
        }
    }

    public void execute(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            stmt.execute();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public long executeInsert(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.executeInsert();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public int executeUpdateDelete(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getWritableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.executeUpdateDelete();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    @NonNull
    public Cursor query(@NonNull String sql, @NonNull Object... bindArgs) {
        return getReadableDatabase().rawQuery(sql, Lists.transform(Arrays.asList(bindArgs),
                new Func1<Object, String>() {
                    @NonNull
                    @Override
                    public String call(@NonNull Object o) {
                        return String.valueOf(o);
                    }
                }).toArray(new String[bindArgs.length]));
    }

    @NonNull
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public String queryForString(@NonNull String sql, @NonNull Object... bindArgs) {
        final SQLiteDatabase db = getReadableDatabase();
        final SQLiteStatement stmt = compileStatement(db, sql);
        try {
            stmt.rebind(bindArgs);
            return stmt.simpleQueryForString();
        } finally {
            if (!db.inTransaction()) {
                IOUtils.closeQuietly(stmt);
            }
        }
    }

    protected abstract SQLiteDatabase getReadableDatabase();

    protected abstract SQLiteDatabase getWritableDatabase();

    protected abstract void shutdown();

    @NonNull
    private SQLiteStatement compileStatement(@NonNull SQLiteDatabase db, @NonNull String sql) {
        if (db.inTransaction()) {
            SQLiteStatement stmt = mStmtCache.get(sql);
            if (stmt == null) {
                final SQLiteStatement newStmt = db.compileStatement(sql);
                stmt = mStmtCache.putIfAbsent(sql, newStmt);
                if (stmt == null) {
                    stmt = newStmt;
                } else {
                    IOUtils.closeQuietly(stmt);
                }
            }
            return stmt;
        }
        return db.compileStatement(sql);
    }

    interface Callbacks {

        void onDatabaseConfigure(@NonNull SQLiteDatabase db);

        void onDatabaseCreate(@NonNull SQLiteDatabase db);

        void onDatabaseUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion);

    }

    //region Binders
    private interface ValueBinder {

        boolean canBind(@Nullable Object value);

        void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value);

    }

    private static class NullBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value == null;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindNull(index);
        }

    }

    private static class LongBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Number;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
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
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindDouble(index, Objects.requireNonNull((Number) value).doubleValue());
        }

    }

    private static class BooleanBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Boolean;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
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
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindBlob(index, Objects.requireNonNull((byte[]) value));
        }

    }

    private static class StringBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof String;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((String) value));
        }

    }

    private static class EnumBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return value instanceof Enum;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindString(index, Objects.requireNonNull((Enum) value).name());
        }

    }

    private static class DateTimeBinder implements ValueBinder {

        @Override
        public boolean canBind(@Nullable Object value) {
            return JODA_TIME_SUPPORT && value instanceof DateTime;
        }

        @Override
        public void bind(@NonNull SQLiteStatement stmt, int index, @Nullable Object value) {
            stmt.bindLong(index, Objects.requireNonNull((DateTime) value).getMillis());
        }

    }
    //endregion

}
