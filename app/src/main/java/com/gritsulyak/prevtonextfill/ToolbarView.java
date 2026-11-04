// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A Win32-like toolbar: flat 16x16 pixel icons on a #c0c0c0 strip, with the
 * same four actions as the original: New Game, Level From Beginning (cheat
 * code), About and Exit.
 */
public final class ToolbarView extends View {
    public interface Listener {
        void onToolbarAction(int action);
    }

    public static final int ACTION_NEW_GAME = 0;
    public static final int ACTION_NEW_LEVEL = 1;
    public static final int ACTION_ABOUT = 2;
    public static final int ACTION_EXIT = 3;

    private static final int COLOR_TOOLBAR = 0xFFC0C0C0;
    private static final int COLOR_PRESSED = 0xFFB8B8B8;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_SHADOW = 0xFF808080;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap[] icons = new Bitmap[4];
    private final RectF[] buttonRects = new RectF[4];
    private final RectF iconRect = new RectF();

    private Listener listener;
    private int pressedIndex = -1;

    public ToolbarView(Context context) {
        this(context, null);
    }

    public ToolbarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToolbarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(COLOR_TOOLBAR);
        setFocusable(true);
        setContentDescription("123Fill toolbar");

        iconPaint.setFilterBitmap(false);
        iconPaint.setAntiAlias(false);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextSize(dp(18f));

        icons[ACTION_NEW_GAME] = load(R.drawable.toolbar_new);
        icons[ACTION_NEW_LEVEL] = load(R.drawable.toolbar_level);
        icons[ACTION_ABOUT] = load(R.drawable.toolbar_about);
        icons[ACTION_EXIT] = load(R.drawable.toolbar_exit);

        for (int i = 0; i < buttonRects.length; i++) {
            buttonRects[i] = new RectF();
        }
    }

    private Bitmap load(int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Centre of one toolbar button in this view's coordinate system (UI tests). */
    public PointF getButtonCenter(int action) {
        if (action < 0 || action >= buttonRects.length || buttonRects[action].isEmpty()) {
            return null;
        }
        RectF rect = buttonRects[action];
        return new PointF(rect.centerX(), rect.centerY());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutButtons();
    }

    private void layoutButtons() {
        float height = getHeight();
        float button = Math.min(dp(48f), height - dp(8f));
        button = Math.max(1f, button);
        float gap = dp(2f);
        float left = dp(4f);
        float top = (height - button) / 2f;

        for (int i = 0; i < buttonRects.length; i++) {
            buttonRects[i].set(left, top, left + button, top + button);
            left += button + gap;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(COLOR_TOOLBAR);

        for (int i = 0; i < buttonRects.length; i++) {
            RectF rect = buttonRects[i];
            if (rect.isEmpty()) {
                continue;
            }
            if (i == pressedIndex) {
                drawPressedButton(canvas, rect);
            }
            drawIcon(canvas, rect, icons[i]);
        }

        // Separators match the original toolbar layout: before About and
        // before Exit.
        drawSeparator(canvas, buttonRects[2].left - dp(3f));
        drawSeparator(canvas, buttonRects[3].left - dp(3f));

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = getHeight() / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText("123Fill", getWidth() - dp(12f), baseline, textPaint);
    }

    private void drawPressedButton(Canvas canvas, RectF rect) {
        float bevel = Math.max(1f, dp(1f));

        paint.setColor(COLOR_PRESSED);
        canvas.drawRect(rect, paint);

        paint.setColor(COLOR_SHADOW);
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + bevel, paint);
        canvas.drawRect(rect.left, rect.top, rect.left + bevel, rect.bottom, paint);

        paint.setColor(COLOR_WHITE);
        canvas.drawRect(rect.left, rect.bottom - bevel, rect.right, rect.bottom, paint);
        canvas.drawRect(rect.right - bevel, rect.top, rect.right, rect.bottom, paint);
    }

    private void drawIcon(Canvas canvas, RectF rect, Bitmap icon) {
        if (icon == null) {
            return;
        }
        float size = rect.width() * 2f / 3f;
        float left = rect.centerX() - size / 2f;
        float top = rect.centerY() - size / 2f;
        iconRect.set(left, top, left + size, top + size);
        canvas.drawBitmap(icon, null, iconRect, iconPaint);
    }

    private void drawSeparator(Canvas canvas, float x) {
        float top = dp(8f);
        float bottom = getHeight() - dp(8f);
        paint.setColor(COLOR_SHADOW);
        canvas.drawRect(x, top, x + dp(1f), bottom, paint);
        paint.setColor(COLOR_WHITE);
        canvas.drawRect(x + dp(1f), top, x + dp(2f), bottom, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pressedIndex = hitTest(event.getX(), event.getY());
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE: {
                int hit = hitTest(event.getX(), event.getY());
                if (hit != pressedIndex) {
                    pressedIndex = hit;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int hit = hitTest(event.getX(), event.getY());
                int action = pressedIndex;
                pressedIndex = -1;
                invalidate();
                if (hit >= 0 && hit == action) {
                    performClick();
                    if (listener != null) {
                        listener.onToolbarAction(hit);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
                pressedIndex = -1;
                invalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private int hitTest(float x, float y) {
        for (int i = 0; i < buttonRects.length; i++) {
            if (buttonRects[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }
}
