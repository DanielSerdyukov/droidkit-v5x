package droidkit.sqlite.bean.abc;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteFk;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("baz")
public class Baz {

    @SQLitePk
    long mId;

    @SQLiteColumn
    String mText;

    @SQLiteFk(value = Bar.class, strict = false)
    long mBarId;

    public long getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public long getBarId() {
        return mBarId;
    }

    public void setBarId(long barId) {
        mBarId = barId;
    }

}
