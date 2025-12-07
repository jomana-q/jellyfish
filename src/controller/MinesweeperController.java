package controller;

import model.Board;
import model.Cell;
import model.CellType;
import model.GameSession;
import model.Question;
import model.QuestionBank;
import model.QuestionBonusEffect;
import view.MinesweeperGUI;
import view.QuestionDialog;

import javax.swing.*;

/**
 * בקר המשחק – אחראי על:
 *  - טיפול בלחיצות (שמאל/ימין) על הלוחות
 *  - מעבר תורות
 *  - בדיקת תנאי סיום
 *  - קריאה ל-View לעדכן תצוגה / להציג Game Over
 */
public class MinesweeperController {

    private final Board board1;
    private final Board board2;
    private final GameSession session;
    private final MinesweeperGUI view;

    /** true = תור שחקן 1 (לוח ראשון), false = שחקן 2 (לוח שני) */
    private boolean player1Turn = true;

    public MinesweeperController(Board board1,
                                 Board board2,
                                 GameSession session,
                                 MinesweeperGUI view) {
        this.board1 = board1;
        this.board2 = board2;
        this.session = session;
        this.view = view;
    }

    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public GameSession getSession() { return session; }
    public boolean isPlayer1Turn() { return player1Turn; }

    /**
     * לחיצה שמאלית – פתיחת תא / הפעלת שאלה / הפתעה.
     */
    public void handleLeftClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;

        // אם זה תא מיוחד שניתן להפעיל (שאלה/הפתעה אחרי שנפתח)
        if (board.canActivateSpecial(row, col)) {
            Cell cell = board.getCell(row, col);

            if (cell.getType() == CellType.SURPRISE) {
                // הפתעה: 50% טובה / 50% רעה
                boolean good = Math.random() < 0.5;
                session.applySurprise(good);
                board.markSpecialUsed(row, col);

            } else if (cell.getType() == CellType.QUESTION) {
                // שאלה: לוקחים שאלה אקראית מה-CSV
                Question q = QuestionBank.getInstance().getRandomQuestion();

                if (q == null) {
                    JOptionPane.showMessageDialog(
                            view,
                            "לא נטענו שאלות מהקובץ.\nבדקי שקובץ questions.csv נמצא בתיקייה הראשית.",
                            "שגיאת שאלות",
                            JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    // --- שמירה של מצב לפני השאלה ---
                    int beforeScore = session.getScore();
                    int beforeLives = session.getLives();
                    
             /*    // דיבוג: להדפיס לקונסול מה רמת המשחק ומה רמת השאלה
                    System.out.println("DEBUG Question: game difficulty = " + session.getDifficulty()
                            + ", question level = " + q.getLevel());*/

                    // מציגים חלון שאלה – מחזיר true אם ענו נכון
                    boolean correct = QuestionDialog.showQuestionDialog(view, q);

                 // מעדכן לבבות/ניקוד לפי רמת השאלה והתשובה
                    QuestionBonusEffect bonus = session.applyQuestionResult(q.getLevel(), correct);
                    
                    if (bonus == QuestionBonusEffect.REVEAL_MINE) {
                        board.revealRandomMine();
                    }

                    if (bonus == QuestionBonusEffect.REVEAL_3X3) {
                        board.revealRandom3x3(session);
                    }
                    
                    view.refreshView();

                    // --- חישוב השינויים לצורך הודעה ---
                    int afterScore = session.getScore();
                    int afterLives = session.getLives();

                    int deltaScore = afterScore - beforeScore;
                    int deltaLives = afterLives - beforeLives;

                    StringBuilder msg = new StringBuilder();
                    msg.append(correct ? "תשובה נכונה! 🎉" : "תשובה שגויה. 😕");

                    if (deltaScore != 0) {
                        msg.append("\nניקוד: ");
                        msg.append(deltaScore > 0 ? "+" : "");
                        msg.append(deltaScore);
                    }
                    if (deltaLives != 0) {
                        msg.append("\nחיים: ");
                        msg.append(deltaLives > 0 ? "+" : "");
                        msg.append(deltaLives);
                    }

                    JOptionPane.showMessageDialog(
                            view,
                            msg.toString(),
                            "תוצאה",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                // אחרי ההפעלה – אי אפשר להשתמש בתא שוב
                board.markSpecialUsed(row, col);
            }

        } else {
            // תא רגיל – פתיחה רגילה
            board.openCell(row, col, session);
        }

        endTurn();
    }


    /**
     * לחיצה ימנית – סימון/ביטול דגל.
     */
    public void handleRightClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;
        board.toggleFlag(row, col, session);
        endTurn();
    }

    /**
     * סיום תור – רענון מסך, בדיקת חיים, החלפת שחקן.
     */
    private void endTurn() {
        view.refreshView();

        if (session.isOutOfLives()) {
            view.showGameOver();
            return;
        }

        player1Turn = !player1Turn;
        view.updateTurnHighlight();
    }
}
