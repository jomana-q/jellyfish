package model;

import java.util.Random;

public class Board {

    private final int rows;
    private final int cols;
    private final int totalMines;

    private Cell[][] grid;

    public Board(Difficulty difficulty) {
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();

        this.grid = new Cell[rows][cols];

        initEmptyCells();
        placeMinesRandomly();
        calculateAdjacentMines();
    }

    // ממלא את כל הלוח בתאים ריקים בהתחלה
    private void initEmptyCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(CellType.EMPTY);
            }
        }
    }

    // מפזר מוקשים באקראי בלי כפילויות
    private void placeMinesRandomly() {
        Random random = new Random();
        int minesPlaced = 0;

        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // אם כבר יש מוקש שם – מדלגים
            if (grid[r][c].getType() != CellType.MINE) {
                grid[r][c] = new Cell(CellType.MINE);
                minesPlaced++;
            }
        }
    }

    // מחשב לכל תא non-mine כמה מוקשים יש סביבו
    private void calculateAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (grid[r][c].getType() == CellType.MINE) {
                    continue;
                }

                int count = 0;

                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {

                        if (dr == 0 && dc == 0) continue;

                        int nr = r + dr;
                        int nc = c + dc;

                        if (isInBounds(nr, nc) &&
                                grid[nr][nc].getType() == CellType.MINE) {
                            count++;
                        }
                    }
                }

                if (count == 0) {
                    grid[r][c].setAdjacentMines(0);
                    // משאירים אותו EMPTY
                } else {
                    grid[r][c].setAdjacentMines(count);
                    // נחשב אותו כ NUMBER
                    // (אפשר להחליט מאוחר יותר אם לשנות את type ל-NUMBER כאן)
                }
            }
        }
    }

    private boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // Getters בסיסיים – יעזרו בהמשך ל-Controller / View
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Cell getCell(int row, int col) {
        if (!isInBounds(row, col)) {
            throw new IllegalArgumentException("Cell out of bounds");
        }
        return grid[row][col];
    }
}
