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
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.gritsulyak.prevtonextfill.game.DrawType;
import com.gritsulyak.prevtonextfill.game.Game;
import com.gritsulyak.prevtonextfill.game.Levels;
import com.gritsulyak.prevtonextfill.game.PathShape;
import com.gritsulyak.prevtonextfill.game.Target;
import com.gritsulyak.prevtonextfill.game.XY;

/**
 * The playfield. It reproduces the original Win32 look: 30x30 grey bevel
 * buttons, the exact 20x20 monochrome path/head bitmaps, and black target
 * numbers. All sprites are drawn with bitmap filtering disabled so the pixels
 * stay square and identical to the originals.
 */
public final class GameView extends View {
    public interface Listener {
        void onLevelCompleted(int nextRound, String nextCode);
    }

    private static final int CELL_UNITS = 30;
    private static final int ART_UNITS = 20;

    private static final int COLOR_BUTTON = 0xFFD4D0C8;
    private static final int COLOR_BUTTON_PRESSED = 0xFFCFCAC2;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_SHADOW = 0xFF808080;

    private final Paint buttonPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint artPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final RectF artRect = new RectF();

    private final Bitmap pathLeftRight;
    private final Bitmap pathUpDown;
    private final Bitmap pathUpRight;
    private final Bitmap pathUpLeft;
    private final Bitmap pathLeftDown;
    private final Bitmap pathRightDown;
    private final Bitmap headUp;
    private final Bitmap headDown;
    private final Bitmap headLeft;
    private final Bitmap headRight;

    private Game game;
    private Listener listener;
    private boolean completed;
    private float cellSize = 1f;
    private float gridLeft;
    private float gridTop;
    private ToneGenerator tone;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.WHITE);
        setFocusable(true);
        setContentDescription("Game board");

        buttonPaint.setStyle(Paint.Style.FILL);
        borderPaint.setStyle(Paint.Style.FILL);
        artPaint.setFilterBitmap(false);
        artPaint.setAntiAlias(false);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));

        pathLeftRight = load(R.drawable.path_left_right);
        pathUpDown = load(R.drawable.path_up_down);
        pathUpRight = load(R.drawable.path_up_right);
        pathUpLeft = load(R.drawable.path_up_left);
        pathLeftDown = load(R.drawable.path_left_down);
        pathRightDown = load(R.drawable.path_right_down);
        headUp = load(R.drawable.head_up);
        headDown = load(R.drawable.head_down);
        headLeft = load(R.drawable.head_left);
        headRight = load(R.drawable.head_right);

        game = new Game(0);
    }

    private Bitmap load(int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void startLevel(int index) {
        game.gameInitNumber(index);
        completed = false;
        computeLayout();
        invalidate();
    }

    public void newGame() {
        startLevel(0);
    }

    public void restartCurrent() {
        startLevel(game.gameRound);
    }

    public int getCurrentRound() {
        return game.gameRound;
    }

    public String getCurrentCode() {
        return game.gameField.code;
    }

    public XY getCurrentPosition() {
        return game.current;
    }

    public int getPassedCount() {
        return game.passed.size();
    }

    public int getCellCount() {
        return game.gameField.cellCount();
    }

    public int getPreviousTargetValue() {
        return game.previousTargetValue;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getColumns() {
        return game.gameField.xSize;
    }

    public int getRows() {
        return game.gameField.ySize;
    }

    /** Centre of a cell in this view's coordinate system (used by UI tests). */
    public PointF getCellCenter(int column, int row) {
        return new PointF(
                gridLeft + column * cellSize + cellSize / 2f,
                gridTop + row * cellSize + cellSize / 2f);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeLayout();
    }

    private void computeLayout() {
        if (game == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        int columns = Math.max(1, game.gameField.xSize);
        int rows = Math.max(1, game.gameField.ySize);

        float padding = dp(12f);
        float availableWidth = Math.max(1f, getWidth() - 2f * padding);
        float availableHeight = Math.max(1f, getHeight() - 2f * padding);
        float maxCell = dp(96f);

        float cell = Math.min(Math.min(availableWidth / columns, availableHeight / rows), maxCell);
        cell = Math.max(1f, (float) Math.floor(cell));

        cellSize = cell;
        gridLeft = (getWidth() - columns * cell) / 2f;
        gridTop = (getHeight() - rows * cell) / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (game == null) {
            return;
        }

        canvas.drawColor(Color.WHITE);

        int columns = game.gameField.xSize;
        int rows = game.gameField.ySize;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                drawCell(canvas, x, y);
            }
        }
    }

    private void drawCell(Canvas canvas, int x, int y) {
        float left = gridLeft + x * cellSize;
        float top = gridTop + y * cellSize;
        cellRect.set(left, top, left + cellSize, top + cellSize);

        XY xy = new XY(x, y);
        Target target = game.gameField.targetAt(x, y);
        boolean onPath = game.passed.contains(xy);

        if (target != null) {
            drawButton(canvas, cellRect, onPath);
            drawNumber(canvas, cellRect, target.value, onPath);
            return;
        }

        drawButton(canvas, cellRect, false);
        if (onPath) {
            if (game.current.equals(xy)) {
                drawHead(canvas, cellRect, xy);
            } else {
                drawPath(canvas, cellRect, xy);
            }
        }
    }

    private void drawButton(Canvas canvas, RectF rect, boolean pressed) {
        float bevel = Math.max(1f, Math.round(cellSize / CELL_UNITS));

        buttonPaint.setColor(pressed ? COLOR_BUTTON_PRESSED : COLOR_BUTTON);
        canvas.drawRect(rect, buttonPaint);

        if (pressed) {
            borderPaint.setColor(COLOR_SHADOW);
            canvas.drawRect(rect.left, rect.top, rect.right, rect.top + bevel, borderPaint);
            canvas.drawRect(rect.left, rect.top, rect.left + bevel, rect.bottom, borderPaint);

            borderPaint.setColor(COLOR_WHITE);
            canvas.drawRect(rect.left, rect.bottom - bevel, rect.right, rect.bottom, borderPaint);
            canvas.drawRect(rect.right - bevel, rect.top, rect.right, rect.bottom, borderPaint);
        } else {
            borderPaint.setColor(COLOR_WHITE);
            canvas.drawRect(rect.left, rect.top, rect.right, rect.top + bevel, borderPaint);
            canvas.drawRect(rect.left, rect.top, rect.left + bevel, rect.bottom, borderPaint);

            borderPaint.setColor(COLOR_SHADOW);
            canvas.drawRect(rect.left, rect.bottom - bevel, rect.right, rect.bottom, borderPaint);
            canvas.drawRect(rect.right - bevel, rect.top, rect.right, rect.bottom, borderPaint);
        }
    }

    private void drawNumber(Canvas canvas, RectF rect, int value, boolean pressed) {
        String text = Integer.toString(value);

        float textSize = cellSize * 0.40f;
        textPaint.setTextSize(textSize);
        float width = textPaint.measureText(text);
        float maxWidth = cellSize * 0.80f;
        if (width > maxWidth) {
            textSize *= maxWidth / width;
            textPaint.setTextSize(textSize);
        }

        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float baseline = rect.centerY() - (metrics.ascent + metrics.descent) / 2f;

        // Win32 pushes the caption one pixel down/right while a button is held
        // down (BM_SETSTATE); reached targets stay pressed.
        if (pressed) {
            float shift = Math.max(1f, Math.round(cellSize / CELL_UNITS));
            baseline += shift;
            canvas.drawText(text, rect.centerX() + shift, baseline, textPaint);
        } else {
            canvas.drawText(text, rect.centerX(), baseline, textPaint);
        }
    }

    private void drawHead(Canvas canvas, RectF rect, XY xy) {
        int index = game.path.lastIndexOf(xy);
        if (index <= 0) {
            return;
        }
        XY previous = game.path.get(index - 1);
        DrawType direction = PathShape.travelDirection(previous, xy);
        drawSprite(canvas, rect, headFor(direction));
    }

    private void drawPath(Canvas canvas, RectF rect, XY xy) {
        int index = game.path.indexOf(xy);
        if (index < 0 || index + 1 >= game.path.size()) {
            return;
        }
        XY previous = index > 0 ? game.path.get(index - 1) : null;
        XY next = game.path.get(index + 1);
        if (previous == null) {
            return;
        }
        drawSprite(canvas, rect, pathFor(PathShape.forCell(previous, xy, next)));
    }

    private Bitmap headFor(DrawType direction) {
        if (direction == null) {
            return null;
        }
        switch (direction) {
            case UP_DOWN:
                return headDown;
            case DOWN_UP:
                return headUp;
            case LEFT_RIGHT:
                return headRight;
            case RIGHT_LEFT:
                return headLeft;
            default:
                return null;
        }
    }

    private Bitmap pathFor(DrawType shape) {
        if (shape == null) {
            return pathUpDown;
        }
        switch (shape) {
            case LEFT_RIGHT:
            case RIGHT_LEFT:
                return pathLeftRight;
            case LEFT_DOWN:
                return pathLeftDown;
            case RIGHT_DOWN:
                return pathRightDown;
            case UP_DOWN:
            case DOWN_UP:
                return pathUpDown;
            case UP_RIGHT:
                return pathUpRight;
            case UP_LEFT:
                return pathUpLeft;
            default:
                return pathUpDown;
        }
    }

    private void drawSprite(Canvas canvas, RectF rect, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        float size = cellSize * ART_UNITS / CELL_UNITS;
        float left = rect.centerX() - size / 2f;
        float top = rect.centerY() - size / 2f;
        artRect.set(left, top, left + size, top + size);
        canvas.drawBitmap(bitmap, null, artRect, artPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (game == null) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                if (!completed) {
                    handleTap(event.getX(), event.getY());
                }
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
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

    private void handleTap(float screenX, float screenY) {
        int column = (int) Math.floor((screenX - gridLeft) / cellSize);
        int row = (int) Math.floor((screenY - gridTop) / cellSize);
        if (column < 0 || column >= game.gameField.xSize
                || row < 0 || row >= game.gameField.ySize) {
            return;
        }

        XY xy = new XY(column, row);
        DrawType type = game.whatAllowedOrNewGame(xy);
        if (type == DrawType.NOT_A_FIELD) {
            beep();
            return;
        }

        game.refreshCurrent(xy);
        invalidate();

        if (game.checkIfGameDone()) {
            completed = true;
            int nextRound = (game.gameRound + 1) % Levels.PLAYABLE_COUNT;
            if (listener != null) {
                listener.onLevelCompleted(nextRound, Levels.ALL.get(nextRound).code);
            }
        }
    }

    private void beep() {
        try {
            if (tone == null) {
                tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40);
            }
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80);
        } catch (RuntimeException ignored) {
            // Some devices have no usable tone generator; haptics still work.
        }
        performHapticFeedback(HapticFeedbackConstants.REJECT);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (tone != null) {
            tone.release();
            tone = null;
        }
        super.onDetachedFromWindow();
    }
}
