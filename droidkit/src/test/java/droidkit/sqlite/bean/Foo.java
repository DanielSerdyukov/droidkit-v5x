package droidkit.sqlite.bean;

import java.util.List;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import droidkit.annotation.SQLiteRelation;

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
    List<Bar> mBars;

}
