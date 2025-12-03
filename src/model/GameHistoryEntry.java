package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * מחלקה המייצגת רשומה אחת בהיסטוריית המשחקים.
 * שומרת את שם השחקן, הניקוד והתאריך שבו המשחק הסתיים.
 */
public class GameHistoryEntry {
    
    private String playerName;
    private int score;
    private String date;

    /**
     * בנאי ליצירת רשומה חדשה בסיום משחק (התאריך נוצר אוטומטית).
     */
    public GameHistoryEntry(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        // יצירת תאריך ושעה נוכחיים בפורמט קריא
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        this.date = dtf.format(LocalDateTime.now());
    }

    /**
     * בנאי לטעינת רשומה קיימת מהקובץ.
     */
    public GameHistoryEntry(String playerName, int score, String date) {
        this.playerName = playerName;
        this.score = score;
        this.date = date;
    }

    // --- Getters ---

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public String getDate() { return date; }

    /**
     * המרה למחרוזת בפורמט CSV (מופרד בפסיקים) לשמירה בקובץ.
     */
    @Override
    public String toString() {
        return playerName + "," + score + "," + date;
    }
}