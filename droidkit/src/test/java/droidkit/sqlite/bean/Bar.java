package droidkit.sqlite.bean;

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

}
