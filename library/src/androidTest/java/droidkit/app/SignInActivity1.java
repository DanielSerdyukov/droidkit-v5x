package droidkit.app;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.CountDownLatch;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;
import droidkit.annotation.OnCreateLoader;
import droidkit.annotation.OnLoadFinished;

/**
 * @author Daniel Serdyukov
 */
public class SignInActivity1 extends Activity {

    final CountDownLatch mLoaderLatch = new CountDownLatch(1);

    @InjectView(droidkit.test.R.id.login)
    EditText mLogin;

    @InjectView(droidkit.test.R.id.password)
    EditText mPassword;

    boolean mSignInClicked;

    boolean mSettingsClicked;

    @InjectView(droidkit.test.R.id.sign_in)
    private Button mSignIn;

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(droidkit.test.R.layout.sign_in);
        //Loaders.init(getLoaderManager(), 0, Bundle.EMPTY, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        };
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(droidkit.test.R.menu.test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public Button getSignIn() {
        return mSignIn;
    }

    @OnClick(droidkit.test.R.id.sign_in)
    void onSignInClick() {
        mSignInClicked = true;
    }

    @OnActionClick(droidkit.test.R.id.action_settings)
    void onSettingsClick() {
        mSettingsClicked = true;
    }

    @OnCreateLoader(0)
    Loader<Object> onCreateLoader() {
        return new AsyncTaskLoader<Object>(getApplicationContext()) {
            @Override
            public Object loadInBackground() {
                return new Object();
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        };
    }

    @OnLoadFinished(0)
    void onLoadFinished(Object object) {
        mLoaderLatch.countDown();
    }

}
