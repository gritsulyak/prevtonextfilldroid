// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

/**
 * Pure geometry helpers copied from GameDrawer::RecalcShapeAndDrawElement and
 * the head-image mapping in GameDrawer::initTypeDrawMatrix.
 */
public final class PathShape {
    private PathShape() {
    }

    /**
     * Chooses the path sprite for a cell from its predecessor and successor in
     * the path. The order of the checks (and their overriding) matches the
     * original C++ code exactly.
     */
    public static DrawType forCell(XY previous, XY cell, XY next) {
        DrawType type = null;

        // Horizontal.
        if ((previous.x > next.x || previous.x < next.x) && previous.y == next.y) {
            type = DrawType.LEFT_RIGHT;
        }
        // Vertical.
        if ((previous.y > next.y || previous.y < next.y) && previous.x == next.x) {
            type = DrawType.UP_DOWN;
        }

        // -+
        //  |
        if (previous.x < next.x && previous.y < next.y && cell.y == previous.y) {
            type = DrawType.LEFT_DOWN;
        }
        if (next.x < previous.x && next.y < previous.y && cell.x == previous.x) {
            type = DrawType.LEFT_DOWN;
        }

        //   |
        //   +-
        if (previous.x < next.x && previous.y < next.y && cell.x == previous.x) {
            type = DrawType.UP_RIGHT;
        }
        if (next.x < previous.x && next.y < previous.y && cell.y == previous.y) {
            type = DrawType.UP_RIGHT;
        }

        //     |
        //   -+
        if (previous.x > next.x && previous.y < next.y && cell.x == previous.x) {
            type = DrawType.UP_LEFT;
        }
        if (next.x > previous.x && next.y < previous.y && cell.y == previous.y) {
            type = DrawType.UP_LEFT;
        }

        //    +-
        //    |
        if (previous.x > next.x && previous.y < next.y && cell.y == previous.y) {
            type = DrawType.RIGHT_DOWN;
        }
        if (next.x > previous.x && next.y < previous.y && cell.x == previous.x) {
            type = DrawType.RIGHT_DOWN;
        }

        // Every real path cell matches at least one branch. The fallback keeps
        // the renderer total without changing any valid case.
        return type == null ? DrawType.UP_DOWN : type;
    }

    /**
     * Direction of travel from {@code previous} to {@code current}. The names
     * match the original what_allowed_or_new_game() return values, which are
     * also the keys used to choose the head bitmap.
     */
    public static DrawType travelDirection(XY previous, XY current) {
        if (previous == null) {
            return null;
        }
        if (previous.x == current.x && previous.y == current.y - 1) {
            return DrawType.UP_DOWN; // moving down
        }
        if (previous.x == current.x && previous.y == current.y + 1) {
            return DrawType.DOWN_UP; // moving up
        }
        if (previous.x == current.x - 1 && previous.y == current.y) {
            return DrawType.LEFT_RIGHT; // moving right
        }
        if (previous.x == current.x + 1 && previous.y == current.y) {
            return DrawType.RIGHT_LEFT; // moving left
        }
        return null;
    }
}
