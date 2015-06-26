package droidkit.app;

import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;

import droidkit.annotation.OnCreateLoader;

/**
 * @author Daniel Serdyukov
 */
public class SignInActivity2 extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(droidkit.test.R.layout.single_fragment);
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(droidkit.test.R.id.fragment1, new SignInForm1())
                    .commit();
        }
    }

    SignInForm1 getSignInForm() {
        return (SignInForm1) getFragmentManager().findFragmentById(droidkit.test.R.id.fragment1);
    }

}
