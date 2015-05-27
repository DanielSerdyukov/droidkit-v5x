package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import droidkit.database.CursorUtils;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteUser {

    String mName;

    int mAge;

    double mWeight;

    byte[] mAvatar;

    boolean mEnabled;

    public SQLiteUser() {

    }

    public SQLiteUser(String name, int age, double weight, byte[] avatar) {
        mName = name;
        mAge = age;
        mWeight = weight;
        mAvatar = avatar;
    }

    public static SQLiteUser of(@NonNull SQLiteClient client, @NonNull Cursor cursor) {
        final SQLiteUser user = new SQLiteUser();
        user.mName = CursorUtils.getString(cursor, "name");
        user.mAge = CursorUtils.getInt(cursor, "age");
        user.mWeight = CursorUtils.getDouble(cursor, "weight");
        user.mAvatar = CursorUtils.getBlob(cursor, "avatar");
        user.mEnabled = CursorUtils.getBoolean(cursor, "enabled");
        return user;
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
}
