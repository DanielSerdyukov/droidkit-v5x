package droidkit.sqlite;

import android.support.annotation.Nullable;

import org.sqlite.database.SQLiteNative;

/**
 * @author Daniel Serdyukov
 */
public class TestSQLiteProvider extends SQLiteProvider {

    static {
        SQLiteNative.loadLibs();
    }

    @Nullable
    @Override
    protected String getDatabaseName() {
        return null;
    }

}
