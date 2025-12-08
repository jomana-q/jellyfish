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

    public int applyFlagRules(CellType type) {
        if (type == CellType.MINE) {
            return +1; // פגיעה טובה
        } else {
            return -3; // החטאה
        }
    }
    /**
     * החלת תוצאות של שאלה לפי:
     *  - רמת המשחק (this.difficulty)
     *  - רמת השאלה (questionLevel)
     *  - האם התשובה נכונה (correct)
     *
     * מימוש מדויק לפי הטבלה במסמך.
     * בכל מקום שיש OR – נעשה הטלת מטבע 50%-50%.
     *
     * הפונקציה:
     *  - מעדכנת ניקוד ולבבות.
     *  - מחזירה QuestionBonusEffect כדי שה-Controller ידע האם
     *    צריך לחשוף מוקש אוטומטית / להציג 3x3.
     */
    public QuestionBonusEffect applyQuestionResult(QuestionLevel questionLevel, boolean correct) {
        int livesDelta = 0;
        int scoreDelta = 0;
        QuestionBonusEffect bonus = QuestionBonusEffect.NONE;

        boolean coinFlip = Math.random() < 0.5; // ל-OR 50%

        switch (difficulty) {
            case EASY -> {
                // רמת משחק: קל
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) {
                            // קל + שאלה קלה: (+3pts) & (+1❤)
                            scoreDelta = +3;
                            livesDelta = +1;
                        } else {
                            // קל + שאלה קלה: (-3pts) OR nothing
                            if (coinFlip) {
                                scoreDelta = -3;
                            } else {
                                scoreDelta = 0;
                            }
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            // קל + שאלה בינונית: חשיפת משבצת מוקש & (+6pts)
                            scoreDelta = +6;
                            livesDelta = 0;
                            bonus = QuestionBonusEffect.REVEAL_MINE;
                            // הערה: לא מקבלים נקודות על חשיפת המוקש עצמה
                        } else {
                            // קל + שאלה בינונית: (-6pts) OR nothing
                            if (coinFlip) {
                                scoreDelta = -6;
                            } else {
                                scoreDelta = 0;
                            }
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            // קל + שאלה קשה: הצגת 3X3 משבצות & (+10pts)
                            scoreDelta = +10;
                            livesDelta = 0;
                            bonus = QuestionBonusEffect.REVEAL_3X3;
                        } else {
                            // קל + שאלה קשה: (-10pts)
                            scoreDelta = -10;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            // קל + שאלת מומחה: (+15pts) & (+2❤)
                            scoreDelta = +15;
                            livesDelta = +2;
                        } else {
                            // קל + שאלת מומחה: (-15pts) & (-1❤)
                            scoreDelta = -15;
                            livesDelta = -1;
                        }
                    }
                }
            }

            case MEDIUM -> {
                // רמת משחק: בינוני
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) {
                            // בינוני + שאלה קלה: (+8pts) & (+1❤)
                            scoreDelta = +8;
                            livesDelta = +1;
                        } else {
                            // בינוני + שאלה קלה: (-8pts)
                            scoreDelta = -8;
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            // בינוני + שאלה בינונית: (+10pts) & (+1❤)
                            scoreDelta = +10;
                            livesDelta = +1;
                        } else {
                            // בינוני + שאלה בינונית:
                            // ((-10pts) & (-1❤)) OR nothing
                            if (coinFlip) {
                                scoreDelta = -10;
                                livesDelta = -1;
                            } else {
                                scoreDelta = 0;
                                livesDelta = 0;
                            }
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            // בינוני + שאלה קשה: (+15pts) & (+1❤)
                            scoreDelta = +15;
                            livesDelta = +1;
                        } else {
                            // בינוני + שאלה קשה: (-15pts) & (-1❤)
                            scoreDelta = -15;
                            livesDelta = -1;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            // בינוני + שאלת מומחה: (+20pts) & (+2❤)
                            scoreDelta = +20;
                            livesDelta = +2;
                        } else {
                            // בינוני + שאלת מומחה:
                            // ((-20pts) & (-1❤)) OR ((-20pts) & (-2❤))
                            scoreDelta = -20;
                            if (coinFlip) {
                                livesDelta = -1;
                            } else {
                                livesDelta = -2;
                            }
                        }
                    }
                }
            }

            case HARD -> {
                // רמת משחק: קשה
                switch (questionLevel) {
                    case EASY -> {
                        if (correct) {
                            // קשה + שאלה קלה: (+10pts) & (+1❤)
                            scoreDelta = +10;
                            livesDelta = +1;
                        } else {
                            // קשה + שאלה קלה: (-10pts) & (-1❤)
                            scoreDelta = -10;
                            livesDelta = -1;
                        }
                    }
                    case MEDIUM -> {
                        if (correct) {
                            // קשה + שאלה בינונית:
                            // ((+15pts) & (+1❤)) OR ((+15pts) & (+2❤))
                            scoreDelta = +15;
                            if (coinFlip) {
                                livesDelta = +1;
                            } else {
                                livesDelta = +2;
                            }
                        } else {
                            // קשה + שאלה בינונית:
                            // ((-15pts) & (-1❤)) OR ((-15pts) & (-2❤))
                            scoreDelta = -15;
                            if (coinFlip) {
                                livesDelta = -1;
                            } else {
                                livesDelta = -2;
                            }
                        }
                    }
                    case HARD -> {
                        if (correct) {
                            // קשה + שאלה קשה: (+20pts) & (+2❤)
                            scoreDelta = +20;
                            livesDelta = +2;
                        } else {
                            // קשה + שאלה קשה: (-20pts) & (-2❤)
                            scoreDelta = -20;
                            livesDelta = -2;
                        }
                    }
                    case EXPERT -> {
                        if (correct) {
                            // קשה + שאלת מומחה: (+40pts) & (+3❤)
                            scoreDelta = +40;
                            livesDelta = +3;
                        } else {
                            // קשה + שאלת מומחה: (-40pts) & (-3❤)
                            scoreDelta = -40;
                            livesDelta = -3;
                        }
                    }
                }
            }
        }

     // קודם מעדכנים ניקוד לפי הטבלה
        updateScore(scoreDelta);

        // אחר כך מעדכנים חיים *בלי* להמיר לב עודף לנקודות
        changeLivesNoOverflowScore(livesDelta);

        return bonus;

    }
    
    /**
     * שינוי לבבות *בלי* להמיר לבבות עודפים לנקודות.
     * משמש במיוחד לתוצאות של שאלות, כדי לכבד את הטבלה בדיוק.
     */
    private void changeLivesNoOverflowScore(int delta) {
        lives += delta;
        if (lives > maxLives) {
            lives = maxLives;  // פשוט חותכים ל-10, בלי לתת נקודות
        }
    }
    
    /**
     * סוף משחק: המרה של כל הלבבות שנותרו לנקודות.
     * משתמשים ב-powerCost כ"מחיר" לב אחד.
     * אחרי ההמרה, מספר הלבבות מתאפס ל-0.
     */
    public void convertRemainingLivesToScoreAtEnd() {
        if (lives > 0) {
            score += lives * difficulty.getPowerCost();
            lives = 0;
        }
    }


}
