package model;

// 1. המחלקה רגילה (לא abstract)
public class Question {

    // 2. שדה חדש ל-ID
    private int id; 
    
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex;
    private QuestionLevel level;

    // 3. בנאי (Constructor) מעודכן שמקבל ID
    public Question(int id, String questionText, String[] answers, int correctAnswerIndex, QuestionLevel level) {
        this.id = id;
        this.questionText = questionText;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
        this.level = level;
    }

    // 4. Getters & Setters ל-ID (חובה בשביל QuestionBank)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // --- שאר ה-Getters ---
    public String getQuestionText() { return questionText; }
    public String[] getAnswers() { return answers; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public QuestionLevel getLevel() { return level; }

    // בדיקה האם התשובה נכונה
    public boolean isCorrect(int index) {
        return index == correctAnswerIndex;
    }

    // פונקציה שהייתה בעבר abstract, כעת היא ריקה (כדי למנוע שגיאות)
    protected void applyEffect(boolean correct, GameSession session) {
        // אין לוגיקה כרגע, הטיפול מתבצע ב-Controller
    }
    
    // אם היה לך שימוש בפונקציה הזו בעבר, נשאיר אותה כדי לא לשבור קוד אחר
    public boolean processAnswer(int chosenIndex, GameSession session) {
        boolean correct = isCorrect(chosenIndex);
        applyEffect(correct, session);
        return correct;
    }
}