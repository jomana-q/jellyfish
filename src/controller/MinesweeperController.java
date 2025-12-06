package controller;

import model.Board;
import model.Cell;
import model.CellType;
import model.GameSession;
import view.MinesweeperGUI;

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

    public void handleLeftClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;

        if (board.canActivateSpecial(row, col)) {
            Cell cell = board.getCell(row, col);

            if (cell.getType() == CellType.SURPRISE) {
                boolean good = Math.random() < 0.5;
                session.applySurprise(good);
                board.markSpecialUsed(row, col);

            } else if (cell.getType() == CellType.QUESTION) {
                // ה-View מטפל בחלון השאלה
                view.showQuestionPlaceholder(row, col);
                board.markSpecialUsed(row, col);
            }

        } else {
            board.openCell(row, col, session);
        }

        endTurn();
    }

    public void handleRightClick(boolean firstBoard, int row, int col) {
        Board board = firstBoard ? board1 : board2;
        board.toggleFlag(row, col, session);
        endTurn();
    }

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
