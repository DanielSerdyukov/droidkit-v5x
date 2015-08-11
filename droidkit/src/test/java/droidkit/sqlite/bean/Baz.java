package droidkit.sqlite.bean;

import java.util.List;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;
import droidkit.annotation.SQLiteRelation;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject("baz")
public class Baz {

    @SQLitePk
    long mId;

    @SQLiteColumn
    String mText;

    @SQLiteRelation
    List<Foo> mFoos;

}
