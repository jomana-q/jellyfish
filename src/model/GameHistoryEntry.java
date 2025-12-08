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
    private String difficulty; // NEW
    private String result;     // NEW
    private String date;

    // בנאי חדש למשחק חדש שנשמר עכשיו
    public GameHistoryEntry(String playerName, int score,
                            String difficulty, String result) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.result = result;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        this.date = dtf.format(LocalDateTime.now());
    }

    // בנאי לטעינה מקובץ (עם כל השדות)
    public GameHistoryEntry(String playerName, int score,
                            String difficulty, String result,
                            String date) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.result = result;
        this.date = date;
    }

    // --- Getters ---
    public String getPlayerName() { return playerName; }
    public int getScore()         { return score; }
    public String getDifficulty() { return difficulty; }
    public String getResult()     { return result; }
    public String getDate()       { return date; }

    @Override
    public String toString() {
        // פורמט CSV מלא
        return playerName + "," + score + "," +
               difficulty + "," + result + "," + date;
    }
}
