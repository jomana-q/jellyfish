package view;

import controller.MinesweeperController;
import model.Board;
import model.Cell;
import model.CellType;
import model.GameSession;
import model.Question;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * מסך המשחק – JPanel בתוך החלון הראשי.
 * מכיל שני לוחות (שני שחקנים), ניקוד/חיים משותפים וניהול תורות.
 * ה-View לא מפעיל ישירות את המודל – הוא מדבר עם MinesweeperController.
 */
public class MinesweeperGUI extends JPanel {

    private final Board board1;
    private final Board board2;
    private final GameSession session;

    private final String player1Name;
    private final String player2Name;

    // הבקר – מוזן מבחוץ (MainMenuGUI)
    private MinesweeperController controller;

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

    /**
     * חיבור הבקר ל-View (נקרא מתוך MainMenuGUI).
     */
    public void setController(MinesweeperController controller) {
        this.controller = controller;
        // אחרי שיש בקר, כדאי לעדכן הדגשת תור
        updateTurnHighlight();
    }

    // --- בניית UI ---

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

        // הדגשת תור ראשונית (לפני שהבקר מוזן – מניחים שחקן 1)
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

                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));
                btn.setFocusPainted(false);

                final int row = r;
                final int col = c;
                final boolean isFirst = firstBoard;

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (controller == null) return;

                        // לוח לא בתור -> אין גישה
                        if (isFirst != controller.isPlayer1Turn()) {
                            return;
                        }

                        if (SwingUtilities.isLeftMouseButton(e)) {
                            controller.handleLeftClick(isFirst, row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            controller.handleRightClick(isFirst, row, col);
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

        panel.setBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 2, true)
        );

        return panel;
    }

    // ---------- רענון ה-GUI ----------

    public void refreshView() {
        boolean p1Turn = (controller == null) || controller.isPlayer1Turn();

        // צבע בסיס לכל לוח
        Color board1Color = new Color(100, 149, 237); // cornflower blue
        Color board2Color = new Color(72, 209, 204);  // turquoise

        updateBoardView(board1, buttons1, board1Color, p1Turn);
        updateBoardView(board2, buttons2, board2Color, !p1Turn);

        livesLabel.setText("Lives: " + session.getLives() + " ❤️");
        scoreLabel.setText("Score: " + session.getScore());
        turnLabel.setText("Turn: " + (p1Turn ? player1Name : player2Name));
    }

    /**
     * צביעה וריענון של לוח בודד.
     * @param playerColor צבע הבסיס של השחקן
     * @param active האם זה הלוח שבתור כרגע
     */
    private void updateBoardView(Board board, JButton[][] buttons, Color playerColor, boolean active) {
        if (buttons == null) return;
        int rows = board.getRows();
        int cols = board.getCols();

        // אם הלוח לא פעיל – נייצר גוון קצת כהה יותר של אותו צבע
        Color baseColor = active ? playerColor : darker(playerColor, 0.6);

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
                    btn.setBackground(baseColor);
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

    /** יצירת צבע כהה יותר מאותו צבע בסיס */
    private Color darker(Color c, double factor) {
        int r = (int) (c.getRed() * factor);
        int g = (int) (c.getGreen() * factor);
        int b = (int) (c.getBlue() * factor);
        return new Color(r, g, b);
    }

    /** הדגשה של הלוח שבתור + נעילת הלוח השני */
    public void updateTurnHighlight() {
        boolean p1Active = (controller == null) || controller.isPlayer1Turn();

        setBoardEnabled(buttons1, p1Active);
        setBoardEnabled(buttons2, !p1Active);

        // תאורה של שמות
        player1Label.setForeground(p1Active ? Color.WHITE : Color.LIGHT_GRAY);
        player2Label.setForeground(!p1Active ? Color.WHITE : Color.LIGHT_GRAY);

        // מסגרת מודגשת ללוח הפעיל
        Color activeBorder = new Color(255, 255, 255, 180);
        Color inactiveBorder = new Color(255, 255, 255, 40);

        if (boardPanel1 != null) {
            boardPanel1.setBorder(
                    BorderFactory.createLineBorder(p1Active ? activeBorder : inactiveBorder, 3, true)
            );
        }
        if (boardPanel2 != null) {
            boardPanel2.setBorder(
                    BorderFactory.createLineBorder(!p1Active ? activeBorder : inactiveBorder, 3, true)
            );
        }

        turnLabel.setText("Turn: " + (p1Active ? player1Name : player2Name));
    }

    private void setBoardEnabled(JButton[][] buttons, boolean enabled) {
        if (buttons == null) return;
        for (JButton[] row : buttons) {
            for (JButton b : row) {
                b.setEnabled(enabled);
                // לא נוגעים ברקע כאן כדי לשמור על הצבע הייחודי של כל לוח
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

    /**
     * מציג חלון שאלה (4 תשובות) ומחזיר את אינדקס התשובה שנבחרה (0-3).
     * אם המשתמש סגר את החלון בלי לבחור תשובה – מוחזר null.
     */
    public Integer askQuestion(Question question) {
        String[] answers = question.getAnswers();

        int choice = JOptionPane.showOptionDialog(
                this,
                question.getQuestionText(),
                "Question",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                answers,
                answers[0]
        );

        if (choice < 0 || choice >= answers.length) {
            return null; // סגירה / ביטול
        }
        return choice;
    }

}
