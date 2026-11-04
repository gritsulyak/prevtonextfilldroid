// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.PointF;
import android.view.View;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gritsulyak.prevtonextfill.game.XY;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * True end-to-end tests: they launch the real activity on an Android device or
 * emulator, tap the custom-drawn board/toolbar through Espresso, and verify the
 * resulting game state and dialogs.
 */
@RunWith(AndroidJUnit4.class)
public final class MainActivityE2ETest {

    private ActivityScenario<MainActivity> scenario;

    @Before
    public void launchApp() {
        scenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void closeApp() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @Test
    public void appStartsOnFirstLevel() {
        onView(withId(R.id.toolbar_view)).check(matches(isDisplayed()));
        onView(withId(R.id.game_view)).check(matches(isDisplayed()));
        assertState(0, "OPAPA", 1, 3, 3);
    }

    @Test
    public void adjacentPlainCellCanBeFilled() {
        clickCell(3, 2);
        assertState(0, "OPAPA", 2, 3, 2);
    }

    @Test
    public void nonAdjacentTargetIsRejected() {
        // The number 1 is at (0,0), far away from the starting number 0.
        clickCell(0, 0);
        assertState(0, "OPAPA", 1, 3, 3);
    }

    @Test
    public void newGameConfirmationRestartsLevel() {
        clickCell(2, 3);
        assertState(0, "OPAPA", 2, 2, 3);

        clickToolbar(ToolbarView.ACTION_NEW_GAME);
        onView(withText(R.string.new_game_message)).check(matches(isDisplayed()));
        clickDialogPositive();

        assertState(0, "OPAPA", 1, 3, 3);
    }

    @Test
    public void cheatCodeStartsTheRequestedLevel() {
        clickToolbar(ToolbarView.ACTION_NEW_LEVEL);
        onView(withText(R.string.code_title)).check(matches(isDisplayed()));

        onView(withClassName(is(EditText.class.getName()))).perform(replaceText("FLOWERS"));
        closeSoftKeyboard();
        clickDialogPositive();

        assertState(28, "FLOWERS", 1, 6, 1);
    }

    @Test
    public void unknownCheatCodeShowsCurrentCodeAndRestarts() {
        clickCell(3, 2);
        assertState(0, "OPAPA", 2, 3, 2);

        clickToolbar(ToolbarView.ACTION_NEW_LEVEL);
        onView(withClassName(is(EditText.class.getName()))).perform(replaceText("NOPE"));
        closeSoftKeyboard();
        clickDialogPositive();

        // The original ReStartRound() behaviour: show the current code...
        onView(withText(R.string.cheat_code_title)).check(matches(isDisplayed()));
        onView(withText("OPAPA")).check(matches(isDisplayed()));
        clickDialogPositive();

        // ... and restart that level.
        assertState(0, "OPAPA", 1, 3, 3);
    }

    @Test
    public void aboutDialogIsShown() {
        clickToolbar(ToolbarView.ACTION_ABOUT);
        onView(withText(R.string.about_title)).check(matches(isDisplayed()));
        onView(withText(containsString("Android port: 2026"))).check(matches(isDisplayed()));
        clickDialogPositive();
    }

    @Test
    public void exitConfirmationCanBeCancelled() {
        clickToolbar(ToolbarView.ACTION_EXIT);
        onView(withText(R.string.exit_message)).check(matches(isDisplayed()));
        clickDialogNegative();
        assertState(0, "OPAPA", 1, 3, 3);
    }

    /**
     * Plays the complete OPAPA level through the UI, including the original
     * jump-back-to-a-reached-target moves, then checks the completion dialog
     * and the transition to COOL.
     */
    @Test
    public void fullLevelCanBeCompletedAndAdvancesToNextLevel() {
        final int[][] moves = {
                {2, 3, 2},
                {1, 3, 3},
                {0, 3, 4},
                {0, 2, 5},
                {0, 1, 6},
                {0, 0, 7},   // target 1
                {1, 0, 8},
                {1, 1, 9},
                {1, 2, 10},  // target 2
                {2, 2, 11},  // target 3
                {2, 1, 12},
                {2, 0, 13},  // target 4
                {3, 0, 14},
                {3, 1, 15},  // target 5
                {0, 0, 15},  // jump back to target 1
                {1, 2, 15},  // jump back to target 2
                {2, 0, 15},  // jump back to target 4
                {2, 2, 15},  // jump back to target 3
                {3, 3, 15},  // jump back to target 0
                {3, 2, 16}   // last empty cell
        };

        for (int[] move : moves) {
            clickCell(move[0], move[1]);
            assertPassedCount(move[2]);
        }

        onView(withText(R.string.cheat_code_title)).check(matches(isDisplayed()));
        onView(withText("COOL")).check(matches(isDisplayed()));
        clickDialogPositive();

        assertState(1, "COOL", 1, 0, 3);
    }

    private void assertState(int round, String code, int passed, int x, int y) {
        scenario.onActivity(activity -> {
            GameView view = activity.findViewById(R.id.game_view);
            assertNotNull(view);
            assertEquals(round, view.getCurrentRound());
            assertEquals(code, view.getCurrentCode());
            assertEquals(passed, view.getPassedCount());
            assertEquals(new XY(x, y), view.getCurrentPosition());
        });
    }

    private void assertPassedCount(int passed) {
        scenario.onActivity(activity -> {
            GameView view = activity.findViewById(R.id.game_view);
            assertEquals(passed, view.getPassedCount());
        });
    }

    private void clickCell(int x, int y) {
        onView(withId(R.id.game_view)).perform(clickCellAction(x, y));
    }

    private void clickToolbar(int action) {
        onView(withId(R.id.toolbar_view)).perform(clickToolbarAction(action));
    }

    private void clickDialogPositive() {
        onView(withId(android.R.id.button1)).perform(click());
    }

    private void clickDialogNegative() {
        onView(withId(android.R.id.button2)).perform(click());
    }

    private static ViewAction clickCellAction(final int x, final int y) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(GameView.class);
            }

            @Override
            public String getDescription() {
                return "click board cell (" + x + "," + y + ")";
            }

            @Override
            public void perform(UiController uiController, View view) {
                final PointF center = ((GameView) view).getCellCenter(x, y);
                final int[] location = new int[2];
                view.getLocationOnScreen(location);
                CoordinatesProvider coordinates = ignored -> new float[]{
                        location[0] + center.x, location[1] + center.y};
                new GeneralClickAction(Tap.SINGLE, coordinates, Press.FINGER)
                        .perform(uiController, view);
            }
        };
    }

    private static ViewAction clickToolbarAction(final int action) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ToolbarView.class);
            }

            @Override
            public String getDescription() {
                return "click toolbar action " + action;
            }

            @Override
            public void perform(UiController uiController, View view) {
                final PointF center = ((ToolbarView) view).getButtonCenter(action);
                final int[] location = new int[2];
                view.getLocationOnScreen(location);
                CoordinatesProvider coordinates = ignored -> new float[]{
                        location[0] + center.x, location[1] + center.y};
                new GeneralClickAction(Tap.SINGLE, coordinates, Press.FINGER)
                        .perform(uiController, view);
            }
        };
    }
}
