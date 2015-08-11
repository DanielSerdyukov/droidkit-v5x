package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
interface SQLiteRawQuery {

    @NonNull
    Cursor cursor();

}
