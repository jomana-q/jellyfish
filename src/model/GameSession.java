package model;

public class GameSession {

    private final Difficulty difficulty;

    private int score;        // pts – משותף לשני השחקנים
    private int lives;        // hearts – משותף
    private final int maxLives;

    public GameSession(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.lives = difficulty.getInitialLives();  // מתחילים לפי רמת הקושי
        this.maxLives = 10;                         // המקסימום תמיד 10 
        this.score = 0;
    }

    // --- getters לצורך GUI/Controller ---

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    // --- ניקוד ---

    public void updateScore(int delta) {
        this.score += delta;
    }

    // --- לבבות ---

    public void decreaseLives() {
        changeLives(-1);
    }

    public void increaseLives() {
        changeLives(+1);
    }

    /** שינוי כללי של לבבות (חיובי/שלילי) */
    private void changeLives(int delta) {
        lives += delta;

        // אם עברנו את המקסימום – כל לב עודף נהפך לנקודות
        if (lives > maxLives) {
            int extra = lives - maxLives;
            lives = maxLives;
            score += extra * difficulty.getPowerCost(); // לב מעל המקסימום = מחיר תחנה
        }
        // ירידה ל-0 או פחות תטופל ע"י ה-Controller כסיום משחק
    }

    /** החזרת true אם נגמרו החיים (תנאי סיום אחד) */
    public boolean isOutOfLives() {
        return lives <= 0;
    }

    // --- הפעלת שאלה/הפתעה – בסיס לאיטרציות הבאות ---

    /** בדיקה האם יש מספיק נקודות לשלם על תחנה (שאלה/הפתעה) */
    public boolean canPayForPower() {
        return score >= difficulty.getPowerCost();
    }

    /** תשלום נקודות להפעלת משבצת שאלה/הפתעה */
    public void payForPower() {
        if (!canPayForPower()) {
            throw new IllegalStateException("Not enough points to activate power");
        }
        updateScore(-difficulty.getPowerCost());
    }

    /**
     * החלת אפקט כללי (שימושי במיוחד לשאלות לפי הטבלה):
     * livesDelta – כמה לבבות לשנות (יכול להיות שלילי/חיובי)
     * scoreDelta – כמה נקודות לשנות
     */
    public void applyEffect(int livesDelta, int scoreDelta) {
        changeLives(livesDelta);
        updateScore(scoreDelta);
    }

    /**
     * הפעלת משבצת הפתעה לפי הטבלה של רמות קושי.
     * good=true → הפתעה טובה, good=false → רעה.
     */
    public void applySurprise(boolean good) {
        payForPower(); // קודם משלמים על ההפעלה

        int pts = difficulty.getSurprisePoints();
        if (good) {
            changeLives(+1);
            updateScore(+pts);
        } else {
            changeLives(-1);
            updateScore(-pts);
        }
    }
}
