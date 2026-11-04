// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

/** A numbered target cell, the Android equivalent of the original Target struct. */
public final class Target {
    public final XY xy;
    public final int value;

    public Target(XY xy, int value) {
        this.xy = xy;
        this.value = value;
    }

    public Target(int x, int y, int value) {
        this(new XY(x, y), value);
    }

    @Override
    public String toString() {
        return "Target" + xy + "=" + value;
    }
}
