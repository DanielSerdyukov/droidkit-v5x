package droidkit.sqlite;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import droidkit.app.AppInfo;

/**
 * @author Daniel Serdyukov
 */
class SQLiteDbInfo {

    private static final String DATABASE_NAME = ".DATABASE_NAME";

    private static final String DATABASE_VERSION = ".DATABASE_VERSION";

    private static final String CASE_SENSITIVE_LIKE = "droidkit.sqlite.CASE_SENSITIVE_LIKE";

    private static final String MEMORY = ":memory:";

    private final List<String> mPragma = new ArrayList<>();

    private final String mName;

    private final int mVersion;

    public SQLiteDbInfo(@Nullable String name, int version) {
        mName = name;
        mVersion = version;
    }

    public static SQLiteDbInfo from(@NonNull Context context) {
        final Bundle metaData = AppInfo.getMetaData(context);
        final SQLiteDbInfo dbInfo = new SQLiteDbInfo(
                metaData.getString(context.getPackageName() + DATABASE_NAME, "data.db"),
                metaData.getInt(context.getPackageName() + DATABASE_VERSION, 1)
        );
        if (metaData.getBoolean(CASE_SENSITIVE_LIKE)) {
            dbInfo.mPragma.add("PRAGMA case_sensitive_like=ON;");
        }
        return dbInfo;
    }

    @NonNull
    List<String> getPragma() {
        return Collections.unmodifiableList(mPragma);
    }

    @Nullable
    String getName() {
        if (!TextUtils.equals(MEMORY, mName)) {
            return mName;
        }
        return null;
    }

    int getVersion() {
        return mVersion;
    }

}
