package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionBank {

    private static final QuestionBank INSTANCE = new QuestionBank();
    
    // רשימת השאלות בזיכרון
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();

    // נתיב הקובץ - בתיקייה הראשית של הפרויקט
    private static final String CSV_FILE_PATH = "questions.csv";

    public static QuestionBank getInstance() { return INSTANCE; }

    private QuestionBank() {
        loadQuestionsFromCSV();
    }

    public List<Question> getQuestions() { return questions; }

    // ==========================================
    // 1. טעינת שאלות מהקובץ (מעודכן ל-ID)
    // ==========================================
    public void loadQuestionsFromCSV() {
        questions.clear();
        File file = new File(CSV_FILE_PATH);
        
        if (!file.exists()) {
            System.err.println("System: questions.csv not found (will be created on save).");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = br.readLine(); // דילוג על הכותרת
            char delimiter = detectDelimiter(header); // זיהוי סוג המפריד

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = parseLine(line, delimiter);

                // אנו מצפים ל-8 עמודות: ID, Question, Difficulty, A, B, C, D, Correct
                if (data.length < 8) continue;

                try {
                    // קריאת ה-ID (עמודה 0)
                    int id = 0;
                    try { 
                        id = Integer.parseInt(safeTrim(data[0])); 
                    } catch (Exception e) { 
                        // אם אין ID תקין, נשים 0 זמנית
                    }

                    String qText = safeTrim(data[1]);
                    int levelNum = Integer.parseInt(safeTrim(data[2]));
                    QuestionLevel level = getLevelFromInt(levelNum);

                    String a = safeTrim(data[3]);
                    String b = safeTrim(data[4]);
                    String c = safeTrim(data[5]);
                    String d = safeTrim(data[6]);
                    
                    int correctIdx = letterToIndex(safeTrim(data[7]));

                    String[] answers = { a, b, c, d };
                    
                    // יצירת השאלה (חובה ש-Question.java יהיה מעודכן עם ID)
                    questions.add(new Question(id, qText, answers, correctIdx, level));

                } catch (Exception e) {
                    System.err.println("שגיאה בטעינת שורה: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 2. הוספת שאלה ושמירה מידית
    // ==========================================
    public void addQuestion(String text, String[] answers, int correctIndex, QuestionLevel level) {
        // ID זמני (0), הפונקציה save תסדר אותו מחדש
        questions.add(new Question(0, text, answers, correctIndex, level));
        saveQuestionsToCSV();
    }

    // ==========================================
    // 3. מחיקת שאלה ושמירה מידית
    // ==========================================
    public void deleteQuestion(int indexInList) {
        if (indexInList >= 0 && indexInList < questions.size()) {
            questions.remove(indexInList);
            saveQuestionsToCSV();
        }
    }

    // ==========================================
    // 4. שמירה וסידור מחדש של ה-ID (חשוב!)
    // ==========================================
    public void saveQuestionsToCSV() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(CSV_FILE_PATH), StandardCharsets.UTF_8))) {

            // כתיבת כותרת
            writer.write("ID,Question,Difficulty,A,B,C,D,Correct");
            writer.newLine();

            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                
                // === חישוב ID חדש לפי הסדר (1, 2, 3...) ===
                int newId = i + 1;
                q.setId(newId); 

                String line = String.format("%d,\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",%s",
                        newId,
                        escapeCsv(q.getQuestionText()),
                        levelToInt(q.getLevel()),
                        escapeCsv(q.getAnswers()[0]),
                        escapeCsv(q.getAnswers()[1]),
                        escapeCsv(q.getAnswers()[2]),
                        escapeCsv(q.getAnswers()[3]),
                        indexToLetter(q.getCorrectAnswerIndex())
                );

                writer.write(line);
                writer.newLine();
            }
            System.out.println("Questions saved and re-indexed successfully!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 5. פונקציות למשחק
    // ==========================================
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(random.nextInt(questions.size()));
    }

    public void reloadQuestions() {
        loadQuestionsFromCSV();
    }

    // ==========================================
    // 6. פונקציות עזר (Helpers)
    // ==========================================

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    private int levelToInt(QuestionLevel l) {
        if (l == null) return 2;
        return switch (l) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
            case EXPERT -> 4;
        };
    }
    
    private QuestionLevel getLevelFromInt(int n) {
        return switch (n) {
            case 1 -> QuestionLevel.EASY;
            case 2 -> QuestionLevel.MEDIUM;
            case 3 -> QuestionLevel.HARD;
            case 4 -> QuestionLevel.EXPERT;
            default -> QuestionLevel.MEDIUM;
        };
    }

    private int letterToIndex(String letter) {
        if (letter == null) return -1;
        return switch (letter.trim().toUpperCase()) {
            case "A" -> 0; case "B" -> 1; case "C" -> 2; case "D" -> 3; default -> -1;
        };
    }

    private String indexToLetter(int index) {
        return switch (index) {
            case 0 -> "A"; case 1 -> "B"; case 2 -> "C"; case 3 -> "D"; default -> "A";
        };
    }
    
    private char detectDelimiter(String header) {
        if (header == null) return ',';
        if (header.contains("\t")) return '\t';
        if (header.contains(";")) return ';';
        return ',';
    }

    /**
     * פונקציה לפירוק שורת CSV (כולל תמיכה במרכאות)
     */
    private String[] parseLine(String line, char delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); 
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}