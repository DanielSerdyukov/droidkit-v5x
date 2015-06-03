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
public class SignInActivity1Test {

    @Rule
    public ActivityTestRule<SignInActivity1> mActivityRule = new ActivityTestRule<>(SignInActivity1.class);

    @Test
    public void testViewInjected() throws Exception {
        final SignInActivity1 activity = mActivityRule.getActivity();
        Assert.assertNotNull(activity.mLogin);
        Assert.assertNotNull(activity.mPassword);
        Assert.assertNotNull(activity.getSignIn());
    }

}
