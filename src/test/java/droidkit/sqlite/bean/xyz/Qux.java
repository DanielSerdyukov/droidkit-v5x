package droidkit.sqlite.bean.xyz;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteFk;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("qux")
public class Qux {

    @SQLitePk
    long mId;

    @SQLiteColumn
    String mText;

    @SQLiteFk(Foo.class)
    long mFooId;

    public long getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public long getFooId() {
        return mFooId;
    }

    public void setFooId(long fooId) {
        mFooId = fooId;
    }

}
