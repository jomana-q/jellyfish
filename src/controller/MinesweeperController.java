package controller;

import model.Board;
import model.Cell;
import model.CellType;
import model.GameSession;
import view.MinesweeperGUI;

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

    // --- getters שה-View יכול להשתמש בהם ---

    public Board getBoard1() {
        return board1;
    }

    public Board getBoard2() {
        return board2;
    }

    public GameSession getSession() {
        return session;
    }

    public boolean isPlayer1Turn() {
        return player1Turn;
    }

    /**
     * טיפול בלחיצה שמאלית – פתיחת תא / הפעלת שאלה/הפתעה.
     * @param firstBoard true אם הלוח הראשון, false אם השני
     */
    public void handleLeftClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;

        // קליק שני על תא מיוחד (שאלה / הפתעה)
        if (board.canActivateSpecial(row, col)) {
            Cell cell = board.getCell(row, col);

            if (cell.getType() == CellType.SURPRISE) {
                // הפתעה: 50/50 טוב/רע
                boolean good = Math.random() < 0.5;
                session.applySurprise(good);
                board.markSpecialUsed(row, col);

            } else if (cell.getType() == CellType.QUESTION) {
                // כאן בעתיד נחבר למערכת שאלות אמיתית
                JOptionPane.showMessageDialog(
                        view,
                        "Question activation – to be implemented.\n(קליק שני מפעיל את השאלה)",
                        "Question",
                        JOptionPane.INFORMATION_MESSAGE
                );
                board.markSpecialUsed(row, col);
            }

        } else {
            // קליק ראשון – פתיחת תא רגילה (כולל מוקש/מספר/ריק/שאלה/הפתעה)
            board.openCell(row, col, session);
        }

        endTurn();
    }

    /**
     * טיפול בלחיצה ימנית – סימון/ביטול דגל.
     */
    public void handleRightClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;
        board.toggleFlag(row, col, session);
        endTurn();
    }

    // --- סוף תור / בדיקת סיום משחק ---

    private void endTurn() {
        // קודם מעדכנים את ה-GUI
        view.refreshView();

        // בדיקת סיום משחק לפי חיים
        if (session.isOutOfLives()) {
            view.showGameOver();
            return;
        }

        // כאן בהמשך אפשר להוסיף תנאי: אם כל המוקשים נחשפו אצל שחקן מסוים

        // מעבר תור
        player1Turn = !player1Turn;
        view.updateTurnHighlight();
    }
}
