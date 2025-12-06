package model;

import java.io.BufferedReader;
import java.io.FileInputStream; 
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * מחלקה המנהלת את מאגר השאלות (טוענת מקובץ CSV).
 */
public class QuestionBank {

    private List<Question> questions;
    private Random random;
    
    // שם קובץ השאלות (חייב להיות בתיקייה הראשית של הפרויקט)
    private static final String CSV_FILE_PATH = "questions.csv"; 

    public QuestionBank() {
        this.questions = new ArrayList<>();
        this.random = new Random();
        loadQuestionsFromCSV(); // טעינת השאלות בעת האתחול
    }

    /**
     * קריאת קובץ ה-CSV וניתוח הנתונים.
     */
    private void loadQuestionsFromCSV() {
        String line = "";
        String splitBy = ",";

        // שימוש ב-UTF-8 כדי לתמוך בעברית ושפות אחרות
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {
            
            br.readLine(); // דילוג על השורה הראשונה (כותרות)

            while ((line = br.readLine()) != null) {
                // דילוג על שורות ריקות
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(splitBy);

                // בדיקה שיש מספיק עמודות (שאלה, 4 תשובות, אינדקס נכון, קושי)
                if (data.length >= 7) {
                    try {
                        String qText = data[0].trim();
                        String[] answers = {
                            data[1].trim(), data[2].trim(), 
                            data[3].trim(), data[4].trim()
                        };
                        
                        // המרת מס' התשובה הנכונה (בקובץ 1-4) לאינדקס מערך (0-3)
                        int correctIdx = Integer.parseInt(data[5].trim()) - 1; 
                        
                        // המרת מס' הקושי (1,2,3) ל-Enum של Difficulty
                        int diffNum = Integer.parseInt(data[6].trim());
                        Difficulty level;
                        if (diffNum == 3) level = Difficulty.HARD;
                        else if (diffNum == 2) level = Difficulty.MEDIUM;
                        else level = Difficulty.EASY;

                        // הוספת השאלה לרשימה
                        questions.add(new Question(qText, answers, correctIdx, level));
                        
                    } catch (Exception e) {
                        System.err.println("שגיאה בקריאת שורה: " + line);
                    }
                }
            }
            // הודעה ללוג על הצלחת הטעינה
            System.out.println("Systems: Loaded " + questions.size() + " questions successfully from CSV.");

        } catch (IOException e) {
            System.err.println("שגיאה: לא ניתן למצוא את הקובץ 'questions.csv' בתיקייה הראשית.");
        }
    }

    /**
     * החזרת שאלה אקראית מהמאגר.
     */
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(random.nextInt(questions.size()));
    }
}