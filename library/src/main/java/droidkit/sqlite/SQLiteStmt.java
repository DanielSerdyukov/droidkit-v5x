package droidkit.sqlite;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteStmt extends Closeable {

    void clearBindings();

    void bindNull(int index);

    void bindLong(int index, long value);

    void bindDouble(int index, double value);

    void bindString(int index, String value);

    void bindBlob(int index, byte[] value);

    void execute();

    long executeInsert();

    int executeUpdateDelete();

    @Override
    void close();

}
