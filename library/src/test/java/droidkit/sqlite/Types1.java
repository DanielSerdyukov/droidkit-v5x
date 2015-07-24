package droidkit.sqlite;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("types1")
class Types1 {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private long mLongType;

    @SQLiteColumn
    private int mIntType;

    @SQLiteColumn
    private short mShortType;

    @SQLiteColumn
    private String mStringType;

    @SQLiteColumn
    private boolean mBooleanType;

    @SQLiteColumn
    private double mDoubleType;

    @SQLiteColumn
    private float mFloatType;

    @SQLiteColumn
    private BigDecimal mBigDecimal;

    @SQLiteColumn
    private BigInteger mBigInteger;

    @SQLiteColumn
    private DateTime mDateTime;

    @SQLiteColumn
    private byte[] mByteArray;

    @SQLiteColumn
    private Role mRole;

    public long getId() {
        return mId;
    }

    public long getLongType() {
        return mLongType;
    }

    public int getIntType() {
        return mIntType;
    }

    public short getShortType() {
        return mShortType;
    }

    public String getStringType() {
        return mStringType;
    }

    public boolean isBooleanType() {
        return mBooleanType;
    }

    public double getDoubleType() {
        return mDoubleType;
    }

    public float getFloatType() {
        return mFloatType;
    }

    public BigDecimal getBigDecimal() {
        return mBigDecimal;
    }

    public BigInteger getBigInteger() {
        return mBigInteger;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public byte[] getByteArray() {
        return mByteArray;
    }

    public Role getRole() {
        return mRole;
    }

    public enum Role {USER, ADMIN}

}
