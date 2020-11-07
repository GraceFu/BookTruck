package com.example.booktruck;

import android.widget.EditText;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

@LargeTest
public class SignUpSuccess {

    private Solo solo;

    @Before
    public void setUp() {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    @Rule
    public ActivityTestRule<SignUpActivity> rule = new ActivityTestRule<>(SignUpActivity.class, true, true);

    public String generateString(){
        String str = "";
        Random rnd = new Random();
        for (int i=0; i<10; i++){
            char randomChar = (char) ('a' + rnd.nextInt(26));
            str += randomChar;
        }
        return str;
    }

    @Test
    public void testSignupWithRightPassword(){
        solo.enterText((EditText) solo.getView(R.id.signup_email), generateString());
        solo.enterText((EditText) solo.getView(R.id.signup_contact), generateString());
        solo.enterText((EditText) solo.getView(R.id.signup_password), generateString());
        solo.clickOnButton("Sign Up");
        solo.waitForActivity(MainActivity.class);
    }
}
