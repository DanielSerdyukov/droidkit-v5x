package unit.test.mock;

import android.support.annotation.NonNull;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("users")
public class SQLiteUser {

    @SQLitePk
    private long mId;

    @SQLiteColumn("name")
    private String mName;

    @SQLiteColumn("lat")
    private double mLat;

    @SQLiteColumn("lon")
    private double mLon;

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
    public SQLiteUser setName(@NonNull String name) {
        mName = name;
        return this;
    }

    public double getLat() {
        return mLat;
    }

    @NonNull
    public SQLiteUser setLat(double lat) {
        mLat = lat;
        return this;
    }

    public double getLon() {
        return mLon;
    }

    @NonNull
    public SQLiteUser setLon(double lon) {
        mLon = lon;
        return this;
    }

    public Role getRole() {
        return mRole;
    }

    @NonNull
    public SQLiteUser setRole(@NonNull Role role) {
        mRole = role;
        return this;
    }

    public enum Role {
        USER, ADMIN
    }

}
