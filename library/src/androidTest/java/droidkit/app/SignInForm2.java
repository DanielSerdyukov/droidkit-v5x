package droidkit.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnClick;

/**
 * @author Daniel Serdyukov
 */
public class SignInForm2 extends Fragment {

    @InjectView(droidkit.test.R.id.login)
    EditText mLogin;

    @InjectView(droidkit.test.R.id.password)
    EditText mPassword;

    boolean mSignInClicked;

    @InjectView(droidkit.test.R.id.sign_in)
    private Button mSignIn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(droidkit.test.R.layout.sign_in, container, false);
    }

    public Button getSignIn() {
        return mSignIn;
    }

    @OnClick(droidkit.test.R.id.sign_in)
    void onSignInClick(View view) {
        mSignInClicked = (mSignIn == view);
    }

}
