// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2010 Roman T. Gritsulyak
// Android port: 2026
package com.gritsulyak.prevtonextfill.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Tests for the ported game rules, level data and the original jump behaviour. */
public class GameLogicTest {

    private static Target t(int x, int y, int value) {
        return new Target(new XY(x, y), value);
    }

    @Test
    public void levelDataIsCompleteAndInsideItsBounds() {
        assertEquals(30, Levels.ALL.size());
        assertEquals(29, Levels.PLAYABLE_COUNT);
        assertEquals("OPAPA", Levels.ALL.get(0).code);
        assertEquals("FLOWERS", Levels.ALL.get(28).code);
        assertEquals("NEXTSTAGE", Levels.ALL.get(29).code);

        for (int i = 0; i < Levels.PLAYABLE_COUNT; i++) {
            GameField field = Levels.ALL.get(i);
            assertTrue(field.code, field.xSize > 0 && field.ySize > 0);
            int zeroCount = 0;
            for (Target target : field.targets) {
                assertTrue(field.code + " target " + target,
                        target.xy.x >= 0 && target.xy.x < field.xSize
                                && target.xy.y >= 0 && target.xy.y < field.ySize);
                if (target.value == 0) {
                    zeroCount++;
                }
            }
            assertEquals(field.code + " must have exactly one start target", 1, zeroCount);
        }
    }

    @Test
    public void gameStartsOnTheZeroTarget() {
        Game game = new Game(0);
        assertEquals(new XY(3, 3), game.current);
        assertEquals(0, game.previousTargetValue);
        assertEquals(1, game.path.size());
        assertEquals(1, game.passed.size());
        assertTrue(game.passed.contains(new XY(3, 3)));
        assertEquals(1, game.lastAllowedTargets.size());
        assertEquals(0, game.lastAllowedTargets.get(0).value);
    }

    @Test
    public void plainCellsMustBeOrthogonallyAdjacent() {
        GameField field = new GameField("TEST", 3, 3, t(0, 0, 0));
        Game game = new Game(field, 0);

        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(2, 2)));
        assertEquals(DrawType.LEFT_RIGHT, game.whatAllowedOrNewGame(new XY(1, 0)));

        game.refreshCurrent(new XY(1, 0));
        assertEquals(new XY(1, 0), game.current);
        assertEquals(DrawType.LEFT_RIGHT, game.whatAllowedOrNewGame(new XY(2, 0)));
        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(2, 2)));
    }

    @Test
    public void targetCannotBeSkipped() {
        GameField field = new GameField("SKIP", 2, 1, t(0, 0, 0), t(1, 0, 2));
        Game game = new Game(field, 0);

        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(1, 0)));
        assertEquals(0, game.previousTargetValue);
        assertFalse(game.isOnPath(new XY(1, 0)));
    }

    @Test
    public void targetsAdvanceExactlyOneStepAtATime() {
        GameField field = new GameField("CHAIN", 3, 1,
                t(0, 0, 0), t(1, 0, 1), t(2, 0, 2));
        Game game = new Game(field, 0);

        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(2, 0)));

        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(1, 0)));
        game.refreshCurrent(new XY(1, 0));
        assertEquals(1, game.previousTargetValue);

        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(2, 0)));
        game.refreshCurrent(new XY(2, 0));
        assertEquals(2, game.previousTargetValue);
        assertTrue(game.checkIfGameDone());
    }

    @Test
    public void plainMovesDoNotAdvanceTheTargetSequence() {
        GameField field = new GameField("PLAIN", 3, 1, t(0, 0, 0), t(2, 0, 1));
        Game game = new Game(field, 0);

        assertEquals(DrawType.LEFT_RIGHT, game.whatAllowedOrNewGame(new XY(1, 0)));
        game.refreshCurrent(new XY(1, 0));
        assertEquals(0, game.previousTargetValue);

        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(2, 0)));
        game.refreshCurrent(new XY(2, 0));
        assertEquals(1, game.previousTargetValue);
        assertTrue(game.checkIfGameDone());
    }

    @Test
    public void cannotJumpBackFromAPlainCell() {
        GameField field = new GameField("NOJUMP", 2, 2,
                t(0, 0, 0), t(1, 0, 1), t(0, 1, 1));
        Game game = new Game(field, 0);

        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(1, 0)));
        game.refreshCurrent(new XY(1, 0));

        assertEquals(DrawType.UP_DOWN, game.whatAllowedOrNewGame(new XY(1, 1)));
        game.refreshCurrent(new XY(1, 1));

        // (0,0) is a reached target, but the figure is on a plain cell now.
        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(0, 0)));
    }

    @Test
    public void reachedTargetCanBeSelectedAgainAndResetsTheSequence() {
        GameField field = new GameField("JUMP", 2, 2,
                t(0, 0, 0), t(1, 0, 1), t(0, 1, 1));
        Game game = new Game(field, 0);

        // Reach the first number 1.
        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(1, 0)));
        game.refreshCurrent(new XY(1, 0));
        assertEquals(1, game.previousTargetValue);

        // Jump back to the reached number 0. This is the original
        // "PreviousTargetType" branch and resets the sequence to 0.
        assertEquals(DrawType.PREVIOUS_TARGET, game.whatAllowedOrNewGame(new XY(0, 0)));
        game.refreshCurrent(new XY(0, 0));
        assertEquals(new XY(0, 0), game.current);
        assertEquals(0, game.previousTargetValue);

        // Now the other number 1 can be reached.
        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(0, 1)));
        game.refreshCurrent(new XY(0, 1));
        assertEquals(1, game.previousTargetValue);

        // Fill the last plain cell.
        assertEquals(DrawType.LEFT_RIGHT, game.whatAllowedOrNewGame(new XY(1, 1)));
        game.refreshCurrent(new XY(1, 1));
        assertTrue(game.checkIfGameDone());
    }

    @Test
    public void duplicateTargetIsBlockedWithoutAJump() {
        GameField field = new GameField("DUP", 2, 2,
                t(0, 0, 0), t(1, 0, 1), t(0, 1, 1));
        Game game = new Game(field, 0);

        assertEquals(DrawType.TARGET, game.whatAllowedOrNewGame(new XY(1, 0)));
        game.refreshCurrent(new XY(1, 0));

        assertEquals(DrawType.UP_DOWN, game.whatAllowedOrNewGame(new XY(1, 1)));
        game.refreshCurrent(new XY(1, 1));

        assertEquals(DrawType.NOT_A_FIELD, game.whatAllowedOrNewGame(new XY(0, 1)));
    }

    @Test
    public void pathShapesMatchTheOriginalTurnTable() {
        assertEquals(DrawType.LEFT_RIGHT,
                PathShape.forCell(new XY(0, 1), new XY(1, 1), new XY(2, 1)));
        assertEquals(DrawType.UP_DOWN,
                PathShape.forCell(new XY(1, 0), new XY(1, 1), new XY(1, 2)));

        // prev-left, next-down through the cell -> "LeftDown" art.
        assertEquals(DrawType.LEFT_DOWN,
                PathShape.forCell(new XY(0, 1), new XY(1, 1), new XY(1, 2)));

        // prev-up, next-right -> "UpRight" art.
        assertEquals(DrawType.UP_RIGHT,
                PathShape.forCell(new XY(1, 0), new XY(1, 1), new XY(2, 1)));

        // prev-right, next-up also uses the "UpRight" art.
        assertEquals(DrawType.UP_RIGHT,
                PathShape.forCell(new XY(2, 1), new XY(1, 1), new XY(1, 0)));

        // prev-left, next-up -> "UpLeft" art.
        assertEquals(DrawType.UP_LEFT,
                PathShape.forCell(new XY(0, 1), new XY(1, 1), new XY(1, 0)));

        // prev-right, next-down -> "RightDown" art.
        assertEquals(DrawType.RIGHT_DOWN,
                PathShape.forCell(new XY(2, 1), new XY(1, 1), new XY(1, 2)));
    }

    @Test
    public void headDirectionsMatchTheOriginalBitmapMapping() {
        assertEquals(DrawType.UP_DOWN,
                PathShape.travelDirection(new XY(1, 0), new XY(1, 1))); // down
        assertEquals(DrawType.DOWN_UP,
                PathShape.travelDirection(new XY(1, 2), new XY(1, 1))); // up
        assertEquals(DrawType.RIGHT_LEFT,
                PathShape.travelDirection(new XY(2, 1), new XY(1, 1))); // left
        assertEquals(DrawType.LEFT_RIGHT,
                PathShape.travelDirection(new XY(0, 1), new XY(1, 1))); // right
    }

    @Test
    public void everyBundledLevelHasASolutionUnderTheStrictRules() {
        for (int i = 0; i < Levels.PLAYABLE_COUNT; i++) {
            GameField field = Levels.ALL.get(i);
            Solver solver = new Solver(field);
            assertTrue("level " + i + " (" + field.code + ") should be solvable",
                    solver.solve());
        }
    }

    @Test
    public void cheatCodesAreUniqueAndResolvable() {
        Map<String, Integer> seen = new HashMap<>();
        for (int i = 0; i < Levels.ALL.size(); i++) {
            String code = Levels.ALL.get(i).code;
            assertFalse("duplicate code " + code, seen.containsKey(code));
            seen.put(code, i);
            assertEquals(i, Levels.indexOfCode(code));
        }
        assertEquals(-1, Levels.indexOfCode("NO SUCH LEVEL"));
    }

    @Test
    public void nextStageIsTheOriginalEndMarker() {
        GameField field = Levels.ALL.get(Levels.PLAYABLE_COUNT);
        assertEquals("NEXTSTAGE", field.code);
        assertNull(field.targetAt(new XY(3, 3)));
        assertNotNull(field.targetAt(new XY(0, 0)));
        assertEquals(88, field.targetAt(new XY(0, 0)).value);
    }

    /**
     * Exhaustive depth-first solver that drives the real {@link Game} class,
     * including the jump-back-to-reached-target rule. It is fast because the
     * bundled boards are small (at most 7x3 for a playable level).
     */
    private static final class Solver {
        private final GameField field;
        private final Set<State> failed = new HashSet<>();

        Solver(GameField field) {
            this.field = field;
        }

        boolean solve() {
            Game game = new Game(field, 0);
            return search(game);
        }

        private boolean search(Game game) {
            if (game.checkIfGameDone()) {
                return true;
            }

            State state = new State(mask(game), game.current, game.previousTargetValue);
            if (!failed.add(state)) {
                return false;
            }

            // The original first branch: while standing on a target, any
            // already reached target can be selected and becomes current.
            if (field.targetAt(game.current) != null) {
                for (XY xy : new ArrayList<>(game.passed)) {
                    if (xy.equals(game.current) || field.targetAt(xy) == null) {
                        continue;
                    }
                    Snapshot snapshot = new Snapshot(game);
                    DrawType type = game.whatAllowedOrNewGame(xy);
                    if (type != DrawType.NOT_A_FIELD) {
                        game.refreshCurrent(xy);
                        if (search(game)) {
                            return true;
                        }
                    }
                    snapshot.restore(game);
                }
            }

            // Ordinary orthogonal move to a cell that has not been filled.
            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] direction : directions) {
                XY next = new XY(game.current.x + direction[0],
                        game.current.y + direction[1]);
                if (next.x < 0 || next.x >= field.xSize
                        || next.y < 0 || next.y >= field.ySize
                        || game.passed.contains(next)) {
                    continue;
                }
                Snapshot snapshot = new Snapshot(game);
                DrawType type = game.whatAllowedOrNewGame(next);
                if (type != DrawType.NOT_A_FIELD) {
                    game.refreshCurrent(next);
                    if (search(game)) {
                        return true;
                    }
                }
                snapshot.restore(game);
            }

            return false;
        }

        private long mask(Game game) {
            long mask = 0L;
            for (XY xy : game.passed) {
                mask |= 1L << (xy.y * field.xSize + xy.x);
            }
            return mask;
        }
    }

    private static final class Snapshot {
        private final XY current;
        private final int previousTargetValue;
        private final List<XY> path;
        private final Set<XY> passed;
        private final List<Target> lastAllowedTargets;

        Snapshot(Game game) {
            current = game.current;
            previousTargetValue = game.previousTargetValue;
            path = new ArrayList<>(game.path);
            passed = new HashSet<>(game.passed);
            lastAllowedTargets = new ArrayList<>(game.lastAllowedTargets);
        }

        void restore(Game game) {
            game.current = current;
            game.previousTargetValue = previousTargetValue;
            game.path.clear();
            game.path.addAll(path);
            game.passed.clear();
            game.passed.addAll(passed);
            game.lastAllowedTargets.clear();
            game.lastAllowedTargets.addAll(lastAllowedTargets);
        }
    }

    private static final class State {
        private final long passedMask;
        private final XY current;
        private final int previousTargetValue;

        State(long passedMask, XY current, int previousTargetValue) {
            this.passedMask = passedMask;
            this.current = current;
            this.previousTargetValue = previousTargetValue;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof State)) {
                return false;
            }
            State state = (State) other;
            return passedMask == state.passedMask
                    && previousTargetValue == state.previousTargetValue
                    && current.equals(state.current);
        }

        @Override
        public int hashCode() {
            return Objects.hash(passedMask, current, previousTargetValue);
        }
    }
}
