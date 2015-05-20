package droidkit.sqlite;

/**
 * @author Daniel Serdyukov
 */
public class SQLite$Gen {

    static {
        SQLite.createTable("users",
                "_id INTEGER PRIMARY KEY",
                "name TEXT",
                "age INTEGER",
                "weight REAL",
                "avatar BLOB");
    }

}
