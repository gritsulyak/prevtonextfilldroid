// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The complete game model, ported from game.cpp.
 *
 * <p>Rules: fill the whole field. The figure starts on the target numbered 0.
 * It moves to an orthogonally adjacent, not-yet-filled cell. A numbered cell
 * may only be entered when its number is exactly {@code previousTargetValue + 1}.
 * When the figure is standing on a numbered cell, it may jump back to any
 * numbered cell that has already been reached; that cell's number becomes the
 * new {@code previousTargetValue}. This is what makes levels with repeated
 * numbers solvable ("you can start moving from a filled block only after you
 * have reached it").</p>
 */
public final class Game {
    public int gameRound;
    public XY current;
    public final List<Target> lastAllowedTargets = new ArrayList<>();
    public int previousTargetValue;

    /** The single path used by the original implementation (m_Pathes[0]). */
    public final List<XY> path = new ArrayList<>();
    public final Set<XY> passed = new HashSet<>();
    public GameField gameField;
    public int currentPathIndex;

    public Game() {
        this(0);
    }

    public Game(int round) {
        gameInitNumber(round);
    }

    /** Constructor used by unit tests and for custom fields. */
    public Game(GameField field, int round) {
        gameInit(field, round);
    }

    public boolean clean() {
        passed.clear();
        path.clear();
        lastAllowedTargets.clear();
        return true;
    }

    public boolean gameInitNumber(int num) {
        return gameInit(Levels.ALL.get(num), num);
    }

    public boolean gameInit(GameField field, int round) {
        gameRound = round;
        gameField = field;

        current = null;
        for (Target target : field.targets) {
            if (target.value == 0) {
                current = target.xy;
                break;
            }
        }
        // The final NEXTSTAGE placeholder has no zero target. The C++ code
        // leaves m_XYcurrent default-constructed, i.e. (0, 0).
        if (current == null) {
            current = new XY(0, 0);
        }

        currentPathIndex = 0;
        previousTargetValue = 0;

        path.clear();
        path.add(current);

        passed.clear();
        passed.add(current);

        lastAllowedTargets.clear();
        lastAllowedTargets.add(new Target(current, previousTargetValue));
        return true;
    }

    public boolean isOnPath(XY xy) {
        return path.contains(xy);
    }

    public Target targetAt(XY xy) {
        return gameField.targetAt(xy);
    }

    /**
     * If the click is allowed, returns the drawing type and updates the
     * sequence state. Otherwise returns {@link DrawType#NOT_A_FIELD}.
     */
    public DrawType whatAllowedOrNewGame(XY xy) {
        Target found = gameField.targetAt(xy);
        Target currentTarget = gameField.targetAt(current);

        boolean isInPathes = isOnPath(xy);
        boolean isTargetField = found != null;
        boolean isCurrentTarget = currentTarget != null;

        // 13.05.2010 fix in the original: no frozen heads. Any already reached
        // target can be selected again while the figure stands on a target.
        if (isInPathes && isTargetField && isCurrentTarget) {
            previousTargetValue = found.value;
            return DrawType.PREVIOUS_TARGET;
        }

        if (isInPathes && isTargetField && !isCurrentTarget) {
            return DrawType.NOT_A_FIELD;
        }

        if (isInPathes && !isTargetField) {
            return DrawType.NOT_A_FIELD;
        }

        if (!isInPathes && isTargetField) { // unfilled target
            if (isOrthogonallyAdjacent(current, xy)) {
                // It is possible to move only from the previous number to the next.
                if (previousTargetValue == found.value - 1) {
                    previousTargetValue++;
                    lastAllowedTargets.add(found);
                    return DrawType.TARGET;
                } else {
                    return DrawType.NOT_A_FIELD;
                }
            }
        }

        // A plain, not numbered cell can be filled when adjacent.
        if (current.x == xy.x && current.y == xy.y - 1) {
            return DrawType.UP_DOWN;
        }
        if (current.x == xy.x && current.y == xy.y + 1) {
            return DrawType.DOWN_UP;
        }
        if (current.x == xy.x - 1 && current.y == xy.y) {
            return DrawType.LEFT_RIGHT;
        }
        if (current.x == xy.x + 1 && current.y == xy.y) {
            return DrawType.RIGHT_LEFT;
        }

        return DrawType.NOT_A_FIELD;
    }

    private static boolean isOrthogonallyAdjacent(XY a, XY b) {
        return (a.x == b.x && (a.y == b.y - 1 || a.y == b.y + 1))
                || (a.y == b.y && (a.x == b.x - 1 || a.x == b.x + 1));
    }

    /** Commits a successful move and returns the previous figure position. */
    public XY refreshCurrent(XY xy) {
        XY previous = current;
        current = xy;
        path.add(xy);
        passed.add(xy);
        return previous;
    }

    public boolean checkIfGameDone() {
        return passed.size() == gameField.cellCount();
    }
}
