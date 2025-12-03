package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * מחלקה המנהלת את טבלת השיאים (High Scores).
 * אחראית על שמירה וטעינה של הנתונים לקובץ 'history.csv'.
 */
public class GameHistory {

    private List<GameHistoryEntry> entries;
    // שם הקובץ שבו יישמרו התוצאות (נוצר אוטומטית בתיקיית הפרויקט)
    private static final String HISTORY_FILE = "history.csv";

    public GameHistory() {
        this.entries = new ArrayList<>();
        loadHistory(); // טעינת היסטוריה ברגע שהמשחק עולה
    }

    /**
     * הוספת תוצאה חדשה ושמירה מיידית לקובץ.
     */
    public void addEntry(String name, int score) {
        entries.add(new GameHistoryEntry(name, score));
        saveHistory(); // עדכון הקובץ
    }

    /**
     * החזרת 10 התוצאות הטובות ביותר (ממוין מהגבוה לנמוך).
     */
    public List<GameHistoryEntry> getTopScores() {
        // מיון הרשימה לפי הניקוד (בסדר יורד)
        entries.sort(Comparator.comparingInt(GameHistoryEntry::getScore).reversed());
        
        // החזרת 10 הראשונים בלבד (או פחות אם אין מספיק)
        if (entries.size() > 10) {
            return entries.subList(0, 10);
        }
        return entries;
    }

    /**
     * שמירת כל הרשימה לקובץ CSV.
     * אם הקובץ לא קיים, הפונקציה תיצור אותו.
     */
    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (GameHistoryEntry entry : entries) {
                writer.write(entry.toString()); // כתיבת השורה (שם,ניקוד,תאריך)
                writer.newLine(); // ירידת שורה
            }
        } catch (IOException e) {
            System.err.println("שגיאה בשמירת ההיסטוריה: " + e.getMessage());
        }
    }

    /**
     * טעינת הנתונים מהקובץ לרשימה בזיכרון.
     */
    private void loadHistory() {
        File file = new File(HISTORY_FILE);
        
        // אם הקובץ עדיין לא קיים (משחק ראשון), אין מה לטעון
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                // בדיקה שהשורה תקינה (יש בה 3 חלקים: שם, ניקוד, תאריך)
                if (parts.length == 3) {
                    String name = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    String date = parts[2];
                    entries.add(new GameHistoryEntry(name, score, date));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("שגיאה בטעינת ההיסטוריה: " + e.getMessage());
        }
    }
}