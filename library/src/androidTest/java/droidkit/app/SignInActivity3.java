package droidkit.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * @author Daniel Serdyukov
 */
public class SignInActivity3 extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(droidkit.test.R.layout.single_fragment);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(droidkit.test.R.id.fragment1, new SignInForm2())
                    .commit();
        }
    }

    SignInForm2 getSignInForm() {
        return (SignInForm2) getSupportFragmentManager().findFragmentById(droidkit.test.R.id.fragment1);
    }

}
