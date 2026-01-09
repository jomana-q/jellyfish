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

    // ===== Timer API =====
    public void startGameTimer() {
        gameStartMillis = System.currentTimeMillis();
        paused = false;
        pausedAtMillis = 0L;
        totalPausedMillis = 0L;

        // ✅ מתחילים מוזיקת משחק
        SoundManager.getInstance().playGameLoop();
    }

    public void togglePause() {
        if (gameStartMillis == 0L) return;

        long now = System.currentTimeMillis();
        SoundManager sm = SoundManager.getInstance();

        if (!paused) {
            paused = true;
            pausedAtMillis = now;

            // ✅ עוצרים מוזיקת רקע בזמן pause
            sm.stopBgm();
        } else {
            paused = false;
            totalPausedMillis += (now - pausedAtMillis);
            pausedAtMillis = 0L;

            // ✅ מחזירים מוזיקת משחק
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

    // ===== Click handling =====
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

                // ✅ ננגן את הסאונד *אחרי* שהמתנה נפתחת (בערך אחרי 350ms)
                SoundManager sm = SoundManager.getInstance();
                int SOUND_DELAY_MS = 350; // אותו זמן כמו ה-Timer הראשון בגיפט

                Timer soundTimer = new Timer(SOUND_DELAY_MS, e -> {
                    if (good) sm.playGoodSurpriseThenResumeGame();
                    else sm.playBadSurpriseThenResumeGame();
                    ((Timer) e.getSource()).stop();
                });
                soundTimer.setRepeats(false);
                soundTimer.start();

                // ✅ האנימציה + האוברליי, ורק בסוף – endTurn (ואז הקו הזהב של השחקן הבא)
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

                // ✅ מתחילים מוזיקת שאלה בלופ
                sm.playQuestionLoop();

                Question q = QuestionBank.getInstance().getRandomQuestion();

                // בזמן הדיאלוג מוזיקת השאלה רצה
                boolean correct = QuestionDialog.showQuestionDialog(view, q);

                // ✅ כשהדיאלוג נסגר: מפסיקים BGM שאלה ומנגנים תוצאה ואז חזרה למשחק
                if (correct) sm.playCorrectFor5SecondsThenResumeGame();
                else sm.playWrongThenResumeGame();

                QuestionBonusEffect bonus = session.applyQuestionResult(q.getLevel(), correct);

                // בונוסים (REVEAL_MINE / REVEAL_3X3) נשארים כמו אצלך
                if (bonus == QuestionBonusEffect.REVEAL_MINE) {
                    board.revealRandomMine();
                } else if (bonus == QuestionBonusEffect.REVEAL_3X3) {
                    board.revealRandom3x3(session);
                }

                board.markSpecialUsed(row, col);

                int outcomeDeltaScore = session.getScore() - scoreAfterPay;
                int outcomeDeltaLives = session.getLives() - livesAfterPay;

                view.refreshView();

                // ✅ עכשיו: קודם מציגים את התוצאה, ורק אחרי X שניות עושים endTurn
                view.showQuestionResultOverlayAndThen(
                        correct ? MinesweeperGUI.OverlayType.GOOD : MinesweeperGUI.OverlayType.BAD,
                        correct ? "CORRECT ANSWER!" : "WRONG ANSWER!",
                        formatPowerSubtitle(payDeltaScore, payDeltaLives, outcomeDeltaScore, outcomeDeltaLives, bonus),
                        OVERLAY_SECONDS,
                        this::endTurn   // יעבור לשחקן הבא *רק אחרי* שהמסך נסגר
                );
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

        // ===== פתיחה רגילה – עם קסקייד מונפש =====
        startCascadeOpen(board, row, col);
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

    /**
     * פתיחת תא (כולל קסקייד) עם אנימציה:
     * – אם זה רק תא אחד → מתנהג כמו openCell רגיל.
     * – אם יש קסקייד אמיתי (הרבה תאים) → פותח תא-תא בטיימר.
     */
    private void startCascadeOpen(Board board, int row, int col) {

        // קודם מחשבים מה היה קורה בקסקייד רגיל
        List<Point> cascade = board.computeCascadeOrder(row, col);

        // אין קסקייד? (או רק תא אחד) – מתנהג כמו קודם
        if (cascade == null || cascade.size() <= 1) {
            board.openCell(row, col, session);
            view.refreshView();
            endTurn();
            return;
        }

        // נועל את הלוחות בזמן האנימציה
        view.setBoardsEnabled(false);

        final int[] index = {0};
        int delayMs = 60; // כמה מילישניות בין תא לתא – אפשר לשחק עם זה

        Timer t = new Timer(delayMs, e -> {

            // סיימנו את כל התאים
            if (index[0] >= cascade.size()) {
                ((Timer) e.getSource()).stop();

                view.refreshView();
                view.setBoardsEnabled(true);
                endTurn();   // פה יקרה גם קו זהב לשחקן הבא
                return;
            }

            Point p = cascade.get(index[0]++);

            // פותחים תא בודד – עם ניקוד/חיים
            board.revealSingleCell(p.x, p.y, session);

            view.refreshView();

            // אם במהלך הקסקייד נגמרו חיים או נגמר המשחק – לעצור מיד:
            if (session.isOutOfLives()
                    || board1.allMinesRevealed()
                    || board2.allMinesRevealed()) {

                ((Timer) e.getSource()).stop();
                view.setBoardsEnabled(true);
                endTurn(); // endTurn כבר יזהה GAME OVER ויפתח דיאלוג
            }
        });

        t.setRepeats(true);
        t.start();
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
}
