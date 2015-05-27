package droidkit.sqlite;

/**
 * @author Daniel Serdyukov
 */
public class SQLite$Gen {

    static {
        SQLite.onCreate("CREATE TABLE IF NOT EXISTS users(" +
                "_id INTEGER PRIMARY KEY," +
                " name TEXT," +
                " age INTEGER," +
                " weight REAL," +
                " avatar BLOB," +
                " enabled INTEGER);");

        SQLite.TABLES.put(SQLiteUser.class, "users");
    }

}
