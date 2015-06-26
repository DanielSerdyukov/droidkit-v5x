package droidkit.app;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Serdyukov
 */
@RunWith(AndroidJUnit4.class)
public class SignInActivity2Test {

    @Rule
    public ActivityTestRule<SignInActivity2> mRule = new ActivityTestRule<>(SignInActivity2.class);

    private SignInActivity2 mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = mRule.getActivity();
    }

    @Test
    public void testViewInjected() throws Exception {
        final SignInForm1 signInForm = mActivity.getSignInForm();
        Assert.assertNotNull(signInForm);
        Assert.assertNotNull(signInForm.mLogin);
        Assert.assertNotNull(signInForm.mPassword);
        Assert.assertNotNull(signInForm.getSignIn());
    }

    @Test
    public void testOnClick() throws Exception {
        Espresso.onView(ViewMatchers
                .withId(droidkit.test.R.id.sign_in))
                .perform(ViewActions.click());
        Assert.assertTrue(mActivity.getSignInForm().mSignInClicked);
    }

    @Test
    public void testOnActionClick() throws Exception {
        Espresso.onView(ViewMatchers
                .withId(droidkit.test.R.id.action_settings))
                .perform(ViewActions.click());
        Assert.assertTrue(mActivity.getSignInForm().mSettingsClicked);
    }

    @Test
    public void testLoaderCallbacks() throws Exception {
        Assert.assertTrue(mActivity.getSignInForm().mLoaderLatch.await(5, TimeUnit.SECONDS));
    }

}
