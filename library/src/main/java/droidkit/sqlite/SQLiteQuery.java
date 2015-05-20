package droidkit.sqlite;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteQuery<T> {

    @Nullable
    public T one() {
        return null;
    }

    @NonNull
    public List<T> list() {
        return Collections.emptyList();
    }

    public int remove() {
        return 0;
    }

}
