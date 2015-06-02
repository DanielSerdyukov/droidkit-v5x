package droidkit.sqlite;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.ref.WeakReference;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import droidkit.database.DatabaseUtils;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("users")
public class SQLiteUser {

    public static final String _TABLE_ = "users";

    @SQLitePk
    long mId;

    @SQLiteColumn("name")
    String mName;

    @SQLiteColumn("age")
    int mAge;

    @SQLiteColumn("weight")
    double mWeight;

    @SQLiteColumn("avatar")
    byte[] mAvatar;

    @SQLiteColumn("enabled")
    boolean mEnabled;

    @SQLiteColumn("role")
    Role mRole = Role.USER;

    private WeakReference<SQLiteClient> mClientRef;

    public static SQLiteUser forTest(String name, int age, double weight, byte[] avatar) {
        return forTest(name, age, weight, avatar, Role.USER);
    }

    public static SQLiteUser forTest(String name, int age, double weight, byte[] avatar, Role role) {
        final SQLiteUser user = new SQLiteUser();
        user.mName = name;
        user.mAge = age;
        user.mWeight = weight;
        user.mAvatar = avatar;
        user.mRole = role;
        return user;
    }

    public static SQLiteUser create(SQLiteClient client) {
        final SQLiteUser user = new SQLiteUser();
        user.mId = client.insertRowId(_TABLE_);
        user.mClientRef = new WeakReference<>(client);
        SQLiteCache.of(SQLiteUser.class).put(user.mId, user);
        return user;
    }

    public static SQLiteUser create(SQLiteClient client, Cursor cursor) {
        final SQLiteUser user = new SQLiteUser();
        user.mClientRef = new WeakReference<>(client);
        user.mId = DatabaseUtils.getLong(cursor, "_id");
        user.mName = DatabaseUtils.getString(cursor, "name");
        user.mAge = DatabaseUtils.getInt(cursor, "age");
        user.mWeight = DatabaseUtils.getDouble(cursor, "weight");
        user.mAvatar = DatabaseUtils.getBlob(cursor, "avatar");
        user.mEnabled = DatabaseUtils.getBoolean(cursor, "enabled");
        user.mRole = DatabaseUtils.getEnum(cursor, "role", Role.class);
        SQLiteCache.of(SQLiteUser.class).put(user.mId, user);
        return user;
    }

    public static void saveToSQLite(SQLiteClient client, SQLiteUser object) {
        final ContentValues values = new ContentValues();
        if (object.mId > 0) {
            DatabaseUtils.putValue(values, "_id", object.mId);
        }
        DatabaseUtils.putValue(values, "name", object.mName);
        DatabaseUtils.putValue(values, "age", object.mAge);
        DatabaseUtils.putValue(values, "weight", object.mWeight);
        DatabaseUtils.putValue(values, "avatar", object.mAvatar);
        DatabaseUtils.putValue(values, "enabled", object.mEnabled);
        DatabaseUtils.putValue(values, "role", object.mRole.name());
        object.mId = client.insert(_TABLE_, values);
        SQLiteCache.of(SQLiteUser.class).put(object.mId, object);
    }

    public String getName() {
        return mName;
    }

    public int getAge() {
        return mAge;
    }

    public double getWeight() {
        return mWeight;
    }

    public byte[] getAvatar() {
        return mAvatar;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public void setWeight(double weight) {
        mWeight = weight;
    }

    public void setAvatar(byte[] avatar) {
        mAvatar = avatar;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setRole(Role role) {
        mRole = role;
    }

    /*private void updateIfActiveObject(String column, Object value) {
        if (mClientRef != null) {
            final SQLiteClient client = mClientRef.get();
            if (client != null) {
                client.executeUpdateDelete("UPDATE " + _TABLE_ + " SET " + column + " = ? WHERE _id = ?;", value, mId);
            }
        }
    }*/

    public enum Role {
        ADMIN,
        USER
    }

}
