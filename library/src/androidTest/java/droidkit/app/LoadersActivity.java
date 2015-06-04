package droidkit.app;

import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;
import droidkit.annotation.OnResetLoader;
import droidkit.sqlite.SQLiteLoader;
import droidkit.sqlite.SQLiteUser;

/**
 * @author Daniel Serdyukov
 */
public class LoadersActivity extends Activity {

    final CountDownLatch mLoader1Loaded = new CountDownLatch(1);

    final CountDownLatch mLoader2Loaded = new CountDownLatch(1);

    final CountDownLatch mLoader3Loaded = new CountDownLatch(1);

    boolean mLoader1Destroyed;

    boolean mLoader2Destroyed;

    boolean mLoader3Destroyed;

    @OnCreateLoader(1)
    Loader<List<SQLiteUser>> onCreateLoader1() {
        return new SQLiteLoader<>(getApplicationContext(), SQLiteUser.class);
    }

    @OnCreateLoader({2, 3})
    Loader<List<SQLiteUser>> onCreateLoader2(Bundle args) {
        return new SQLiteLoader<>(getApplicationContext(), SQLiteUser.class);
    }

    @OnLoadFinished(1)
    void onLoadFinished1() {
        mLoader1Loaded.countDown();
    }

    @OnLoadFinished(2)
    void onLoadFinished2(List<SQLiteUser> data) {
        mLoader2Loaded.countDown();
    }

    @OnLoadFinished(3)
    void onLoadFinished3(Loader<List<SQLiteUser>> loader, List<SQLiteUser> data) {
        mLoader3Loaded.countDown();
    }

    @OnResetLoader(1)
    void onResetLoader1() {
        mLoader1Destroyed = true;
    }

    @OnResetLoader({2, 3})
    void onResetLoader2(Loader<List<SQLiteUser>> loader) {
        if (2 == loader.getId()) {
            mLoader2Destroyed = true;
        } else if (3 == loader.getId()) {
            mLoader3Destroyed = true;
        }
    }

}
