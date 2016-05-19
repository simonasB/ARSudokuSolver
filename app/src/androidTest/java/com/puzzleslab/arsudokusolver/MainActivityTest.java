package com.puzzleslab.arsudokusolver;

import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.puzzleslab.arsudokusolver.Activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void mainActivity_ClickRescanButtonAndCheckIfPopupAppeared() throws InterruptedException {
        onView(withId(R.id.button_rescan)).perform(click());
        Thread.sleep(2);
        onView(withId(R.id.textview_popupText)).inRoot(isPlatformPopup()).check(
                matches(withText(R.string.textview_popup)));
        onView(withId(R.id.button_dismiss)).inRoot(isPlatformPopup()).check(
                matches(withText(R.string.button_dismiss)));
        onView(withId(R.id.button_dismiss)).inRoot(isPlatformPopup()).perform(click());
    }
}