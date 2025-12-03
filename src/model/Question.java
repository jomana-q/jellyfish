package model;

/**
 * מחלקה המייצגת שאלה בודדת במשחק.
 */
public class Question {

    private String questionText;    // תוכן השאלה
    private String[] answers;       // מערך של 4 תשובות אפשריות
    private int correctAnswerIndex; // האינדקס של התשובה הנכונה (0-3)
    private Difficulty level;       // רמת הקושי של השאלה

    /**
     * בנאי (Constructor) ליצירת שאלה חדשה.
     */
    public Question(String questionText, String[] answers, int correctAnswerIndex, Difficulty level) {
        this.questionText = questionText;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
        this.level = level;
    }

    // --- Getters ---

    public String getQuestionText() {
        return questionText;
    }

    public String[] getAnswers() {
        return answers;
    }

    public Difficulty getLevel() {
        return level;
    }
    
    /**
     * בדיקה האם התשובה שנבחרה היא הנכונה.
     * @param index - האינדקס שהמשתמש בחר
     * @return true אם התשובה נכונה, אחרת false
     */
    public boolean isCorrect(int index) {
        return index == correctAnswerIndex;
    }
    
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}