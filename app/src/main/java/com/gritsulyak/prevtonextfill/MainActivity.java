// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gritsulyak.prevtonextfill.game.Levels;

/**
 * Single-screen activity. The Win32 toolbar/menu is reproduced with a custom
 * toolbar plus the same modal dialogs (new game, cheat code, about, exit).
 */
public final class MainActivity extends Activity
        implements GameView.Listener, ToolbarView.Listener {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        configureSystemBars();
        buildUi();
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(192, 192, 192));
        window.setNavigationBarColor(Color.rgb(192, 192, 192));

        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        window.getDecorView().setSystemUiVisibility(flags);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        ToolbarView toolbar = new ToolbarView(this);
        toolbar.setId(R.id.toolbar_view);
        toolbar.setListener(this);
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        gameView = new GameView(this);
        gameView.setId(R.id.game_view);
        gameView.setListener(this);
        root.addView(gameView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onToolbarAction(int action) {
        switch (action) {
            case ToolbarView.ACTION_NEW_GAME:
                confirmNewGame();
                break;
            case ToolbarView.ACTION_NEW_LEVEL:
                showCodeDialog();
                break;
            case ToolbarView.ACTION_ABOUT:
                showAbout();
                break;
            case ToolbarView.ACTION_EXIT:
                confirmExit();
                break;
            default:
                break;
        }
    }

    private void confirmNewGame() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setMessage(R.string.new_game_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> gameView.newGame())
                .setNegativeButton(R.string.no, null)
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    private void showCodeDialog() {
        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(R.string.code_label);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        FrameLayout container = new FrameLayout(this);
        int padding = dp(16);
        container.setPadding(padding, dp(4), padding, 0);
        container.addView(input, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.code_title)
                .setView(container)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String code = input.getText().toString().trim();
                    dialog.dismiss();
                    applyCheatCode(code);
                }));
        dialog.show();
    }

    private void applyCheatCode(String code) {
        int index = Levels.indexOfCode(code);
        if (index >= 0 && index < Levels.PLAYABLE_COUNT) {
            gameView.startLevel(index);
            return;
        }

        if (index >= Levels.PLAYABLE_COUNT) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.cheat_code_title)
                    .setMessage(R.string.nextstage_note)
                    .setPositiveButton(R.string.ok, (dialog, which) -> gameView.restartCurrent())
                    .setCancelable(false)
                    .show();
            return;
        }

        showCurrentCodeAndRestart();
    }

    /** Mirrors the original ReStartRound(): show the current code, then restart. */
    private void showCurrentCodeAndRestart() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cheat_code_title)
                .setMessage(gameView.getCurrentCode())
                .setPositiveButton(R.string.ok, (dialog, which) -> gameView.restartCurrent())
                .setCancelable(false)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.about_title)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(R.string.about_text)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null)
                .setNeutralButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onLevelCompleted(int nextRound, String nextCode) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.cheat_code_title)
                .setMessage(nextCode)
                .setPositiveButton(R.string.ok, (dialog, which) -> gameView.startLevel(nextRound))
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        confirmExit();
    }
}
