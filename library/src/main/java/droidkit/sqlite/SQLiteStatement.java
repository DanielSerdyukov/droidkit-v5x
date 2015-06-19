package droidkit.sqlite;

import android.support.annotation.NonNull;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
public interface SQLiteStatement extends Closeable {

    void rebind(@NonNull Object... args);

    void execute();

    long executeInsert();

    int executeUpdateDelete();

    String simpleQueryForString();

    void close();

}
