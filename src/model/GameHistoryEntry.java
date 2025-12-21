package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameHistoryEntry {

    private String playerName;
    private int score;
    private String difficulty;
    private String result;
    private int durationSeconds;
    private String date;

    // חדש: נשמר עכשיו
    public GameHistoryEntry(String playerName, int score, String difficulty, String result, int durationSeconds) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.result = result;
        this.durationSeconds = durationSeconds;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        this.date = dtf.format(LocalDateTime.now());
    }

    // טעינה מקובץ
    public GameHistoryEntry(String playerName, int score, String difficulty, String result, int durationSeconds, String date) {
        this.playerName = playerName;
        this.score = score;
        this.difficulty = difficulty;
        this.result = result;
        this.durationSeconds = durationSeconds;
        this.date = date;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public String getDifficulty() { return difficulty; }
    public String getResult() { return result; }
    public String getDate() { return date; }
    public int getDurationSeconds() { return durationSeconds; }

    @Override
    public String toString() {
        // פורמט חדש: name,score,difficulty,result,durationSeconds,date
        return playerName + "," + score + "," + difficulty + "," + result + "," + durationSeconds + "," + date;
    }
}
