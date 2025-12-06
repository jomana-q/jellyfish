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

    // Singleton – מופע יחיד לכל המשחק
    private static final QuestionBank INSTANCE = new QuestionBank();

    public static QuestionBank getInstance() {
        return INSTANCE;
    }

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
        String line;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {

            // --- קריאת שורת כותרות ---
            String header = br.readLine(); // ID,Question,Difficulty,A,B,C,D,Correct Answer
            if (header == null) {
                System.err.println("Systems: CSV file is empty.");
                return;
            }

            // זיהוי המפריד הנכון: פסיק / נקודה-פסיק / טאב
            String delimiterRegex;
            if (header.contains(",")) {
                delimiterRegex = ",";        // CSV "רגיל"
            } else if (header.contains(";")) {
                delimiterRegex = ";";        // לפעמים משתמשים ב־;
            } else {
                delimiterRegex = "\t";       // קובץ טאב כמו אצלך מאקסל
            }

            int row = 0;

            while ((line = br.readLine()) != null) {
                row++;

                if (line.trim().isEmpty()) {
                    continue; // שורה ריקה
                }

                // split לפי המפריד שמצאנו
                String[] data = line.split(delimiterRegex, -1);

                // אנחנו מצפים ל־8 עמודות:
                // 0: ID
                // 1: Question
                // 2: Difficulty (1/2/3)
                // 3: A
                // 4: B
                // 5: C
                // 6: D
                // 7: Correct Answer (A/B/C/D)
                if (data.length < 8) {
                    System.err.println("Systems: bad CSV row (expected 8 columns, got "
                            + data.length + "): " + line);
                    continue;
                }

                try {
                    String qText = data[1].trim();

                    String[] answers = {
                            data[3].trim(), // A
                            data[4].trim(), // B
                            data[5].trim(), // C
                            data[6].trim()  // D
                    };

                    // Difficulty: 1=EASY, 2=MEDIUM, 3=HARD
                    int diffNum = Integer.parseInt(data[2].trim());
                    Difficulty level;
                    if (diffNum == 1) {
                        level = Difficulty.EASY;
                    } else if (diffNum == 2) {
                        level = Difficulty.MEDIUM;
                    } else {
                        level = Difficulty.HARD;
                    }

                    // Correct Answer: אות A/B/C/D → אינדקס 0–3
                    int correctIdx = letterToIndex(data[7].trim());
                    if (correctIdx < 0 || correctIdx > 3) {
                        System.err.println("Systems: bad correct answer letter in row " + row
                                + ": " + data[7]);
                        continue;
                    }

                    questions.add(new Question(qText, answers, correctIdx, level));

                } catch (Exception e) {
                    System.err.println("Systems: error parsing row " + row + ": " + line);
                    e.printStackTrace();
                }
            }

            System.out.println("Systems: Loaded " + questions.size() + " questions successfully from CSV.");

        } catch (IOException e) {
            System.err.println("שגיאה: לא ניתן לקרוא את הקובץ '" + CSV_FILE_PATH + "'.");
            e.printStackTrace();
        }
    }

    /** המרת אות (A/B/C/D) לאינדקס 0–3. */
    private int letterToIndex(String letter) {
        if (letter == null) return -1;
        letter = letter.trim().toUpperCase();

        return switch (letter) {
            case "A" -> 0;
            case "B" -> 1;
            case "C" -> 2;
            case "D" -> 3;
            default -> -1;
        };
    }

    /** החזרת שאלה אקראית מהמאגר. */
    public Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return null;
        }
        return questions.get(random.nextInt(questions.size()));
    }
}
