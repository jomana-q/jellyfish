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
    /**
     * openCell (חשיפת תא)
     * פותח תא ספציפי ומחיל את לוגיקת המשחק הבסיסית.
     * Open Cell: Main game logic entry point.
     *
     * @param row The row index / מספר השורה
     * @param col The column index / מספר העמודה
     * @param session The current game session / סשן המשחק הנוכחי
     */
    public void openCell(int row, int col, GameSession session) {
        
        // נשתמש ב-getCell של מפתח 1 לבדיקת גבולות ותקינות
        Cell cell = getCell(row, col);

        // תנאי יציאה: התא אינו קיים (מחוץ לגבולות), כבר נחשף או מסומן בדגל.
        if (cell == null || cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        // חשיפת התא
        cell.setRevealed(true);

        // החלת לוגיקה בהתאם לסוג התא
        switch (cell.getType()) {
            case MINE:
                // מוקש: הפחתת חיים (שימוש במשימת מפתח 3)
                session.decreaseLives();
                break;

            case EMPTY:
                // תא ריק: עדכון ניקוד והפעלת קסקדה
                session.updateScore(1);
                cascade(row, col);
                break;

            case NUMBER:
            case QUESTION:
            case SURPRISE:
                // תא מספר/מיוחד: עדכון ניקוד בלבד
                session.updateScore(1);
                break;
        }
    }


    /**
     * cascade (קסקדה רקורסיבית)
     * פונקציה תהודה (רקורסיבית) לניקוי שטח ריק של תאים מחוברים.
     *
     * @param row שורת התא הנוכחי
     * @param col עמודת התא הנוכחי
     */
    private void cascade(int row, int col) {
        // לולאה על 8 השכנים (i ו-j מ-1- עד 1+)
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                
                // דלג על התא הנוכחי
                if (i == 0 && j == 0) continue;

                int r = row + i;
                int c = col + j;

                // השגת השכן (getCell דואג לבדיקת גבולות)
                Cell neighbor = getCell(r, c);

                // תנאי חשיפה: השכן קיים, לא נחשף, לא דגל, ולא מוקש.
                if (neighbor != null && !neighbor.isRevealed() && !neighbor.isFlagged() && neighbor.getType() != CellType.MINE) {
                    
                    neighbor.setRevealed(true); // חשיפת התא השכן

                    // אם התא השכן ריק, המשך רקורסיה (קסקדה)
                    if (neighbor.getType() == CellType.EMPTY) {
                        cascade(r, c);
                    }
                    // אם התא השכן הוא מספר, עוצרים שם.
                }
            }
        }
    }
}
