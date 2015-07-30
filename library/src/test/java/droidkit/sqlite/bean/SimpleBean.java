package droidkit.sqlite.bean;

import droidkit.annotation.SQLiteColumn;
import droidkit.annotation.SQLiteObject;
import droidkit.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
@SQLiteObject(value = "simple_beans", activeRecord = false)
public class SimpleBean {

    @SQLitePk
    private long mId;

    @SQLiteColumn
    private String mText;

    public long getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

}
