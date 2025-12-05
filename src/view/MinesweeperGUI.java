package view;

import model.Board;
import model.Cell;
import model.CellType;
import model.GameSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * מסך המשחק – JPanel בתוך החלון הראשי.
 * מכיל שני לוחות (שני שחקנים), ניקוד/חיים משותפים וניהול תורות.
 */
public class MinesweeperGUI extends JPanel {

    private final Board board1;
    private final Board board2;
    private final GameSession session;

    private final String player1Name;
    private final String player2Name;

    private boolean player1Turn = true; // true = תור שחקן 1, false = שחקן 2

    private JButton[][] buttons1;
    private JButton[][] buttons2;

    private JPanel boardPanel1;
    private JPanel boardPanel2;

    private JLabel player1Label;
    private JLabel player2Label;
    private JLabel livesLabel;
    private JLabel scoreLabel;
    private JLabel turnLabel;

    public MinesweeperGUI(String player1Name,
                          String player2Name,
                          Board board1,
                          Board board2,
                          GameSession session) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.board1 = board1;
        this.board2 = board2;
        this.session = session;

        initUI();
        refreshView();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Top bar: שמות + ניקוד/חיים/תור ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        // שמות
        JPanel namesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        namesPanel.setOpaque(false);
        player1Label = new JLabel(player1Name, SwingConstants.CENTER);
        player2Label = new JLabel(player2Name, SwingConstants.CENTER);
        player1Label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        player2Label.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        player1Label.setForeground(Color.WHITE);
        player2Label.setForeground(Color.LIGHT_GRAY);

        namesPanel.add(player1Label);
        namesPanel.add(player2Label);

        topBar.add(namesPanel, BorderLayout.CENTER);

        // ניקוד + חיים + תור
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setOpaque(false);

        livesLabel = new JLabel("Lives: ", SwingConstants.LEFT);
        scoreLabel = new JLabel("Score: ", SwingConstants.LEFT);
        turnLabel = new JLabel("", SwingConstants.RIGHT);

        for (JLabel lbl : new JLabel[]{livesLabel, scoreLabel, turnLabel}) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        }

        statsPanel.add(livesLabel);
        statsPanel.add(scoreLabel);
        statsPanel.add(turnLabel);

        topBar.add(statsPanel, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);

        // --- מרכז: שני לוחות זה לצד זה ---
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 30, 0));
        boardsContainer.setOpaque(false);

        boardPanel1 = buildSingleBoardPanel(board1, true);
        boardPanel2 = buildSingleBoardPanel(board2, false);

        boardsContainer.add(boardPanel1);
        boardsContainer.add(boardPanel2);

        add(boardsContainer, BorderLayout.CENTER);

        // עדכון תאורה / Enabled לפי תור
        updateTurnHighlight();
    }

    /** בונה לוח אחד (פאנל של כפתורים) */
    private JPanel buildSingleBoardPanel(Board board, boolean firstBoard) {
        int rows = board.getRows();
        int cols = board.getCols();

        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setOpaque(false);
        JButton[][] buttons = new JButton[rows][cols];

        // חישוב גודל הפונט לפי כמות השורות (ככל שיש יותר שורות, הפונט קטן יותר)
        int fontSize = 22; 
        if (rows > 15) fontSize = 12;      // Hard (16x16)
        else if (rows > 10) fontSize = 16; // Medium (13x13)

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = new JButton();
                btn.setMargin(new Insets(0, 0, 0, 0));
                
                // שימוש בגודל הפונט שחישבנו
                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));
                
                btn.setFocusPainted(false);

                final int row = r;
                final int col = c;
                final boolean isFirst = firstBoard;

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // לוח לא בתור -> אין גישה
                        if (isFirst != player1Turn) {
                            return;
                        }

                        if (SwingUtilities.isLeftMouseButton(e)) {
                            handleLeftClick(board, row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            handleRightClick(board, row, col);
                        }
                    }
                });

                panel.add(btn);
                buttons[r][c] = btn;
            }
        }

        if (firstBoard) {
            buttons1 = buttons;
        } else {
            buttons2 = buttons;
        }

        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 2, true));

        return panel;
    }
    // ---------- לוגיקת הקליקים ----------

    private void handleLeftClick(Board board, int row, int col) {
        // אם ניתן להפעיל שאלה/הפתעה (כלומר כבר נחשפה ולא הופעלה) – זה קליק שני
        if (board.canActivateSpecial(row, col)) {
            Cell cell = board.getCell(row, col);

            if (cell.getType() == CellType.SURPRISE) {
                // הפתעה: משלמים מחיר, ואז 50-50 טוב/רע
                boolean good = Math.random() < 0.5;
                session.applySurprise(good);
                board.markSpecialUsed(row, col);
            } else if (cell.getType() == CellType.QUESTION) {
                // כאן בעתיד יופיע מסך שאלה אמיתי מה-CSV
                JOptionPane.showMessageDialog(
                        this,
                        "Question activation – to be implemented.\n(קליק שני מפעיל את השאלה)",
                        "Question",
                        JOptionPane.INFORMATION_MESSAGE
                );
                board.markSpecialUsed(row, col);
            }

        } else {
            // קליק ראשון – חשיפת תא רגילה (כולל ריק/מספר/מוקש/שאלה/הפתעה)
            board.openCell(row, col, session);
        }

        endTurnAndRefresh();
    }

    private void handleRightClick(Board board, int row, int col) {
        board.toggleFlag(row, col, session);
        endTurnAndRefresh();
    }

    /** סוף תור – רענון מסך, בדיקת חיים, מעבר תור */
    private void endTurnAndRefresh() {
        refreshView();

        if (session.isOutOfLives()) {
            showGameOver();
            return;
        }

        // מעבר תור
        player1Turn = !player1Turn;
        updateTurnHighlight();
    }

    // ---------- רענון ה-GUI ----------

    public void refreshView() {
        updateBoardView(board1, buttons1);
        updateBoardView(board2, buttons2);

        livesLabel.setText("Lives: " + session.getLives()+ " ❤️");
        scoreLabel.setText("Score: " + session.getScore());
        turnLabel.setText("Turn: " + (player1Turn ? player1Name : player2Name));
    }

    private void updateBoardView(Board board, JButton[][] buttons) {
        if (buttons == null) return;
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                JButton btn = buttons[r][c];

                if (!cell.isRevealed()) {
                    // תא לא נחשף עדיין
                    if (cell.isFlagged()) {
                        btn.setText("⚑");
                    } else {
                        btn.setText("");
                    }
                    btn.setBackground(new Color(30, 60, 90));
                    btn.setForeground(Color.WHITE);

                } else {
                    // תא נחשף
                    switch (cell.getType()) {
                        case MINE:
                            btn.setText("💣");
                            btn.setBackground(new Color(150, 30, 30));
                            btn.setForeground(Color.WHITE);
                            break;
                        case NUMBER:
                            int n = cell.getAdjacentMines();
                            btn.setText(String.valueOf(n));
                            btn.setBackground(new Color(200, 200, 230));
                            btn.setForeground(Color.BLACK);
                            break;
                        case EMPTY:
                            btn.setText("");
                            btn.setBackground(new Color(180, 200, 220));
                            btn.setForeground(Color.BLACK);
                            break;
                        case QUESTION:
                            btn.setText("?");
                            btn.setBackground(new Color(200, 180, 230));
                            btn.setForeground(Color.BLACK);
                            break;
                        case SURPRISE:
                            btn.setText("🎁");
                            btn.setBackground(new Color(210, 190, 120));
                            btn.setForeground(Color.BLACK);
                            break;
                    }
                }
            }
        }
    }

    /** הדגשה של הלוח שבתור + נעילת הלוח השני */
    private void updateTurnHighlight() {
        boolean p1Active = player1Turn;

        setBoardEnabled(buttons1, p1Active);
        setBoardEnabled(buttons2, !p1Active);

        // תאורה של שמות
        player1Label.setForeground(p1Active ? Color.WHITE : Color.LIGHT_GRAY);
        player2Label.setForeground(!p1Active ? Color.WHITE : Color.LIGHT_GRAY);

        // מסגרת מודגשת ללוח הפעיל
        Color activeBorder = new Color(255, 255, 255, 180);
        Color inactiveBorder = new Color(255, 255, 255, 40);

        boardPanel1.setBorder(BorderFactory.createLineBorder(p1Active ? activeBorder : inactiveBorder, 3, true));
        boardPanel2.setBorder(BorderFactory.createLineBorder(!p1Active ? activeBorder : inactiveBorder, 3, true));

        turnLabel.setText("Turn: " + (p1Active ? player1Name : player2Name));
    }

    private void setBoardEnabled(JButton[][] buttons, boolean enabled) {
        if (buttons == null) return;
        for (JButton[] row : buttons) {
            for (JButton b : row) {
                b.setEnabled(enabled);
                // קצת שינוי צבע כשהלוח כבוי
                if (!enabled) {
                    b.setBackground(new Color(20, 40, 60));
                }
            }
        }
    }

    // ---------- סיום משחק ----------

    public void showGameOver() {
        // ננעל את הלוחות
        setBoardEnabled(buttons1, false);
        setBoardEnabled(buttons2, false);

        JOptionPane.showMessageDialog(
                this,
                "Game over!\nScore: " + session.getScore(),
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
