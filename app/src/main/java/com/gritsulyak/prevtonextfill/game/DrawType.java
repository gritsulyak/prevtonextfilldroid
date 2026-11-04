// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

/** Drawing/move classification, mirroring the original DrawType enum. */
public enum DrawType {
    LEFT_RIGHT,
    RIGHT_LEFT,
    LEFT_DOWN,
    RIGHT_DOWN,
    UP_DOWN,
    DOWN_UP,
    UP_RIGHT,
    UP_LEFT,
    HEAD,
    TARGET,
    PREVIOUS_TARGET,
    NOT_A_FIELD,
    FINAL_FIELD
}
