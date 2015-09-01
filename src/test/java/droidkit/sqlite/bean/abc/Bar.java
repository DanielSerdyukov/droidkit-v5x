package droidkit.sqlite.bean.abc;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import droidkit.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("bar")
public class Bar {

    @SQLitePk
    long mId;

    @SQLiteColumn
    String mText;

    @SQLiteRelation
    Baz mBaz;

    public long getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Baz getBaz() {
        return mBaz;
    }

    public void setBaz(Baz baz) {
        mBaz = baz;
    }

}
