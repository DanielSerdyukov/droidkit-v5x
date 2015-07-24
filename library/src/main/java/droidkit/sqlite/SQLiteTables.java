package droidkit.sqlite;

import android.support.annotation.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.FieldLookup;

/**
 * @author Daniel Serdyukov
 */
public abstract class SQLiteTables {

    static final String SQLITE = "$SQLite";

    private static final ConcurrentMap<Class<?>, String> TABLES = new ConcurrentHashMap<>();

    private static final String TABLE = "TABLE";

    private SQLiteTables() {
    }

    @NonNull
    public static String resolve(@NonNull Class<?> type) {
        try {
            String table = TABLES.get(type);
            if (table == null) {
                final String newTable = FieldLookup.global()
                        .find(type.getName() + SQLITE, TABLE)
                        .getStatic();
                table = TABLES.putIfAbsent(type, newTable);
                if (table == null) {
                    table = newTable;
                }
            }
            return table;
        } catch (DynamicException e) {
            throw new SQLiteException("No such table for " + type.getName(), e);
        }
    }

}
