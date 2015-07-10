package unit.test.mock;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("accounts")
public class Account {

    @SQLitePk(3)
    private long mId;

    @SQLiteColumn
    private String mComment;

    @SQLiteColumn
    private double mBalance;

    public long getId() {
        return mId;
    }

    public String getComment() {
        return mComment;
    }

    public Account setComment(String comment) {
        mComment = comment;
        return this;
    }

    public double getBalance() {
        return mBalance;
    }

    public Account setBalance(double balance) {
        mBalance = balance;
        return this;
    }

}
