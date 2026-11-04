// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Level data transcribed verbatim from the original Win32 source
 * (game_fields.cpp, initializeGlobalFields).
 */
public final class Levels {
    private Levels() {
    }

    /**
     * Number of real levels. Index 29 ("NEXTSTAGE") is the original C++ end
     * marker: it has no zero target and cannot be played. It remains in
     * {@link #ALL} so the cheat-code lookup is byte-for-byte compatible, but
     * the game loop wraps back to OPAPA after the last real level.
     */
    public static final int PLAYABLE_COUNT = 29;

    public static final List<GameField> ALL = Collections.unmodifiableList(Arrays.asList(
            // 0: OPAPA
            new GameField("OPAPA", 4, 4, t(3, 3, 0), t(0, 0, 1), t(1, 2, 2), t(2, 2, 3), t(2, 0, 4), t(3, 1, 5)),
            // 1: COOL
            new GameField("COOL", 4, 4, t(0, 3, 0), t(0, 1, 1), t(2, 3, 1), t(0, 0, 2), t(1, 2, 2), t(3, 1, 2), t(3, 2, 3), t(3, 0, 3)),
            // 2: SIMPLE
            new GameField("SIMPLE", 4, 4, t(1, 1, 0), t(3, 1, 1), t(0, 3, 1), t(3, 0, 2), t(0, 1, 2), t(2, 0, 3), t(1, 0, 4)),
            // 3: ALOT
            new GameField("ALOT", 4, 4, t(1, 0, 0), t(2, 0, 1), t(0, 1, 1), t(2, 1, 2), t(3, 1, 2), t(1, 3, 2), t(2, 2, 2), t(2, 3, 3), t(3, 3, 3)),
            // 4: SAME
            new GameField("SAME", 4, 4, t(0, 0, 0), t(0, 2, 1), t(1, 1, 3), t(1, 2, 2), t(2, 0, 1), t(2, 1, 2), t(2, 2, 3), t(3, 0, 2)),
            // 5: AGAIN
            new GameField("AGAIN", 4, 4, t(0, 1, 4), t(0, 2, 2), t(1, 0, 3), t(1, 2, 0), t(1, 3, 1), t(2, 1, 2), t(3, 0, 1)),
            // 6: RIGHT
            new GameField("RIGHT", 4, 4, t(1, 1, 3), t(1, 2, 4), t(2, 1, 2), t(2, 2, 3), t(3, 0, 1), t(3, 1, 1), t(3, 3, 0)),
            // 7: BORING
            new GameField("BORING", 4, 4, t(0, 1, 0), t(1, 0, 1), t(1, 1, 2), t(1, 2, 3), t(2, 0, 2), t(2, 1, 1), t(3, 0, 3), t(3, 1, 2), t(3, 3, 3)),
            // 8: MINI
            new GameField("MINI", 4, 4, t(3, 3, 0), t(3, 1, 1), t(0, 0, 2), t(1, 3, 3), t(1, 1, 4)),
            // 9: TOME
            new GameField("TOME", 4, 4, t(0, 0, 0), t(1, 2, 1), t(2, 1, 2), t(2, 3, 3), t(2, 0, 3)),
            // 10: INCREASE
            new GameField("INCREASE", 5, 5, t(0, 0, 0), t(1, 1, 1), t(4, 4, 2), t(2, 2, 2), t(2, 4, 3)),
            // 11: PUSH
            new GameField("PUSH", 5, 5, t(0, 0, 0), t(2, 2, 1), t(2, 4, 2), t(1, 1, 2), t(1, 3, 3)),
            // 12: CLICK
            new GameField("CLICK", 5, 5, t(2, 2, 0), t(0, 0, 1), t(0, 4, 1), t(4, 0, 1), t(4, 4, 1), t(4, 1, 2), t(0, 2, 2), t(0, 1, 3), t(4, 3, 3), t(3, 3, 4)),
            // 13: BACK
            new GameField("BACK", 5, 5, t(1, 1, 0), t(0, 4, 1), t(2, 2, 1), t(4, 4, 1)),
            // 14: GEEK
            new GameField("GEEK", 5, 5, t(4, 4, 0), t(1, 1, 1), t(4, 2, 1)),
            // 15: NOTENDACTUALLY
            new GameField("NOTENDACTUALLY", 5, 5, t(2, 0, 0), t(1, 1, 2), t(0, 3, 1), t(3, 1, 1), t(1, 3, 3), t(4, 4, 2)),
            // 16: TWOFIVES
            new GameField("TWOFIVES", 5, 5, t(2, 0, 4), t(1, 1, 5), t(4, 1, 5), t(0, 2, 1), t(3, 2, 3), t(1, 3, 0), t(2, 3, 2), t(0, 4, 2), t(2, 4, 1), t(3, 4, 4)),
            // 17: ADDITION
            new GameField("ADDITION", 5, 4, t(2, 0, 3), t(1, 1, 2), t(2, 1, 3), t(3, 1, 2), t(1, 2, 1), t(2, 2, 1), t(3, 2, 1), t(2, 3, 0)),
            // 18: AGAINAGAIN
            new GameField("AGAINAGAIN", 5, 4, t(2, 0, 5), t(1, 1, 4), t(2, 1, 3), t(3, 1, 2), t(2, 2, 1), t(0, 3, 0)),
            // 19: RECTANGLE
            new GameField("RECTANGLE", 7, 3, t(1, 0, 2), t(2, 0, 3), t(4, 0, 4), t(5, 0, 2), t(2, 1, 1), t(3, 1, 0), t(4, 1, 1), t(1, 2, 2), t(2, 2, 1), t(4, 2, 4), t(5, 2, 3)),
            // 20: ISEASY
            new GameField("ISEASY", 7, 3, t(0, 0, 1), t(4, 0, 1), t(6, 0, 2), t(1, 1, 1), t(3, 1, 0), t(5, 1, 3), t(0, 2, 2), t(2, 2, 2), t(6, 2, 3)),
            // 21: TRYNOW
            new GameField("TRYNOW", 7, 3, t(0, 0, 0), t(6, 0, 2), t(3, 1, 1), t(0, 2, 1), t(6, 2, 2)),
            // 22: PROFITABLE
            new GameField("PROFITABLE", 7, 3, t(0, 0, 0), t(3, 0, 3), t(6, 0, 3), t(0, 2, 1), t(3, 2, 2), t(6, 2, 4)),
            // 23: BANANA
            new GameField("BANANA", 7, 3, t(1, 1, 0), t(2, 1, 1), t(4, 1, 2), t(5, 1, 3)),
            // 24: REDAPPLE
            new GameField("REDAPPLE", 7, 3, t(0, 0, 1), t(1, 0, 2), t(2, 0, 3), t(6, 0, 4), t(0, 2, 0), t(4, 2, 3), t(5, 2, 4), t(6, 2, 5)),
            // 25: BLUECROCO
            new GameField("BLUECROCO", 7, 3, t(0, 0, 1), t(6, 0, 2), t(2, 1, 2), t(3, 1, 0), t(4, 1, 1), t(5, 1, 2), t(0, 2, 1), t(6, 2, 3)),
            // 26: YELLOWGORILLA
            new GameField("YELLOWGORILLA", 7, 3, t(6, 0, 1), t(1, 1, 2), t(3, 1, 0), t(5, 1, 2), t(0, 2, 1)),
            // 27: VIOLETCAT
            new GameField("VIOLETCAT", 7, 3, t(0, 0, 2), t(6, 0, 2), t(1, 1, 1), t(3, 1, 4), t(5, 1, 3), t(0, 2, 2), t(6, 2, 0)),
            // 28: FLOWERS
            new GameField("FLOWERS", 7, 3, t(2, 0, 4), t(3, 0, 3), t(4, 0, 1), t(2, 1, 3), t(3, 1, 2), t(4, 1, 1), t(6, 1, 0), t(2, 2, 3), t(6, 2, 2)),
            // 29: NEXTSTAGE
            new GameField("NEXTSTAGE", 12, 5, t(0, 0, 88), t(0, 1, 88), t(0, 2, 88), t(0, 3, 88), t(0, 4, 88), t(1, 0, 88), t(1, 2, 88), t(1, 4, 88), t(2, 0, 88), t(2, 4, 88), t(4, 0, 88), t(4, 1, 88), t(4, 2, 88), t(4, 3, 88), t(4, 4, 88), t(5, 1, 88), t(6, 2, 88), t(7, 0, 88), t(7, 1, 88), t(7, 2, 88), t(7, 3, 88), t(7, 4, 88), t(9, 0, 88), t(9, 1, 88), t(9, 2, 88), t(9, 3, 88), t(9, 4, 88), t(10, 0, 88), t(10, 4, 88), t(11, 1, 88), t(11, 2, 88), t(11, 3, 88))
    ));

    private static Target t(int x, int y, int value) {
        return new Target(new XY(x, y), value);
    }

    /** Returns the first level whose cheat code matches exactly, or -1. */
    public static int indexOfCode(String code) {
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).code.equals(code)) {
                return i;
            }
        }
        return -1;
    }
}
