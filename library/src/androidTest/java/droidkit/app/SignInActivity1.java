package droidkit.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import droidkit.annotation.InjectView;

/**
 * @author Daniel Serdyukov
 */
public class SignInActivity1 extends Activity {

    @InjectView(droidkit.test.R.id.login)
    EditText mLogin;

    @InjectView(droidkit.test.R.id.password)
    EditText mPassword;

    @InjectView(droidkit.test.R.id.sign_in)
    private Button mSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(droidkit.test.R.layout.sign_in);
    }

    public Button getSignIn() {
        return mSignIn;
    }

}
