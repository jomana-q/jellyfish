package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GameHistory implements GameObserver {

    private List<GameHistoryEntry> entries;
    private static final String HISTORY_FILE = "history.csv";

    public GameHistory() {
        this.entries = new ArrayList<>();
        loadHistory();
    }

    @Override
    public void onGameStateChanged(GameSession session) {


    }

    // חדש: כולל durationSeconds
    public void addEntry(String name, int score, String difficulty, String result, int durationSeconds) {
        entries.add(new GameHistoryEntry(name, score, difficulty, result, durationSeconds));
        saveHistory();
    }

    // ישן (לתאימות)
    public void addEntry(String name, int score, String difficulty, String result) {
        addEntry(name, score, difficulty, result, 0);
    }

    public void addEntry(String name, int score) {
        addEntry(name, score, "", "", 0);
    }

    public List<GameHistoryEntry> getTopScores() {
        entries.sort(Comparator.comparingInt(GameHistoryEntry::getScore).reversed());
        if (entries.size() > 10) return entries.subList(0, 10);
        return entries;
    }

    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {

            // ⭐ כותרת עמודות
            writer.write("Players Name,Score,Game Difficulty,Result,Duration (sec),Date");
            writer.newLine();

            for (GameHistoryEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }


    private void loadHistory() {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            // ⭐ דילוג על שורת הכותרת
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                try {
                    if (parts.length == 3) {
                        // ישן: name,score,date
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        String date = parts[2];
                        entries.add(new GameHistoryEntry(name, score, "", "", 0, date));

                    } else if (parts.length == 5) {
                        // חדש-ישן: name,score,difficulty,result,date
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        String difficulty = parts[2];
                        String result = parts[3];
                        String date = parts[4];
                        entries.add(new GameHistoryEntry(name, score, difficulty, result, 0, date));

                    } else if (parts.length >= 6) {
                        // חדש: name,score,difficulty,result,durationSeconds,date
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        String difficulty = parts[2];
                        String result = parts[3];
                        int duration = Integer.parseInt(parts[4]);
                        String date = parts[5];
                        entries.add(new GameHistoryEntry(name, score, difficulty, result, duration, date));
                    }

                } catch (NumberFormatException e) {
                    System.err.println("שגיאה בטעינת רשומה: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("שגיאה בטעינת ההיסטוריה: " + e.getMessage());
        }
    }

}
