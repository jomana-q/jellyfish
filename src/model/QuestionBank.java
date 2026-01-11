package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * מחלקה המנהלת את מאגר השאלות (טוענת מקובץ CSV/TSV).
 */
public class QuestionBank {

    private static final QuestionBank INSTANCE = new QuestionBank();
    public static QuestionBank getInstance() { return INSTANCE; }

    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();

    // אם הקובץ יושב ליד ההרצה (project root). אם תרצי classpath - תגידי ואשנה.
    private static final String CSV_FILE_PATH = "questions.csv";

    private QuestionBank() {
        loadQuestionsFromCSV();
    }

    private void loadQuestionsFromCSV() {
        File f = new File(CSV_FILE_PATH);

        System.out.println("System: Loading questions from: " + f.getAbsolutePath());
        System.out.println("System: Exists=" + f.exists() + " size=" + (f.exists() ? f.length() : -1));

        if (!f.exists()) {
            System.err.println("Systems: questions.csv not found.");
            return;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null || header.trim().isEmpty()) {
                System.err.println("Systems: CSV file is empty.");
                return;
            }

            // remove UTF-8 BOM if exists
            header = header.replace("\uFEFF", "");

            char delimiter = detectDelimiter(header);

            int rowNum = 1; // header row = 1
            String line;

            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                // parse with quoting support
                String[] data = parseLine(line, delimiter);

                // Expect 8 columns:
                // 0 ID, 1 Question, 2 Difficulty(1-4), 3 A,4 B,5 C,6 D,7 Correct(A-D)
                if (data.length < 8) {
                    System.err.println("Systems: bad row " + rowNum +
                            " (expected 8 columns, got " + data.length + "). Skipping. Line: " + line);
                    continue;
                }

                try {
                    String qText = safeTrim(data[1]);
                    int levelNum = Integer.parseInt(safeTrim(data[2]));

                    String a = safeTrim(data[3]);
                    String b = safeTrim(data[4]);
                    String c = safeTrim(data[5]);
                    String d = safeTrim(data[6]);

                    // basic validation
                    if (qText.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
                        System.err.println("Systems: missing question/answers in row " + rowNum +
                                ". Skipping. Line: " + line);
                        continue;
                    }

                    QuestionLevel level = switch (levelNum) {
                        case 1 -> QuestionLevel.EASY;
                        case 2 -> QuestionLevel.MEDIUM;
                        case 3 -> QuestionLevel.HARD;
                        case 4 -> QuestionLevel.EXPERT;
                        default -> {
                            System.err.println("Systems: unknown question level " + levelNum +
                                    " in row " + rowNum + ". Using MEDIUM.");
                            yield QuestionLevel.MEDIUM;
                        }
                    };

                    int correctIdx = letterToIndex(safeTrim(data[7]));
                    if (correctIdx < 0 || correctIdx > 3) {
                        System.err.println("Systems: bad correct answer letter in row " + rowNum +
                                ": " + data[7] + ". Skipping.");
                        continue;
                    }

                    String[] answers = { a, b, c, d };
                    questions.add(new Question(qText, answers, correctIdx, level) {
                        @Override
                        protected void applyEffect(boolean correct, GameSession session) {

                        }
                    });

                } catch (NumberFormatException nfe) {
                    System.err.println("Systems: error parsing row " + rowNum + " (difficulty not a number). Skipping.");
                    System.err.println("Systems: Line: " + line);
                } catch (Exception e) {
                    System.err.println("Systems: error parsing row " + rowNum + ". Skipping.");
                    System.err.println("Systems: Line: " + line);
                }
            }

            System.out.println("System: Questions loaded successfully. Total: " + questions.size());

        } catch (IOException e) {
            System.err.println("שגיאה: לא ניתן לקרוא את הקובץ '" + CSV_FILE_PATH + "'.");
            e.printStackTrace();
        }
    }

    public void reloadQuestions() {
        questions.clear();
        loadQuestionsFromCSV();
        System.out.println("System: Questions reloaded successfully. Total: " + questions.size());
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(random.nextInt(questions.size()));
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

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

    private char detectDelimiter(String header) {
        // Prefer tab if exists (Excel often saves TSV)
        if (header.contains("\t")) return '\t';
        if (header.contains(","))  return ',';
        if (header.contains(";"))  return ';';
        // fallback
        return ',';
    }

    /**
     * Parses a delimited line with support for quotes:
     * - fields can be wrapped in "..."
     * - inside quotes, delimiter doesn't split
     * - escaped quotes "" become "
     */
    private String[] parseLine(String line, char delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // escaped quote
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
