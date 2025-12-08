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

    /** חשיפת תא ע"י השחקן. */
    public void openCell(int row, int col, GameSession session) {
        if (!isInBounds(row, col)) {
            return;
        }
        revealRecursive(row, col, session);
    }

    // פונקציה פנימית רקורסיבית (לחשיפה + קסקדה)
    private void revealRecursive(int row, int col, GameSession session) {
        if (!isInBounds(row, col)) {
            return;
        }

        Cell cell = grid[row][col];

        // לא חושפים שוב תא שכבר נפתח או מסומן בדגל
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        // מסמנים כחשוף
        cell.setRevealed(true);

        // מוקש → מפסידים חיים, בלי נקודות, בלי קסקדה
        if (cell.getType() == CellType.MINE) {
            session.decreaseLives();
            return;
        }

        // כל תא שאינו מוקש → נקודה אחת
        session.updateScore(+1);

        // נמשיך קסקדה **רק** מתאים שאין לידם מוקשים:
        // EMPTY / QUESTION / SURPRISE (שנבחרו ממשבצות ריקות)
        boolean zeroCell =
                cell.getType() == CellType.EMPTY ||
                cell.getType() == CellType.QUESTION ||
                cell.getType() == CellType.SURPRISE;

        // אם זה מספר (NUMBER) – נפתח אבל לא ממשיך קסקדה
        if (!zeroCell) {
            return;
        }

        // קסקדה: עוברים על כל השכנים, כל עוד הם לא מוקש
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {

                if (dr == 0 && dc == 0) continue;

                int nr = row + dr;
                int nc = col + dc;

                if (!isInBounds(nr, nc)) continue;

                Cell neighbor = grid[nr][nc];

                // מדלגים על תאים שכבר נפתחו או מסומנים בדגל
                if (neighbor.isRevealed() || neighbor.isFlagged()) {
                    continue;
                }

                // לא מריצים קסקדה אל מוקש
                if (neighbor.getType() == CellType.MINE) {
                    continue;
                }

                // ניגש שוב רקורסיבית – הוא יקבל נקודה,
                // ואם גם הוא "ריק" (EMPTY/QUESTION/SURPRISE) הוא ימשיך את הקסקדה
                revealRecursive(nr, nc, session);
            }
        }
    }

    /**
     * סימון / ביטול סימון בדגל.
     *  - Mine מסומן בדגל → pts+1  + חשיפת המוקש
     *  - מספר / ריק / שאלה / הפתעה מסומנים בדגל → pts-3
     *  - ביטול סימון: בלי שינוי ניקוד.
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
     *  - הוא עוד לא הופעל (powerUsed=false)
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

    // פונקציה אופציונלית – חישוב ניקוד לדגל (אם תרצי להשתמש בה במקום toggleFlag)
    public FlagResult flagCell(int row, int col) {
        Cell cell = grid[row][col];

        if (cell.isRevealed()) {
            return new FlagResult(false, "Cell already revealed", 0);
        }

        if (cell.isFlagged()) {
            return new FlagResult(false, "Cell already flagged", 0);
        }

        cell.setFlagged(true);

        int points;
        switch (cell.getType()) {
            case MINE:
                points = +1;
                break;
            case NUMBER:
            case EMPTY:
            case QUESTION:
            case SURPRISE:
                points = -3;
                break;
            default:
                points = 0;
        }

        return new FlagResult(true, "Flagged", points);
    }

    // פונקציה אופציונלית – הפעלת שאלה/הפתעה עם ניקוד ולבבות
    public ActivationResult activateCell(int row, int col, Difficulty difficulty) {
        Cell cell = grid[row][col];

        if (!cell.isRevealed()) {
            return new ActivationResult(false, "Cell must be revealed first", 0, 0);
        }

        if (cell.isPowerUsed()) {
            return new ActivationResult(false, "Cell already used", 0, 0);
        }

        if (cell.getType() != CellType.QUESTION && cell.getType() != CellType.SURPRISE) {
            return new ActivationResult(false, "Cell cannot be activated", 0, 0);
        }

        // עלות הפעלה לפי רמת קושי
        int cost = switch (difficulty) {
            case EASY -> 5;
            case MEDIUM -> 8;
            case HARD -> 12;
        };

        int points = -cost;
        int hearts = 0;

        // אם זו הפתעה → 50/50 טוב/רע
        if (cell.getType() == CellType.SURPRISE) {
            boolean good = Math.random() < 0.5;

            if (good) {
                points += switch (difficulty) {
                    case EASY -> +8;
                    case MEDIUM -> +12;
                    case HARD -> +16;
                };
                hearts = +1;
            } else {
                points -= switch (difficulty) {
                    case EASY -> 8;
                    case MEDIUM -> 12;
                    case HARD -> 16;
                };
                hearts = -1;
            }
        }

        // סימון התא כמשומש
        cell.setPowerUsed(true);

        return new ActivationResult(true, "Activated", points, hearts);
    }
    
    public boolean revealRandomMine() {
        List<int[]> hiddenMines = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                if (cell.getType() == CellType.MINE && !cell.isRevealed()) {
                    hiddenMines.add(new int[]{r, c});
                }
            }
        }

        if (hiddenMines.isEmpty()) return false;

        int[] pos = hiddenMines.get(random.nextInt(hiddenMines.size()));
        grid[pos[0]][pos[1]].setRevealed(true);

        return true;
    }
    
    public void revealRandom3x3(GameSession session) {
        int r = random.nextInt(rows);
        int c = random.nextInt(cols);

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = r + dr;
                int nc = c + dc;

                if (!isInBounds(nr, nc)) continue;

                Cell cell = grid[nr][nc];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    // השימוש ב-openCell מבטיח שקבלת נקודות תהיה תקינה
                    openCell(nr, nc, session);
                }
            }
        }
    }
    
    /**
     * בדיקה אם כל המוקשים על הלוח כבר נחשפו.
     * במשחק שלך, כשמסמנים מוקש בדגל את כבר מסמנת אותו כ-revealed,
     * לכן מספיק לבדוק שכל התאים מסוג MINE הם revealed.
     */
    public boolean allMinesRevealed() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                if (cell.getType() == CellType.MINE && !cell.isRevealed()) {
                    return false; // עדיין יש מוקש שלא גילו
                }
            }
        }
        return true; // כל המוקשים גלו
    }
    
    /**
     * סוף משחק: לחשוף את כל התאים על הלוח,
     * בלי לעדכן ניקוד / לבבות (רק setRevealed).
     */
    public void revealAllCells() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                cell.setRevealed(true);
            }
        }
    }


}
