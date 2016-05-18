package com.puzzleslab.arsudokusolver;

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
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void mainActivity_ClickRescanButtonAndCheckIfPopupAppeared() {
        onView(withText(R.string.button_scan)).perform(click());
        //onView(withId(R.layout.popup)).check(matches(withText("Could not detect or solve detected sudoku. Please press the Rescan button again.")));
        onView(withText(R.id.dismiss)).perform(click());
    }
}