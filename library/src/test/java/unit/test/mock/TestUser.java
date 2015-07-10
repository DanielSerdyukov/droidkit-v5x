package unit.test.mock;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;

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

    @SQLiteColumn(value = "name")
    private String mName;

    @SQLiteColumn("weight")
    private double mWeight;

    @SQLiteColumn("role")
    private Role mRole = Role.USER;

    @SQLiteColumn("big_int")
    private BigInteger mBigInt;

    @SQLiteColumn("big_dec")
    private BigDecimal mBigDec;

    @SQLiteColumn("last_modified")
    private DateTime mLastModified;

    @SQLiteColumn
    private byte[] mBlob;

    public long getId() {
        return mId;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public TestUser setName(@NonNull String name) {
        mName = name;
        return this;
    }

    public double getWeight() {
        return mWeight;
    }

    @NonNull
    public TestUser setWeight(double weight) {
        mWeight = weight;
        return this;
    }

    public Role getRole() {
        return mRole;
    }

    @NonNull
    public TestUser setRole(@NonNull Role role) {
        mRole = role;
        return this;
    }

    public BigInteger getBigInt() {
        return mBigInt;
    }

    @NonNull
    public TestUser setBigInt(BigInteger bigInt) {
        mBigInt = bigInt;
        return this;
    }

    public BigDecimal getBigDec() {
        return mBigDec;
    }

    @NonNull
    public TestUser setBigDec(BigDecimal bigDec) {
        mBigDec = bigDec;
        return this;
    }

    @NonNull
    public DateTime getLastModified() {
        return mLastModified;
    }

    @NonNull
    public TestUser setLastModified(@NonNull DateTime lastModified) {
        mLastModified = lastModified;
        return this;
    }

    public enum Role {
        USER, ADMIN
    }

}
