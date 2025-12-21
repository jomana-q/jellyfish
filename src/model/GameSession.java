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
    }

    /** החזרת true אם נגמרו החיים (תנאי סיום אחד) */
    public boolean isOutOfLives() {
        return lives <= 0;
    }

    // --- הפעלת שאלה/הפתעה ---

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
     * הפעלת משבצת הפתעה (הגרסה הישנה) – נשארת אם תרצי להשתמש בה,
     * אבל עכשיו בבקר אנחנו עושים "שלב־שלב", אז לא נשתמש בה שם.
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

    /**
     * ⭐ חדש: תוצאת הפתעה בלבד (בלי לשלם).
     * נדרש כדי שב-Control נוכל:
     * 1) payForPower() → refresh
     * 2) applySurpriseOutcome() → refresh
     */
    public void applySurpriseOutcome(boolean good) {
        int pts = difficulty.getSurprisePoints();
        if (good) {
            changeLives(+1);
            updateScore(+pts);
        } else {
            changeLives(-1);
            updateScore(-pts);
        }
    }

    public int applyFlagRules(CellType type) {
        if (type == CellType.MINE) {
            return +1; // פגיעה טובה
        } else {
            return -3; // החטאה
        }
    }

    /**
     * החלת תוצאות של שאלה לפי הטבלה (כולל OR 50/50).
     * שימי לב: כאן אין תשלום powerCost – התשלום יבוצע בבקר לפני השאלה
     * כדי להציג שינוי ניקוד "בזמן אמת".
     */
    public QuestionBonusEffect applyQuestionResult(QuestionLevel questionLevel, boolean correct) {
        int livesDelta = 0;
        int scoreDelta = 0;
        QuestionBonusEffect bonus = QuestionBonusEffect.NONE;

        boolean coinFlip = Math.random() < 0.5;

        switch (difficulty) {
            case EASY -> {
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) { scoreDelta = +3; livesDelta = +1; }
                        else { scoreDelta = coinFlip ? -3 : 0; }
                    }
                    case MEDIUM -> {
                        if (correct) { scoreDelta = +6; bonus = QuestionBonusEffect.REVEAL_MINE; }
                        else { scoreDelta = coinFlip ? -6 : 0; }
                    }
                    case HARD -> {
                        if (correct) { scoreDelta = +10; bonus = QuestionBonusEffect.REVEAL_3X3; }
                        else { scoreDelta = -10; }
                    }
                    case EXPERT -> {
                        if (correct) { scoreDelta = +15; livesDelta = +2; }
                        else { scoreDelta = -15; livesDelta = -1; }
                    }
                }
            }

            case MEDIUM -> {
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) { scoreDelta = +8; livesDelta = +1; }
                        else { scoreDelta = -8; }
                    }
                    case MEDIUM -> {
                        if (correct) { scoreDelta = +10; livesDelta = +1; }
                        else {
                            if (coinFlip) { scoreDelta = -10; livesDelta = -1; }
                            else { scoreDelta = 0; livesDelta = 0; }
                        }
                    }
                    case HARD -> {
                        if (correct) { scoreDelta = +15; livesDelta = +1; }
                        else { scoreDelta = -15; livesDelta = -1; }
                    }
                    case EXPERT -> {
                        if (correct) { scoreDelta = +20; livesDelta = +2; }
                        else { scoreDelta = -20; livesDelta = coinFlip ? -1 : -2; }
                    }
                }
            }

            case HARD -> {
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) { scoreDelta = +10; livesDelta = +1; }
                        else { scoreDelta = -10; livesDelta = -1; }
                    }
                    case MEDIUM -> {
                        if (correct) { scoreDelta = +15; livesDelta = coinFlip ? +1 : +2; }
                        else { scoreDelta = -15; livesDelta = coinFlip ? -1 : -2; }
                    }
                    case HARD -> {
                        if (correct) { scoreDelta = +20; livesDelta = +2; }
                        else { scoreDelta = -20; livesDelta = -2; }
                    }
                    case EXPERT -> {
                        if (correct) { scoreDelta = +40; livesDelta = +3; }
                        else { scoreDelta = -40; livesDelta = -3; }
                    }
                }
            }
        }

        updateScore(scoreDelta);
        changeLivesNoOverflowScore(livesDelta);
        return bonus;
    }

    /** שינוי לבבות בלי המרה לנקודות (לשאלות לפי הטבלה) */
    private void changeLivesNoOverflowScore(int delta) {
        lives += delta;
        if (lives > maxLives) {
            lives = maxLives;
        }
    }

    /** סוף משחק: המרת לבבות לנקודות */
    public void convertRemainingLivesToScoreAtEnd() {
        if (lives > 0) {
            score += lives * difficulty.getPowerCost();
            lives = 0;
        }
    }
}
