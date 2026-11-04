// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A level: its dimensions, cheat code and numbered target cells. */
public final class GameField {
    public final String code;
    public final int xSize;
    public final int ySize;
    public final List<Target> targets;

    private final Map<XY, Target> targetsByXY;

    public GameField(String code, int xSize, int ySize, Target... targets) {
        this.code = code;
        this.xSize = xSize;
        this.ySize = ySize;

        List<Target> list = new ArrayList<>(targets.length);
        Map<XY, Target> map = new HashMap<>(targets.length * 2);
        for (Target target : targets) {
            list.add(target);
            map.put(target.xy, target);
        }
        this.targets = Collections.unmodifiableList(list);
        this.targetsByXY = Collections.unmodifiableMap(map);
    }

    public Target targetAt(XY xy) {
        return targetsByXY.get(xy);
    }

    public Target targetAt(int x, int y) {
        return targetsByXY.get(new XY(x, y));
    }

    public int cellCount() {
        return xSize * ySize;
    }
}
