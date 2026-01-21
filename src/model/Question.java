package model;

public abstract class Question {

    private String questionText;      // תוכן השאלה
    private String[] answers;         // 4 תשובות אפשריות
    private int correctAnswerIndex;   // האינדקס של התשובה הנכונה (0-3)
    private QuestionLevel level;      // רמת הקושי של השאלה (EASY/MEDIUM/HARD/EXPERT)

    public Question(String questionText,
                    String[] answers,
                    int correctAnswerIndex,
                    QuestionLevel level) {
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

    /** מאפשר ל־UI לדעת איזו תשובה נכונה */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public QuestionLevel getLevel() {
        return level;
    }

    /**
     * בדיקה האם התשובה שנבחרה היא הנכונה.
     */
    public boolean isCorrect(int index) {
        return index == correctAnswerIndex;
    }

    public final boolean processAnswer(int chosenIndex, GameSession session) {
        boolean correct = isCorrect(chosenIndex);
        applyEffect(correct, session);
        return correct;
    }

    protected abstract void applyEffect(boolean correct, GameSession session);
}