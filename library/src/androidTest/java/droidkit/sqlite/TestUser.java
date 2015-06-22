package droidkit.sqlite;

import android.support.annotation.NonNull;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("users")
public class TestUser {

    @SQLitePk
    private long mId;

    @SQLiteColumn("name")
    private String mName;

    @SQLiteColumn("role")
    private Role mRole = Role.USER;

    public long getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public TestUser setName(String name) {
        mName = name;
        return this;
    }

    @NonNull
    public Role getRole() {
        return mRole;
    }

    @NonNull
    public TestUser setRole(Role role) {
        mRole = role;
        return this;
    }

    public enum Role {
        USER, ADMIN
    }

}
