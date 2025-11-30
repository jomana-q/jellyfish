package controller;

import model.Board;
import model.GameSession;
import view.MinesweeperGUI;

public class MinesweeperController {

    private final Board board;
    private final GameSession session;
    private final MinesweeperGUI view;

    /**
     * בנאי (Constructor) - מקבל את המודל והתצוגה ומחבר ביניהם.
     */
    public MinesweeperController(Board board, GameSession session, MinesweeperGUI view) {
        this.board = board;
        this.session = session;
        this.view = view;
        
        // חשוב: מעבירים את הבקר (this) לתצוגה כדי שהיא תוכל לקרוא לפונקציות שלנו
        this.view.setController(this);
    }

    /**
     * טיפול בלחיצה שמאלית של העכבר (פתיחת תא).
     */
    public void handleLeftClick(int row, int col) {
        board.openCell(row, col, session);
        
        if (session.isOutOfLives()) {
            view.refreshView();
            view.showGameOver();
            return;
        }

        view.refreshView();
    }

    /**
     * טיפול בלחיצה ימנית של העכבר (דגל).
     */
    public void handleRightClick(int row, int col) {
        board.toggleFlag(row, col, session);
        view.refreshView();
    }
}