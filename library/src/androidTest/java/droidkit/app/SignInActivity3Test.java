package droidkit.app;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SignInActivity3Test {

    @Rule
    public ActivityTestRule<SignInActivity3> mActivityRule = new ActivityTestRule<>(SignInActivity3.class);

    @Test
    public void testViewInjected() throws Exception {
        final SignInActivity3 activity = mActivityRule.getActivity();
        final SignInForm2 signInForm = activity.getSignInForm();
        Assert.assertNotNull(signInForm);
        Assert.assertNotNull(signInForm.mLogin);
        Assert.assertNotNull(signInForm.mPassword);
        Assert.assertNotNull(signInForm.getSignIn());
    }

}
