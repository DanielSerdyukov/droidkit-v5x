package droidkit.sqlite;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("users")
public class SQLiteUser {

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

    public enum Role {
        ADMIN,
        USER
    }

}
