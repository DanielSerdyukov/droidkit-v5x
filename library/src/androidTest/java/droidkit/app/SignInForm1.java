package droidkit.app;

import android.app.Fragment;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
public class SignInForm1 extends Fragment {

    final CountDownLatch mLoaderLatch = new CountDownLatch(1);

    @InjectView(droidkit.test.R.id.login)
    EditText mLogin;

    @InjectView(droidkit.test.R.id.password)
    EditText mPassword;

    boolean mSignInClicked;

    boolean mSettingsClicked;

    @InjectView(droidkit.test.R.id.sign_in)
    private Button mSignIn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(droidkit.test.R.layout.sign_in, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(droidkit.test.R.menu.test, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        Loaders.init(getLoaderManager(), 0, Bundle.EMPTY, this);
    }

    public Button getSignIn() {
        return mSignIn;
    }

    @OnClick(droidkit.test.R.id.sign_in)
    void onSignInClick() {
        mSignInClicked = true;
    }

    @OnActionClick(droidkit.test.R.id.action_settings)
    boolean onSettingsClick(MenuItem item) {
        mSettingsClicked = true;
        return true;
    }

    @OnCreateLoader(0)
    Loader<Object> onCreateLoader() {
        return new AsyncTaskLoader<Object>(getActivity().getApplicationContext()) {
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
