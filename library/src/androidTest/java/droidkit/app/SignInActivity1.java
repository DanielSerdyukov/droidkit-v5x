package droidkit.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

import droidkit.annotation.InjectView;
import droidkit.annotation.OnActionClick;
import droidkit.annotation.OnClick;

/**
 * @author Daniel Serdyukov
 */
public class SignInActivity1 extends Activity {

    @InjectView(droidkit.test.R.id.login)
    EditText mLogin;

    @InjectView(droidkit.test.R.id.password)
    EditText mPassword;

    boolean mSignInClicked;

    boolean mSettingsClicked;

    @InjectView(droidkit.test.R.id.sign_in)
    private Button mSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(droidkit.test.R.layout.sign_in);
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

}
