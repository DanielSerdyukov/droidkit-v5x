package droidkit.sqlite.bean;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.math.BigInteger;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("beans")
public class SQLiteBean {

    public static final String TABLE = "beans";

    public static final String[] COLUMNS = new String[]{
            "_id",
            "long",
            "int",
            "short",
            "string",
            "boolean",
            "double",
            "float",
            "big_decimal",
            "big_integer",
            "bytes",
            "role",
            "date"
    };

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private long mLong;

    @SQLiteColumn
    private int mInt;

    @SQLiteColumn
    private short mShort;

    @SQLiteColumn
    private String mString;

    @SQLiteColumn
    private boolean mBoolean;

    @SQLiteColumn
    private double mDouble;

    @SQLiteColumn
    private float mFloat;

    @SQLiteColumn
    private BigDecimal mBigDecimal;

    @SQLiteColumn
    private BigInteger mBigInteger;

    @SQLiteColumn("bytes")
    private byte[] mByteArray;

    @SQLiteColumn
    private Role mRole;

    @SQLiteColumn("date")
    private DateTime mDateTime;

    public long getId() {
        return mId;
    }

    public long getLong() {
        return mLong;
    }

    public int getInt() {
        return mInt;
    }

    public short getShort() {
        return mShort;
    }

    public String getString() {
        return mString;
    }

    public boolean isBoolean() {
        return mBoolean;
    }

    public double getDouble() {
        return mDouble;
    }

    public float getFloat() {
        return mFloat;
    }

    public BigDecimal getBigDecimal() {
        return mBigDecimal;
    }

    public BigInteger getBigInteger() {
        return mBigInteger;
    }

    public byte[] getByteArray() {
        return mByteArray;
    }

    public Role getRole() {
        return mRole;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public enum Role {USER, ADMIN}

}
