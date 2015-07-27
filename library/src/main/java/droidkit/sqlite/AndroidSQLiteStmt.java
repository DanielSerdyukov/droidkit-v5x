package droidkit.sqlite;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

/**
 * @author Daniel Serdyukov
 */
class AndroidSQLiteStmt implements SQLiteStmt {

    private final SQLiteStatement mStatement;

    AndroidSQLiteStmt(@NonNull SQLiteStatement statement) {
        mStatement = statement;
    }

    @Override
    public void clearBindings() {
        mStatement.clearBindings();
    }

    @Override
    public void bindNull(int index) {
        mStatement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long value) {
        mStatement.bindLong(index, value);
    }

    @Override
    public void bindDouble(int index, double value) {
        mStatement.bindDouble(index, value);
    }

    @Override
    public void bindString(int index, String value) {
        mStatement.bindString(index, value);
    }

    @Override
    public void bindBlob(int index, byte[] value) {
        mStatement.bindBlob(index, value);
    }

    @Override
    public void execute() {
        mStatement.executeInsert();
    }

    @Override
    public long executeInsert() {
        return mStatement.executeInsert();
    }

    @Override
    public int executeUpdateDelete() {
        return mStatement.executeUpdateDelete();
    }

    @Override
    public void close() {
        mStatement.close();
    }

}
