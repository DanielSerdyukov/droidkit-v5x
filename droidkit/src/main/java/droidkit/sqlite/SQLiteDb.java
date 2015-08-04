package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteDb extends Closeable {

    @NonNull
    Cursor query(@NonNull String sql, @Nullable String... bindArgs);

    void beginTransactionNonExclusive();

    void setTransactionSuccessful();

    void endTransaction();

    boolean inTransaction();

    SQLiteStmt compileStatement(@NonNull String sql);

    @Override
    void close();

}
