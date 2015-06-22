package droidkit.sqlite;

import android.support.annotation.Nullable;

/**
 * @author Daniel Serdyukov
 */
public class TestSQLiteProvider extends SQLiteProvider {

    @Nullable
    @Override
    protected String getDatabaseName() {
        return null;
    }
    
}
