package model;

/**
 * אפקט מיוחד כתוצאה משאלה, מעבר לניקוד ולחיים.
 */
public enum QuestionBonusEffect {
    NONE,          // בלי אפקט מיוחד
    REVEAL_MINE,   // לחשוף משבצת מוקש אחת אוטומטית
    REVEAL_3X3     // להציג / לחשוף בלוק 3x3 משבצות
}
