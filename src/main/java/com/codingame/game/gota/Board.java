package com.codingame.game.gota;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    long seed;
    int HEIGHT;
    int WIDTH;
    public Cell[][] cells;
    List<Unit> units;

    public int getHEIGHT() {
        return HEIGHT;
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public Board(int size, long seed) {
        this.seed = seed;
        HEIGHT= size;
        WIDTH = size;
        cells = new Cell[HEIGHT][WIDTH];
        units = new ArrayList<>();

        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0;  x < WIDTH; ++x) {
                cells[y][x] = new Cell(x, y);
            }
        }
        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0;  x < WIDTH; ++x) {
                Cell cell = cells[y][x];
                if (isInside(y-1, x-1)) cell.neighbours.add(cells[y-1][x-1]);
                if (isInside(y-1, x)) cell.neighbours.add(cells[y-1][x]);
                if (isInside(y-1, x+1)) cell.neighbours.add(cells[y-1][x+1]);
                if (isInside(y, x-1)) cell.neighbours.add(cells[y][x-1]);
                if (isInside(y, x+1)) cell.neighbours.add(cells[y][x+1]);
                if (isInside(y+1, x-1)) cell.neighbours.add(cells[y+1][x-1]);
                if (isInside(y+1, x)) cell.neighbours.add(cells[y+1][x]);
                if (isInside(y+1, x+1)) cell.neighbours.add(cells[y+1][x+1]);
            }
        }
        for (int i = 1; i < size - 1; ++i) {
            units.add(cells[0][i].setUnit(0));
            units.add(cells[HEIGHT - 1][i].setUnit(0));
            units.add(cells[i][0].setUnit(1));
            units.add(cells[i][WIDTH - 1].setUnit(1));
        }
   }

    boolean isInside(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    // Returns if player won.
    public boolean hasPlayerWon(int player) {
        // How many units on my side ?
        int myUnits = 0;
        for (Unit unit:units) {
            if (unit.owner == player) {
                myUnits++;
            }
        }
        // Clear for floodfill
        for (int y = 0; y < HEIGHT; ++y) {
            for (int x = 0;  x < WIDTH; ++x) {
                cells[y][x].visited = false;
            }
        }
        // Find a starting point
        Cell start = null;
        for (Unit unit:units) {
            if (unit.owner == player) {
                start = unit.cell;
                break;
            }
        }
        // Floodfill
        ArrayList<Cell> queue = new ArrayList<>();
        queue.add(start);
        int visited = 1;
        start.visited = true;
        for (int i = 0; i < visited; ++i) {
            Cell current = queue.get(i);
            for (Cell neighbour:current.neighbours ) {
                if (!neighbour.visited && neighbour.unit != null && neighbour.unit.owner == player) {
                    queue.add(neighbour);
                    neighbour.visited = true;
                    visited++;
                }
            }
        }
        return visited == myUnits;
    }

    private ArrayList<Action> getLegalActions(ArrayList<Cell> lineOfAction, int owner) {
        int numUnits = 0;
        for (Cell cell:lineOfAction) {
            if(cell.unit != null) numUnits++;
        }

        ArrayList<Action> actions = new ArrayList<>();
        for (Cell cell:lineOfAction) {
            Unit unit = cell.unit;
            if (unit == null) continue;
            if (unit.owner != owner) continue;
            int start = lineOfAction.indexOf(cell);
            int end;
            // Backwards
            end = start - numUnits;
            if (end >= 0 && isValidMove(lineOfAction, owner, start, end)) {
                Action action = new Action(unit, lineOfAction.get(end));
                actions.add(action);
            }
            // Forwards
            end = start + numUnits;
            if (end < lineOfAction.size() && isValidMove(lineOfAction, owner, start, end)) {
                Action action = new Action(unit, lineOfAction.get(end));
                actions.add(action);
            }
        }
        return actions;
    }

    boolean isValidMove(ArrayList<Cell> lineOfAction, int owner, int start, int end)
    {
        if (end < start) {
            for (int i = start - 1; i > end; --i) {
                Unit unit = lineOfAction.get(i).unit;
                if (unit != null && unit.owner != owner)
                    return false;
            }
        }
        else {
            for (int i = start + 1; i < end; ++i) {
                Unit unit = lineOfAction.get(i).unit;
                if (unit != null && unit.owner != owner)
                    return false;
            }
        }
        Unit unit = lineOfAction.get(end).unit;
        if (unit != null && unit.owner == owner)
            return false;
        return true;
    }

    public ArrayList<Action> getLegalActions(int player) {
        // For each row, column, diagonal,
        // * count the number of units
        // * for each player unit, check whether backward/forward move is valid
        //   * is inside the board
        //   * is not blocked by an opponent unit
        //   * lands on empty or opponent unit
        ArrayList<Action> actions = new ArrayList<>();
        // Rows
        for (int y = 0; y < HEIGHT; ++y) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int x = 0; x < WIDTH; ++x) {
                lineOfAction.add(cells[y][x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }
        // Columns
        for (int x = 0; x < WIDTH; ++x) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int y = 0; y < HEIGHT; ++y) {
                lineOfAction.add(cells[y][x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }
        // Diagonals
        for (int y = 1; y < HEIGHT; ++y) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int x = 0; x <= y; ++x) {
                lineOfAction.add(cells[y-x][x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }
        for (int x = 1; x < WIDTH - 1; ++x) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int y = x; y < HEIGHT; ++y) {
                lineOfAction.add(cells[y][WIDTH - 1 - y + x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }

        for (int y = 0; y < HEIGHT - 1; ++y) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int x = 0; x < WIDTH - y; ++x) {
                lineOfAction.add(cells[y + x][x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }
        for (int x = 1; x < WIDTH - 1; ++x) {
            ArrayList<Cell> lineOfAction = new ArrayList<>();
            for (int y = 0; y < HEIGHT - x; ++y) {
                lineOfAction.add(cells[y][y + x]);
            }
            actions.addAll(getLegalActions(lineOfAction, player));
        }
        return actions;
    }

    public void applyAction(Unit unit, Cell target) {
        units.remove(target.unit);
        unit.moveTo(target);
    }
}
