package droidkit.sqlite;

import android.support.annotation.NonNull;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
interface SQLiteStatement extends Closeable {

    void rebind(@NonNull Object... args);

    void execute();

    long executeInsert();

    int executeUpdateDelete();

    String simpleQueryForString();

    void bindNull(int index);

    void bindDouble(int index, double value);

    void bindLong(int index, long value);

    void bindBlob(int index, @NonNull byte[] value);

    void bindString(int index, @NonNull String value);

    @Override
    void close();

}
