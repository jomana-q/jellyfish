package controller;

import model.*;
import view.MinesweeperGUI;
import view.QuestionDialog;

public class MinesweeperController {

    private final Board board1;
    private final Board board2;
    private final GameSession session;
    private final MinesweeperGUI view;

    private boolean player1Turn = true;

    private static final int OVERLAY_SECONDS = 5;

    // =========================
    // Timer / Pause fields
    // =========================
    private boolean paused = false;
    private long gameStartMillis = 0L;
    private long pausedAtMillis = 0L;
    private long totalPausedMillis = 0L;

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

    // =========================
    // Timer API
    // =========================
    public void startGameTimer() {
        gameStartMillis = System.currentTimeMillis();
        paused = false;
        pausedAtMillis = 0L;
        totalPausedMillis = 0L;
    }

    public void togglePause() {
        if (gameStartMillis == 0L) return; // timer not started yet

        long now = System.currentTimeMillis();
        if (!paused) {
            paused = true;
            pausedAtMillis = now;
        } else {
            paused = false;
            totalPausedMillis += (now - pausedAtMillis);
            pausedAtMillis = 0L;
        }
    }

    public boolean isPaused() {
        return paused;
    }

    /** elapsed time EXCLUDING pauses */
    public long getElapsedActiveMillis() {
        if (gameStartMillis == 0L) return 0L;

        long now = paused ? pausedAtMillis : System.currentTimeMillis();
        long elapsed = (now - gameStartMillis) - totalPausedMillis;
        return Math.max(0L, elapsed);
    }

    // =========================
    // Click handling
    // =========================
    public void handleLeftClick(boolean firstBoard, int row, int col) {
        if (paused) return;

        Board board = firstBoard ? board1 : board2;
        Cell cell = board.getCell(row, col);

        if (cell.isRevealed() && !board.canActivateSpecial(row, col)) return;

        if (board.canActivateSpecial(row, col)) {

            if (!session.canPayForPower()) {
                view.showTemporaryOverlay(
                        "NOT ENOUGH SCORE\nActivation cost: -" + session.getDifficulty().getPowerCost() + " pts",
                        OVERLAY_SECONDS
                );
                view.refreshView();
                return;
            }

            if (cell.getType() == CellType.QUESTION) {
                Question test = QuestionBank.getInstance().getRandomQuestion();
                if (test == null) {
                    view.showTemporaryOverlay(
                            "NO QUESTIONS LOADED\nMake sure questions.csv exists",
                            OVERLAY_SECONDS
                    );
                    view.refreshView();
                    return;
                }
            }

            int scoreBeforeAll = session.getScore();
            int livesBeforeAll = session.getLives();

            int cost = session.getDifficulty().getPowerCost();

            session.payForPower();
            int scoreAfterPay = session.getScore();
            int livesAfterPay = session.getLives();

            String overlayMsg;

            if (cell.getType() == CellType.SURPRISE) {
                boolean good = Math.random() < 0.5;

                session.applySurpriseOutcome(good);
                board.markSpecialUsed(row, col);

                int outcomeScore = session.getScore() - scoreAfterPay;
                int outcomeLives = session.getLives() - livesAfterPay;
                int totalScoreDelta = session.getScore() - scoreBeforeAll;
                int totalLivesDelta = session.getLives() - livesBeforeAll;

                overlayMsg =
                        (good ? "GOOD SURPRISE!" : "BAD SURPRISE!") +
                        "\nActivation Surprise cost: -" + cost + " pts" +
                        "\nSurprise effect: " + signed(outcomeScore) + " pts, Lives " + signed(outcomeLives) +
                        "\nTotal score change: " + signed(totalScoreDelta) +
                        "\nTotal lives change: " + signed(totalLivesDelta);

            } else if (cell.getType() == CellType.QUESTION) {

                Question q = QuestionBank.getInstance().getRandomQuestion();
                boolean correct = QuestionDialog.showQuestionDialog(view, q);

                QuestionBonusEffect bonus = session.applyQuestionResult(q.getLevel(), correct);

                if (bonus == QuestionBonusEffect.REVEAL_MINE) {
                    board.revealRandomMine();
                } else if (bonus == QuestionBonusEffect.REVEAL_3X3) {
                    board.revealRandom3x3(session);
                }

                board.markSpecialUsed(row, col);

                int outcomeScore = session.getScore() - scoreAfterPay;
                int outcomeLives = session.getLives() - livesAfterPay;
                int totalScoreDelta = session.getScore() - scoreBeforeAll;
                int totalLivesDelta = session.getLives() - livesBeforeAll;

                overlayMsg =
                        (correct ? "CORRECT ANSWER!" : "WRONG ANSWER!") +
                        "\nActivation Question cost: -" + cost + " pts" +
                        "\nAnswer effect: " + signed(outcomeScore) + " pts, Lives " + signed(outcomeLives) +
                        "\nTotal score change: " + signed(totalScoreDelta) +
                        "\nTotal lives change: " + signed(totalLivesDelta);

                if (bonus != QuestionBonusEffect.NONE) {
                    overlayMsg += "\nBONUS: " + bonus;
                }

            } else {
                overlayMsg = "CANNOT ACTIVATE THIS CELL";
            }

            view.refreshView();
            view.showTemporaryOverlay(overlayMsg, OVERLAY_SECONDS);
            endTurn();
            return;
        }

        board.openCell(row, col, session);
        endTurn();
    }

    public void handleRightClick(boolean firstBoard, int row, int col) {
        if (paused) return;

        Board board = firstBoard ? board1 : board2;
        Cell cell = board.getCell(row, col);

        if (cell.isRevealed()) return;
        if (cell.isPowerUsed()) return;

        int scoreBefore = session.getScore();
        int livesBefore = session.getLives();

        board.toggleFlag(row, col, session);

        int totalScoreDelta = session.getScore() - scoreBefore;
        int totalLivesDelta = session.getLives() - livesBefore;

        view.refreshView();
        view.showTemporaryOverlay(
                "FLAG TOGGLED" +
                        "\nTotal score change: " + signed(totalScoreDelta) +
                        "\nTotal lives change: " + signed(totalLivesDelta),
                OVERLAY_SECONDS
        );

        endTurn();
    }

    private void endTurn() {
        view.refreshView();

        if (board1.allMinesRevealed() || board2.allMinesRevealed()) {
            view.showGameOver(true);
            return;
        }

        if (session.isOutOfLives()) {
            view.showGameOver(false);
            return;
        }

        player1Turn = !player1Turn;
        view.updateTurnHighlight();
    }

    private String signed(int x) {
        if (x > 0) return "+" + x;
        return String.valueOf(x);
    }
}
