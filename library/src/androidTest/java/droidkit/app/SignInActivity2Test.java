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
public class SignInActivity2Test {

    @Rule
    public ActivityTestRule<SignInActivity2> mActivityRule = new ActivityTestRule<>(SignInActivity2.class);

    @Test
    public void testViewInjected() throws Exception {
        final SignInActivity2 activity = mActivityRule.getActivity();
        final SignInForm1 signInForm = activity.getSignInForm();
        Assert.assertNotNull(signInForm);
        Assert.assertNotNull(signInForm.mLogin);
        Assert.assertNotNull(signInForm.mPassword);
        Assert.assertNotNull(signInForm.getSignIn());
    }

}
