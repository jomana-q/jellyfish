package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {

    private final int rows;
    private final int cols;
    private final int totalMines;
    private final int questionCount;
    private final int surpriseCount;

    private final Cell[][] grid;
    private final Random random = new Random();

    public Board(Difficulty difficulty) {
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines    = difficulty.getMines();
        this.questionCount = difficulty.getQuestionCount();
        this.surpriseCount = difficulty.getSurpriseCount();

        this.grid = new Cell[rows][cols];

        initEmptyCells();          // כל התאים מתחילים EMPTY
        placeMinesRandomly();      // מפזרים מוקשים
        calculateAdjacentMines();  // הופכים EMPTY->NUMBER כשצריך
        placeSpecialTiles();       // הופכים חלק מה-EMPTY ל-QUESTION/SURPRISE
    }

    // ---------- בניית הלוח ----------

    private void initEmptyCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(CellType.EMPTY);
            }
        }
    }

    // מפזר מוקשים באקראי בלי כפילויות
    private void placeMinesRandomly() {
        int minesPlaced = 0;

        while (minesPlaced < totalMines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (grid[r][c].getType() != CellType.MINE) {
                grid[r][c].setType(CellType.MINE);
                minesPlaced++;
            }
        }
    }

    // מחשב לכל תא non-mine כמה מוקשים יש סביבו
    // ומחליט אם הוא EMPTY (0 שכנים) או NUMBER (1–8 שכנים)
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
                    grid[r][c].setType(CellType.EMPTY);
                } else {
                    grid[r][c].setAdjacentMines(count);
                    grid[r][c].setType(CellType.NUMBER);
                }
            }
        }
    }

    // משבצות ריקות שנשארו הופכות אקראית לשאלה/הפתעה
    private void placeSpecialTiles() {
        List<int[]> emptyCells = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].getType() == CellType.EMPTY) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        Collections.shuffle(emptyCells, random);

        int idx = 0;

        // שאלות
        for (int i = 0; i < questionCount && idx < emptyCells.size(); i++, idx++) {
            int[] pos = emptyCells.get(idx);
            grid[pos[0]][pos[1]].setType(CellType.QUESTION);
        }

        // הפתעות
        for (int i = 0; i < surpriseCount && idx < emptyCells.size(); i++, idx++) {
            int[] pos = emptyCells.get(idx);
            grid[pos[0]][pos[1]].setType(CellType.SURPRISE);
        }
    }

    private boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // ---------- Getters בסיסיים ----------

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

    // ---------- לוגיקת משחק – איטרציה 1 ----------

    /**
     * חשיפת תא ע"י השחקן.
     * לפי האפיון:
     *  - MINE   → hearts-1
     *  - EMPTY / NUMBER / QUESTION / SURPRISE → pts+1
     *  - EMPTY → מפעיל קסקדה של ריקים (וגם מספרים מסביבם)
     */
    public void openCell(int row, int col, GameSession session) {
        revealRecursive(row, col, session);
    }

    // פונקציה פנימית רקורסיבית (משתמשת גם לקסקדה)
    private void revealRecursive(int row, int col, GameSession session) {
        if (!isInBounds(row, col)) {
            return;
        }

        Cell cell = grid[row][col];

        // לא חושפים שוב תא שנחשף או מסומן בדגל
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        cell.setRevealed(true);

        if (cell.getType() == CellType.MINE) {
            // מוקש: מפסידים חיים, בלי ניקוד
            session.decreaseLives();
            return;
        }

        // כל תא שהוא לא מוקש → נקודה אחת
        session.updateScore(+1);

        // אם זה לא תא ריק – לא ממשיכים קסקדה
        if (cell.getType() != CellType.EMPTY) {
            return;
        }

        // קסקדה: חושפים שכנים שהם EMPTY או NUMBER בלבד
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {

                if (dr == 0 && dc == 0) continue;

                int nr = row + dr;
                int nc = col + dc;

                if (!isInBounds(nr, nc)) continue;

                Cell neighbor = grid[nr][nc];

                if (neighbor.isRevealed() || neighbor.isFlagged()) {
                    continue;
                }

                if (neighbor.getType() == CellType.EMPTY ||
                    neighbor.getType() == CellType.NUMBER) {
                    revealRecursive(nr, nc, session);
                }
            }
        }
    }

    /**
     * סימון / ביטול סימון בדגל.
     * לפי האפיון:
     *  - Mine   מסומן בדגל → pts+1  + חשיפת המוקש
     *  - Number / Empty / Question / Surprise מסומנים בדגל → pts-3
     * ביטול סימון: בלי שינוי ניקוד.
     */
    public void toggleFlag(int row, int col, GameSession session) {
        Cell cell = getCell(row, col);

        // אי אפשר לסמן תא שכבר נחשף
        if (cell.isRevealed()) {
            return;
        }

        if (cell.isFlagged()) {
            // ביטול דגל – לא משנים ניקוד
            cell.setFlagged(false);
            return;
        }

        // סימון חדש
        cell.setFlagged(true);

        switch (cell.getType()) {
            case MINE:
                session.updateScore(+1);
                // האפיון אומר "וחושפת המוקש" – אז נסמן גם חשיפה
                cell.setRevealed(true);
                break;

            case NUMBER:
            case EMPTY:
            case QUESTION:
            case SURPRISE:
                session.updateScore(-3);
                break;
        }
    }

    /**
     * בדיקה אם ניתן להפעיל משבצת שאלה/הפתעה:
     *  - התא נחשף
     *  - הוא Question או Surprise
     *  - הוא עוד לא הופעל (USED)
     */
    public boolean canActivateSpecial(int row, int col) {
        Cell cell = getCell(row, col);
        return cell.isRevealed()
                && !cell.isPowerUsed()
                && (cell.getType() == CellType.QUESTION
                    || cell.getType() == CellType.SURPRISE);
    }

    /** סימון התא כ־USED אחרי שהופעלה השאלה/הפתעה */
    public void markSpecialUsed(int row, int col) {
        Cell cell = getCell(row, col);
        cell.setPowerUsed(true);
    }
}
