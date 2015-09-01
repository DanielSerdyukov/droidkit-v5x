package droidkit.sqlite.bean.xyz;

import java.util.ArrayList;
import java.util.List;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import droidkit.annotation.SQLiteRelation;
import droidkit.sqlite.bean.abc.Bar;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("foo")
public class Foo {

    @SQLitePk
    long mId;

    @SQLiteColumn
    String mText;

    @SQLiteRelation
    List<Bar> mBars = new ArrayList<>();

    public long getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public List<Bar> getBars() {
        return mBars;
    }

}
