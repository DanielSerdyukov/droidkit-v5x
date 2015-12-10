package droidkit.sqlite;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import droidkit.dynamic.DynamicException;
import droidkit.dynamic.MethodLookup;

/**
 * @author Daniel Serdyukov
 */
public class SQLiteList<T> extends AbstractList<T> {

    private final List<T> mList;

    private SQLiteList(@NonNull List<T> unpacked) {
        mList = unpacked;
    }

    static <T> SQLiteList<T> unpack(@NonNull Cursor cursor, @NonNull Class<T> type) {
        final List<T> list = new ArrayList<>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                list.add(unpackOne(cursor, type));
            } while (cursor.moveToNext());
        }
        return new SQLiteList<>(list);
    }

    static <T> T unpackOne(@NonNull Cursor cursor, @NonNull Class<T> type) {
        try {
            return MethodLookup.global()
                    .find(type.getName() + "$SQLiteHelper", "instantiate", Cursor.class)
                    .invokeStatic(cursor);
        } catch (DynamicException e) {
            throw new SQLiteException("Can't instantiate object", e);
        }
    }

    @Override
    public T get(int location) {
        return mList.get(location);
    }

    @Override
    public void add(int location, T object) {
        SQLite.save(object);
        mList.add(location, object);
    }

    @Override
    public T remove(int location) {
        final T object = mList.remove(location);
        if (object != null) {
            SQLite.remove(object);
        }
        return object;
    }

    @Override
    public int size() {
        return mList.size();
    }

}
