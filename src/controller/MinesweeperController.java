package controller;

import model.*;
import view.MinesweeperGUI;
import view.QuestionDialog;

import javax.swing.Timer;
import java.awt.Point;
import java.util.List;

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

        // להתחיל מוזיקת משחק
        SoundManager.getInstance().playGameLoop();
    }

    public void togglePause() {
        if (gameStartMillis == 0L) return;

        long now = System.currentTimeMillis();
        SoundManager sm = SoundManager.getInstance();

        if (!paused) {
            paused = true;
            pausedAtMillis = now;
            sm.stopBgm();
        } else {
            paused = false;
            totalPausedMillis += (now - pausedAtMillis);
            pausedAtMillis = 0L;
            sm.playGameLoop();
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
                boolean isQuestionTile = (cell.getType() == CellType.QUESTION);
                int cost = session.getDifficulty().getPowerCost();
                int current = session.getScore();

                view.showNotEnoughPointsOverlay(isQuestionTile, cost, current);
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

            // ===== SURPRISE =====
            if (cell.getType() == CellType.SURPRISE) {
                boolean good = Math.random() < 0.5;

                session.applySurpriseOutcome(good);
                board.markSpecialUsed(row, col);

                int outcomeDeltaScore = session.getScore() - scoreAfterPay;
                int outcomeDeltaLives = session.getLives() - livesAfterPay;

                SoundManager sm = SoundManager.getInstance();
                int SOUND_DELAY_MS = 350;
                Timer soundTimer = new Timer(SOUND_DELAY_MS, e -> {
                    if (good) sm.playGoodSurpriseThenResumeGame();
                    else sm.playBadSurpriseThenResumeGame();
                    ((Timer) e.getSource()).stop();
                });
                soundTimer.setRepeats(false);
                soundTimer.start();

                view.playGiftCenterAndShowOverlay(
                        good ? MinesweeperGUI.OverlayType.GOOD : MinesweeperGUI.OverlayType.BAD,
                        good ? "GOOD SURPRISE!" : "BAD SURPRISE!",
                        formatPowerSubtitle(payDeltaScore, payDeltaLives, outcomeDeltaScore, outcomeDeltaLives, null),
                        OVERLAY_SECONDS,
                        this::endTurn
                );

                return;
            }

            // ===== QUESTION =====
            if (cell.getType() == CellType.QUESTION) {

                SoundManager sm = SoundManager.getInstance();

                sm.playQuestionLoop();

                Question q = QuestionBank.getInstance().getRandomQuestion();
                boolean correct = QuestionDialog.showQuestionDialog(view, q);

                if (correct) sm.playCorrectFor5SecondsThenResumeGame();
                else sm.playWrongThenResumeGame();

                QuestionBonusEffect bonus = session.applyQuestionResult(q.getLevel(), correct);

                if (bonus == QuestionBonusEffect.REVEAL_MINE) {
                    board.revealRandomMine();
                } else if (bonus == QuestionBonusEffect.REVEAL_3X3) {
                    board.revealBest3x3(session);
                }

                board.markSpecialUsed(row, col);

                int outcomeDeltaScore = session.getScore() - scoreAfterPay;
                int outcomeDeltaLives = session.getLives() - livesAfterPay;

                view.refreshView();
                view.showQuestionResultOverlay(
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

        // ===== פתיחה רגילה – עכשיו עם אופציה לאנימציית קסקייד =====
        startCascadeOpen(board, row, col);
    }

    /**
     * פתיחת תא עם אנימציית קסקייד:
     * - אם אין קסקייד אמיתי (תא אחד בלבד) → openCell רגיל.
     * - אם יש קסקייד – פתיחה תא-תא עם Timer מהיר.
     */
    private void startCascadeOpen(Board board, int row, int col) {

        int minesBefore = countRevealedMines(board1) + countRevealedMines(board2);
        int livesBefore = session.getLives();
        int scoreBefore = session.getScore();

        List<Point> cascade = board.computeCascadeOrder(row, col);

        if (cascade == null || cascade.size() <= 1) {
            board.openCell(row, col, session);

            int minesAfter = countRevealedMines(board1) + countRevealedMines(board2);
            int livesAfter = session.getLives();
            int scoreAfter = session.getScore();

            showMineToastIfChanged(minesBefore, minesAfter, livesBefore, livesAfter, scoreBefore, scoreAfter);
            endTurn();
            return;
        }

        final int[] index = {0};
        int delayMs = 40; // אנימציה מהירה

        Timer t = new Timer(delayMs, e -> {

            if (index[0] >= cascade.size()) {
                ((Timer) e.getSource()).stop();

                int minesAfter = countRevealedMines(board1) + countRevealedMines(board2);
                int livesAfter = session.getLives();
                int scoreAfter = session.getScore();

                showMineToastIfChanged(minesBefore, minesAfter, livesBefore, livesAfter, scoreBefore, scoreAfter);
                endTurn();
                return;
            }

            Point p = cascade.get(index[0]++);
            board.revealSingleCell(p.x, p.y, session);
            view.refreshView();
        });

        t.setRepeats(true);
        t.start();
    }

    public void handleRightClick(boolean firstBoard, int row, int col) {
        if (paused) return;

        Board board = firstBoard ? board1 : board2;
        Cell cell = board.getCell(row, col);

        if (cell.isRevealed()) return;
        if (cell.isPowerUsed()) return;

        boolean wasFlagged = cell.isFlagged();

        int minesBefore = countRevealedMines(board1) + countRevealedMines(board2);
        int livesBefore = session.getLives();
        int scoreBefore = session.getScore();

        board.toggleFlag(row, col, session);

        int minesAfter = countRevealedMines(board1) + countRevealedMines(board2);
        int livesAfter = session.getLives();
        int scoreAfter = session.getScore();

        showMineToastIfChanged(minesBefore, minesAfter, livesBefore, livesAfter, scoreBefore, scoreAfter);

        boolean revealedMineNow = (minesAfter > minesBefore);
        boolean isUnflag = wasFlagged && !cell.isFlagged() && !cell.isRevealed();

        if (!isUnflag && !revealedMineNow) {
            int d = scoreAfter - scoreBefore;
            if (d < 0) view.showToast("Wrong flag ❌ (" + d + " score)", 1600);
            else view.showToast("Flag placed 🚩", 1200);
        } else if (isUnflag) {
            view.showToast("Flag removed 🚫  Keep going!", 1200);
        }

        view.refreshView();

        if (isUnflag) return;
        if (revealedMineNow) return;

        endTurn();
    }

    private void endTurn() {
        view.refreshView();

        if (board1.allMinesRevealed() || board2.allMinesRevealed()) {
            SoundManager.getInstance().stopBgm();
            view.showGameOver(true);
            return;
        }
        if (session.isOutOfLives()) {
            SoundManager.getInstance().stopBgm();
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

        if (payDeltaScore != 0 || payDeltaLives != 0) {
            sb.append("Activation: ")
              .append(formatDelta(payDeltaScore, payDeltaLives));
        }

        if (sb.length() > 0) sb.append("  |  ");
        sb.append("Outcome: ")
          .append(formatDelta(outcomeDeltaScore, outcomeDeltaLives));

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

    private int countRevealedMines(Board b) {
        int count = 0;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                Cell cell = b.getCell(r, c);
                if (cell.getType() == CellType.MINE && cell.isRevealed()) count++;
            }
        }
        return count;
    }

    private void showMineToastIfChanged(int minesBefore, int minesAfter, int livesBefore, int livesAfter, int scoreBefore, int scoreAfter) {
        int dMines  = minesAfter - minesBefore;
        int dLives  = livesAfter - livesBefore;
        int dScore  = scoreAfter - scoreBefore;

        if (dMines <= 0) return; // no new mine revealed

        if (dLives < 0) {
            view.showToast("Oops! You hit a mine 💥  (-1 life)", 1700);
        } else if (dScore > 0) {
            view.showToast("Nice! Mine flagged 💎 (+" + dScore + " score) Keep going!", 1800);
        } else {
            view.showToast("Mine revealed 💥", 1400);
        }
    }
}
