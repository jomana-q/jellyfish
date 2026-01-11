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
//                grid[r][c] = new Cell(CellType.EMPTY);
                grid[r][c] = CellFactory.createCell(CellType.EMPTY);
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
        if (!isInBounds(row, col)) return;

        // שחקן: כן ניקוד, כן קסקדה
        revealRecursive(row, col, session, true, true, true);
    }

    /**
     * חשיפה פנימית: אפשר לשלוט אם נותנים נקודות ואם עושים קסקדה.
     */
    private void revealRecursive(int row, int col, GameSession session,
            boolean awardPoints,
            boolean allowCascade,
            boolean playerClick) {

        if (!isInBounds(row, col)) return;

        Cell cell = grid[row][col];

        // לא חושפים שוב תא שכבר נפתח או מסומן בדגל
        if (cell.isRevealed() || cell.isFlagged()) return;

        // מסמנים כחשוף
        cell.setRevealed(true);

        if (cell.getType() == CellType.MINE) {
            if (playerClick) {
                session.decreaseLives();
            }
            return;
        }


        // כל תא שאינו מוקש → נקודה אחת רק אם זו חשיפה "רגילה"
        if (awardPoints) {
            session.updateScore(+1);
        }

        // קסקדה רק אם מותר
        if (!allowCascade) return;

        // נמשיך קסקדה **רק** מתאים שאין לידם מוקשים:
        boolean zeroCell =
                cell.getType() == CellType.EMPTY ||
                cell.getType() == CellType.QUESTION ||
                cell.getType() == CellType.SURPRISE;

        if (!zeroCell) return;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int nr = row + dr;
                int nc = col + dc;

                if (!isInBounds(nr, nc)) continue;

                Cell neighbor = grid[nr][nc];

                if (neighbor.isRevealed() || neighbor.isFlagged()) continue;
                if (neighbor.getType() == CellType.MINE) continue;

                // בקסקדה עדיין נותנים נקודות (זה חלק מחשיפת שחקן)
                revealRecursive(nr, nc, session, true, true, false);
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

        // קודם כל: אם יש דגל -> מבטלים (גם אם revealed)
        if (cell.isFlagged()) {
            cell.setFlagged(false);
            return;
        }

        // עכשיו: אם כבר נחשף ואין דגל - לא עושים כלום
        if (cell.isRevealed()) {
            return;
        }

        // סימון חדש
        cell.setFlagged(true);

        switch (cell.getType()) {
            case MINE:
                session.updateScore(+1);
                cell.setRevealed(true); // חושף מוקש
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
                if (cell.getType() == CellType.MINE && !cell.isRevealed() && !cell.isFlagged()) {
                    hiddenMines.add(new int[]{r, c});
                }
            }
        }

        if (hiddenMines.isEmpty()) return false;

        int[] pos = hiddenMines.get(random.nextInt(hiddenMines.size()));
        grid[pos[0]][pos[1]].setRevealed(true); // אפקט אוטומטי: בלי ניקוד ובלי חיים
        return true;
    }

    
    public void revealBest3x3(GameSession session) {

        // אם הלוח קטן מ-3x3 אין מה לעשות
        if (rows < 3 || cols < 3) return;

        int bestR = -1, bestC = -1;
        int bestScore = -1;

        // מרכזים חוקיים בלבד (לא בקצוות) כדי שתמיד יהיה 3x3 מלא
        for (int r = 1; r <= rows - 2; r++) {
            for (int c = 1; c <= cols - 2; c++) {

                int score = 0;

                // סופרים כמה מתוך ה-9 עדיין לא פתוחים ולא בדגל
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        Cell cell = grid[r + dr][c + dc];
                        if (!cell.isRevealed() && !cell.isFlagged()) {
                            score++;
                        }
                    }
                }

                // בוחרים את החלון הכי טוב
                if (score > bestScore) {
                    bestScore = score;
                    bestR = r;
                    bestC = c;
                } else if (score == bestScore && score > 0) {
                    // שבירת תיקו רנדומלית קטנה כדי שלא תמיד יבחר אותו איזור
                    if (random.nextBoolean()) {
                        bestR = r;
                        bestC = c;
                    }
                }
            }
        }

        // אין אף 3x3 שיש בו משהו חדש לפתוח
        if (bestScore <= 0) return;

        // פותחים בדיוק את ה-3x3 שנבחר: בלי ניקוד ובלי קסקדה ובלי ירידת חיים
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = bestR + dr;
                int nc = bestC + dc;

                Cell cell = grid[nr][nc];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    revealRecursive(nr, nc, session, false, false, false);
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
