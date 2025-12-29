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

    private static final int OVERLAY_SECONDS = 3;

    // Timer / Pause fields
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

    // Timer API
    public void startGameTimer() {
        gameStartMillis = System.currentTimeMillis();
        paused = false;
        pausedAtMillis = 0L;
        totalPausedMillis = 0L;
    }

    public void togglePause() {
        if (gameStartMillis == 0L) return;

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

    public boolean isPaused() { return paused; }

    /** elapsed time EXCLUDING pauses */
    public long getElapsedActiveMillis() {
        if (gameStartMillis == 0L) return 0L;

        long now = paused ? pausedAtMillis : System.currentTimeMillis();
        long elapsed = (now - gameStartMillis) - totalPausedMillis;
        return Math.max(0L, elapsed);
    }

    // Click handling
    public void handleLeftClick(boolean firstBoard, int row, int col) {
        if (paused) return;

        Board board = firstBoard ? board1 : board2;
        Cell cell = board.getCell(row, col);

        if (cell.isRevealed() && !board.canActivateSpecial(row, col)) return;

        // activate question/surprise
        if (board.canActivateSpecial(row, col)) {

            if (!session.canPayForPower()) {
                view.showResultOverlay(
                        MinesweeperGUI.OverlayType.INFO,
                        "NOT ENOUGH SCORE",
                        "Need " + session.getDifficulty().getPowerCost() + " pts to activate",
                        OVERLAY_SECONDS
                );
                view.refreshView();
                return;
            }

            // Question bank empty?
            if (cell.getType() == CellType.QUESTION) {
                Question test = QuestionBank.getInstance().getRandomQuestion();
                if (test == null) {
                    view.showResultOverlay(
                            MinesweeperGUI.OverlayType.INFO,
                            "NO QUESTIONS",
                            "questions.csv missing or empty",
                            OVERLAY_SECONDS
                    );
                    view.refreshView();
                    return;
                }
            }

            // Pay activation cost first
            int scoreBeforePay = session.getScore();
            int livesBeforePay = session.getLives();
            session.payForPower();
            int scoreAfterPay = session.getScore();
            int livesAfterPay = session.getLives();

            int payDeltaScore = scoreAfterPay - scoreBeforePay;
            int payDeltaLives = livesAfterPay - livesBeforePay; // usually 0

            // SURPRISE
            if (cell.getType() == CellType.SURPRISE) {
                boolean good = Math.random() < 0.5;

                session.applySurpriseOutcome(good);
                board.markSpecialUsed(row, col);

                int outcomeDeltaScore = session.getScore() - scoreAfterPay;
                int outcomeDeltaLives = session.getLives() - livesAfterPay;

                view.refreshView();
                view.showResultOverlay(
                        good ? MinesweeperGUI.OverlayType.GOOD : MinesweeperGUI.OverlayType.BAD,
                        good ? "GOOD SURPRISE!" : "BAD SURPRISE!",
                        formatPowerSubtitle(payDeltaScore, payDeltaLives, outcomeDeltaScore, outcomeDeltaLives, null),
                        OVERLAY_SECONDS
                );
                endTurn();
                return;
            }

            // QUESTION
            if (cell.getType() == CellType.QUESTION) {
                Question q = QuestionBank.getInstance().getRandomQuestion();
                boolean correct = QuestionDialog.showQuestionDialog(view, q);

                QuestionBonusEffect bonus = session.applyQuestionResult(q.getLevel(), correct);

                if (bonus == QuestionBonusEffect.REVEAL_MINE) {
                    board.revealRandomMine();
                } else if (bonus == QuestionBonusEffect.REVEAL_3X3) {
                    board.revealRandom3x3(session);
                }

                board.markSpecialUsed(row, col);

                int outcomeDeltaScore = session.getScore() - scoreAfterPay;
                int outcomeDeltaLives = session.getLives() - livesAfterPay;

                view.refreshView();
                view.showResultOverlay(
                        correct ? MinesweeperGUI.OverlayType.GOOD : MinesweeperGUI.OverlayType.BAD,
                        correct ? "CORRECT ANSWER!" : "WRONG ANSWER!",
                        formatPowerSubtitle(payDeltaScore, payDeltaLives, outcomeDeltaScore, outcomeDeltaLives, bonus),
                        OVERLAY_SECONDS
                );
                endTurn();
                return;
            }

            // fallback
            view.showResultOverlay(
                    MinesweeperGUI.OverlayType.INFO,
                    "CANNOT ACTIVATE",
                    "Try another cell",
                    OVERLAY_SECONDS
            );
            view.refreshView();
            endTurn();
            return;
        }

        // normal open
        board.openCell(row, col, session);
        endTurn();
    }

    public void handleRightClick(boolean firstBoard, int row, int col) {
        if (paused) return;

        Board board = firstBoard ? board1 : board2;
        Cell cell = board.getCell(row, col);

        if (cell.isRevealed()) return;
        if (cell.isPowerUsed()) return;

        board.toggleFlag(row, col, session);
        view.refreshView();
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
        view.refreshView();
    }

    private String formatPowerSubtitle(int payDeltaScore, int payDeltaLives,
                                       int outcomeDeltaScore, int outcomeDeltaLives,
                                       QuestionBonusEffect bonus) {

        StringBuilder sb = new StringBuilder();

        // Activation (cost)
        if (payDeltaScore != 0 || payDeltaLives != 0) {
            sb.append("Activation: ")
              .append(formatDelta(payDeltaScore, payDeltaLives));
        }

        // Outcome
        if (sb.length() > 0) sb.append("  |  ");
        sb.append("Outcome: ")
          .append(formatDelta(outcomeDeltaScore, outcomeDeltaLives));

        // Bonus (optional)
        if (bonus != null && bonus != QuestionBonusEffect.NONE) {
            sb.append("  |  Bonus: ").append(shortBonusName(bonus));
        }

        return sb.toString();
    }

    private String formatDelta(int deltaScore, int deltaLives) {
        return signed(deltaScore) + " pts, " + signedLives(deltaLives);
    }

    private String signed(int x) {
        return (x > 0) ? "+" + x : String.valueOf(x);
    }

    private String signedLives(int deltaLives) {
        if (deltaLives == 1) return "+1 life";
        if (deltaLives == -1) return "-1 life";
        if (deltaLives > 1) return "+" + deltaLives + " lives";
        if (deltaLives < -1) return deltaLives + " lives";
        return "0 lives";
    }

    private String shortBonusName(QuestionBonusEffect b) {
        return switch (b) {
            case REVEAL_MINE -> "Reveal Mine";
            case REVEAL_3X3 -> "Reveal 3x3";
            default -> b.name();
        };
    }
}
