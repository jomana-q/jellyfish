package model;

import java.awt.Color;

/**
 * מנהל ערכות הנושא (Singleton).
 * אחראי לשמור את הצבעים של המצב הנוכחי (Dark/Light).
 */
public class ThemeManager {

    private static ThemeManager instance;
    private boolean isDarkMode = true; // ברירת מחדל: מצב כהה

    // Singleton
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    // הגדרת מצב כהה או בהיר
    public void setDarkMode(boolean dark) {
        this.isDarkMode = dark;
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    // --- צבעים דינמיים ---

    // צבע רקע עליון (גרדיאנט התחלה)
    public Color getBackgroundColor1() {
        return isDarkMode ? new Color(10, 25, 40) : new Color(240, 248, 255); // כחול עמוק או לבן-תכלת
    }

    // צבע רקע תחתון (גרדיאנט סוף)
    public Color getBackgroundColor2() {
        return isDarkMode ? new Color(25, 50, 60) : new Color(200, 220, 255); // כחול-ירוק או תכלת בהיר
    }

    // צבע הטקסט הראשי
    public Color getTextColor() {
        return isDarkMode ? Color.WHITE : new Color(30, 30, 30); // לבן או שחור כהה
    }
}