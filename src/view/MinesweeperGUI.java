package view;

import controller.MinesweeperController;
import model.ThemeManager;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Minesweeper GUI (Final):
 * - Timer starts when controller is attached (game starts)
 * - Pause/Resume:
 *   - blocks clicks + disables boards
 *   - overlay pause menu like the picture
 * - History saves durationSeconds excluding pause time
 * - Score chip fully opaque so nothing appears "under" it
 */
public class MinesweeperGUI extends JPanel {

    private static final int MAX_LIVES_DISPLAY = 10;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_TOP   = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FONT_OVERLAY = new Font("Arial", Font.BOLD, 40);

    // =========================
    // Overlay (cards)
    // =========================
    private JLayeredPane layeredPane;
    private JPanel overlayRoot;          // transparent dark background + centered content
    private CardLayout overlayCards;
    private JPanel overlayMessageCard;
    private JPanel overlayPauseCard;
    
    private JLabel overlayLabel;
    private Timer overlayTimer;

    // =========================
    // Game fields
    // =========================
    private final Board board1;
    private final Board board2;
    private final GameSession session;

    private final String player1Name;
    private final String player2Name;

    private final MainMenuGUI parent;
    private MinesweeperController controller;

    private CellButton[][] buttons1;
    private CellButton[][] buttons2;

    private JPanel boardPanel1;
    private JPanel boardPanel2;

    // =========================
    // UI components
    // =========================
    private JLabel playerALabel;
    private JLabel playerBLabel;
    private JLabel turnLabel;
    private JLabel timeLabel;
    private TurnIndicator p1Indicator;
    private TurnIndicator p2Indicator;

    private PauseIconButton pauseBtn;

    private LivesHeartsPanel livesHeartsPanel;
    private JLabel scoreChip;

    // clock refresh
    private Timer uiClockTimer;

    public MinesweeperGUI(MainMenuGUI parent,
                          String player1Name,
                          String player2Name,
                          Board board1,
                          Board board2,
                          GameSession session) {
        this.parent = parent;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.board1 = board1;
        this.board2 = board2;
        this.session = session;

        initUI();
        refreshView();
    }

    public void setController(MinesweeperController controller) {
        this.controller = controller;

        // timer starts HERE
        this.controller.startGameTimer();

        startUiClock();
        updateTurnHighlight();
        refreshView();
    }

    // =========================
    // Overlay API (temporary messages)
    // =========================
    public void showTemporaryOverlay(String message, int seconds) {
        if (overlayRoot == null) return;

        String html =
                "<html><div style='text-align:center; line-height:1.25;'>" +
                        message.replace("\n", "<br>") +
                        "</div></html>";

        overlayLabel.setText(html);

        overlayCards.show(overlayRoot, "MSG");
        overlayRoot.setVisible(true);
        overlayRoot.repaint();

        if (overlayTimer != null && overlayTimer.isRunning()) overlayTimer.stop();

        overlayTimer = new Timer(seconds * 1000, e -> hideOverlayIfNotPaused());
        overlayTimer.setRepeats(false);
        overlayTimer.start();
    }

    private void hideOverlayIfNotPaused() {
        if (controller != null && controller.isPaused()) return;
        overlayRoot.setVisible(false);
        overlayRoot.repaint();
    }

    private void showPauseOverlay() {
        if (overlayTimer != null && overlayTimer.isRunning()) overlayTimer.stop();
        overlayCards.show(overlayRoot, "PAUSE");
        overlayRoot.setVisible(true);
        overlayRoot.repaint();
    }

    private void hidePauseOverlay() {
        overlayRoot.setVisible(false);
        overlayRoot.repaint();
    }

    // =========================
    // UI setup
    // =========================
    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel mainPanel = buildMainPanel();

        layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                if (mainPanel != null) mainPanel.setBounds(0, 0, w, h);
                if (overlayRoot != null) overlayRoot.setBounds(0, 0, w, h);
            }
        };
        layeredPane.setLayout(null);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        createOverlay();
        layeredPane.add(overlayRoot, JLayeredPane.PALETTE_LAYER);

        add(layeredPane, BorderLayout.CENTER);
    }

    private JPanel buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout(14, 12));
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(buildPlayersRow());
        header.add(Box.createVerticalStrut(10));
        header.add(buildBoardsTitleRow());

        root.add(header, BorderLayout.NORTH);

        // Boards
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 28, 0));
        boardsContainer.setOpaque(false);

        boardPanel1 = buildSingleBoardPanel(board1, true);
        boardPanel2 = buildSingleBoardPanel(board2, false);

        boardsContainer.add(boardPanel1);
        boardsContainer.add(boardPanel2);

        root.add(boardsContainer, BorderLayout.CENTER);

        // Bottom bar
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildPlayersRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

     // ⭐ תיקון: שמות השחקנים בצדדים
     // ⭐ תיקון: שימוש בטקסט קבוע כדי למנוע שגיאות
        playerALabel = createDynamicLabel(player1Name, new Font("Segoe UI", Font.BOLD, 14));
        playerBLabel = createDynamicLabel(player2Name, new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);

        
     // ⭐ תיקון: שימוש ב-createDynamicLabel
        timeLabel = createDynamicLabel("Time: 00:00", new Font("Segoe UI", Font.BOLD, 18));
        turnLabel = createDynamicLabel("Turn: Player 1", new Font("Segoe UI", Font.BOLD, 18));
        
        pauseBtn = new PauseIconButton();
        pauseBtn.setToolTipText("Pause / Resume");
        pauseBtn.addActionListener(e -> togglePauseFromGUI());

        center.add(turnLabel);
        center.add(timeLabel);
        center.add(pauseBtn);

        p1Indicator = new TurnIndicator();
        p2Indicator = new TurnIndicator();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(p1Indicator);
        left.add(playerALabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(playerBLabel);
        right.add(p2Indicator);

        row.add(left, BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);


        return row;
    }

    private JPanel buildBoardsTitleRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel boardALabel = createDynamicLabel("Board A", new Font("Segoe UI", Font.BOLD, 16));
        JLabel boardBLabel = createDynamicLabel("Board B", new Font("Segoe UI", Font.BOLD, 16));

        JPanel legend = createLegendPanel();

        row.add(boardALabel, BorderLayout.WEST);
        row.add(legend, BorderLayout.CENTER);
        row.add(boardBLabel, BorderLayout.EAST);

        return row;
    }

    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        livesHeartsPanel = new LivesHeartsPanel(MAX_LIVES_DISPLAY);
        livesHeartsPanel.setOpaque(false);

        scoreChip = new JLabel("", SwingConstants.CENTER);
        scoreChip.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scoreChip.setForeground(Color.WHITE);

        // ✅ חשוב: רקע אטום לגמרי כדי שלא יראו "Shared Lives" / "Turn/Time" מתחת
        scoreChip.setOpaque(true);
        scoreChip.setBackground(new Color(30, 55, 85)); // no alpha

        scoreChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        bottom.add(livesHeartsPanel, BorderLayout.WEST);
        bottom.add(scoreChip, BorderLayout.EAST);
        return bottom;
    }

    private JPanel createLegendPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        p.setOpaque(false);

        p.add(new LegendIconChip("❓", new Color(200, 180, 230), Color.BLACK));
        p.add(new LegendIconChip("🎁", new Color(210, 190, 120), Color.BLACK));
        p.add(new LegendIconChip("💣", new Color(150, 30, 30), Color.WHITE));

        return p;
    }

    // =========================
    // Overlay creation (Pause menu style)
    // =========================
    private void createOverlay() {
        overlayCards = new CardLayout();
        overlayRoot = new JPanel(overlayCards) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 140)); // dark transparent
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlayRoot.setOpaque(false);
        overlayRoot.setVisible(false);

        // message card
        overlayMessageCard = new JPanel(new GridBagLayout());
        overlayMessageCard.setOpaque(false);
        overlayLabel = new JLabel("", SwingConstants.CENTER);
        overlayLabel.setFont(FONT_OVERLAY);
        overlayLabel.setForeground(new Color(255, 140, 0));
        overlayMessageCard.add(overlayLabel);

        // pause card (wood panel + buttons)
        overlayPauseCard = new PauseMenuPanel(
                () -> togglePauseFromGUI(),                  // RESUME
                () -> parent.startGame(player1Name, player2Name, session.getDifficulty()), // restart
                () -> parent.showMainMenu(),                 // menu
                () -> JOptionPane.showMessageDialog(this, "Settings") // settings placeholder
        );

        overlayRoot.add(overlayMessageCard, "MSG");
        overlayRoot.add(overlayPauseCard, "PAUSE");
    }

    // =========================
    // Board build
    // =========================
    private JPanel buildSingleBoardPanel(Board board, boolean firstBoard) {
        int rows = board.getRows();
        int cols = board.getCols();

        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setOpaque(false);
        CellButton[][] buttons = new CellButton[rows][cols];

        int fontSize = 24;
        if (rows > 15) fontSize = 14;
        else if (rows > 10) fontSize = 18;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
            	CellButton btn = new CellButton();
            	btn.setFocusPainted(false);
            	btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));

                final int row = r;
                final int col = c;
                final boolean isFirst = firstBoard;

                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (controller == null) return;
                        if (controller.isPaused()) return;
                        if (isFirst != controller.isPlayer1Turn()) return;

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

        if (firstBoard) buttons1 = buttons;
        else buttons2 = buttons;

        panel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 4, true));
        return panel;
    }

    // =========================
    // Pause handling
    // =========================
    private void togglePauseFromGUI() {
        if (controller == null) return;

        controller.togglePause();
        boolean paused = controller.isPaused();

        pauseBtn.setPaused(paused);

        setAllBoardsEnabled(!paused);

        if (paused) showPauseOverlay();
        else hidePauseOverlay();

        refreshView();
    }

    private void setAllBoardsEnabled(boolean enabled) {
        setBoardEnabled(buttons1, enabled);
        setBoardEnabled(buttons2, enabled);
    }

    private void setBoardEnabled(CellButton[][] buttons, boolean enabled) {
        if (buttons == null) return;
        for (CellButton[] row : buttons) {
            for (CellButton b : row) b.setEnabled(enabled);
        }
    }

    // =========================
    // UI Clock
    // =========================
    private void startUiClock() {
        if (uiClockTimer != null && uiClockTimer.isRunning()) uiClockTimer.stop();
        uiClockTimer = new Timer(250, e -> updateTimeLabel());
        uiClockTimer.start();
    }

    private void updateTimeLabel() {
        if (controller == null) return;

        long ms = controller.getElapsedActiveMillis();
        long totalSeconds = ms / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        timeLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    // =========================
    // Refresh
    // =========================
    public void refreshView() {
        boolean p1Turn = (controller == null) || controller.isPlayer1Turn();

        Color board1Color = new Color(100, 149, 237);
        Color board2Color = new Color(72, 209, 204);

        updateBoardView(board1, buttons1, board1Color, p1Turn);
        updateBoardView(board2, buttons2, board2Color, !p1Turn);

        String current = p1Turn ? player1Name : player2Name;
        turnLabel.setText("Turn: " + current);

        livesHeartsPanel.setLives(session.getLives());
        livesHeartsPanel.repaint();

        scoreChip.setText("Score: " + session.getScore());

        updateTimeLabel();
        updateTurnIndicatorUI();
    }
    
    private void updateTurnIndicatorUI() {
        boolean p1Turn = (controller == null) || controller.isPlayer1Turn();
        p1Indicator.setActive(p1Turn);
        p2Indicator.setActive(!p1Turn);
    }

    private void updateBoardView(Board board, CellButton[][] buttons, Color playerColor, boolean active) {
        if (buttons == null) return;

        int rows = board.getRows();
        int cols = board.getCols();

        Color baseColor = active ? playerColor : darker(playerColor, 0.6);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                CellButton btn = buttons[r][c];

                if (!cell.isRevealed()) {
                    btn.setText(cell.isFlagged() ? "🚩" : "");
                    btn.setFill(baseColor);
                    btn.setTextColor(Color.WHITE);
                    btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, btn.getFont().getSize()));
                } else {
                    switch (cell.getType()) {
                    case MINE -> {
                        btn.setText("💣");
                        btn.setFill(new Color(170, 40, 40), new Color(190, 55, 55));
                        btn.setTextColor(Color.WHITE);

                        int h = btn.getHeight();
                        
                        if (h > 0) {
                            int newSize = (int)(h * 0.6); 
                            
                            if (newSize < 8) newSize = 8;
                            
                            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, newSize));
                        } else {
                            btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                        }
                    }
                        
                        
                        case NUMBER -> {
                            btn.setText(String.valueOf(cell.getAdjacentMines()));
                            btn.setFill(new Color(210, 215, 235), new Color(220, 225, 245));
                            btn.setTextColor(Color.BLACK);
                            btn.setFont(new Font("Segoe UI", Font.BOLD, btn.getFont().getSize()));
                        }
                        case EMPTY -> {
                            btn.setText("");
                            btn.setFill(new Color(185, 205, 225), new Color(195, 215, 235));
                            btn.setTextColor(new Color(40, 40, 40));
                            btn.setFont(new Font("Segoe UI", Font.BOLD, btn.getFont().getSize()));
                        }
                        case QUESTION -> {
                            if (cell.isPowerUsed()) {
                                btn.setText("USED");
                                btn.setFill(new Color(200, 180, 230), new Color(210, 195, 240)); 
                                btn.setTextColor(Color.BLACK);
                                btn.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, btn.getFont().getSize() - 6)));
                            } else {
                                btn.setText("❓");
                                btn.setFill(new Color(200, 180, 230), new Color(210, 195, 240)); 
                                btn.setTextColor(Color.BLACK);
                                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, btn.getFont().getSize()));
                            }
                        }
                        case SURPRISE -> {
                            if (cell.isPowerUsed()) {
                                btn.setText("USED");
                                btn.setFill(new Color(210, 190, 120), new Color(220, 200, 135)); 
                                btn.setTextColor(Color.BLACK);
                                btn.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, btn.getFont().getSize() - 6)));
                            } else {
                                btn.setText("🎁");
                                btn.setFill(new Color(210, 190, 120), new Color(220, 200, 135)); // surprise
                                btn.setTextColor(Color.BLACK);
                                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, btn.getFont().getSize()));
                            }
                        }
                    }
                }
            }
        }
    }

    private Color darker(Color c, double factor) {
        int r = (int) (c.getRed() * factor);
        int g = (int) (c.getGreen() * factor);
        int b = (int) (c.getBlue() * factor);
        return new Color(r, g, b);
    }

    public void updateTurnHighlight() {
        boolean p1Active = (controller == null) || controller.isPlayer1Turn();
        boolean paused = controller != null && controller.isPaused();

        if (paused) {
            setAllBoardsEnabled(false);
        } else {
            setBoardEnabled(buttons1, p1Active);
            setBoardEnabled(buttons2, !p1Active);
        }
        updateTurnIndicatorUI();
    }

    // =========================
    // Game Over (saves durationSeconds)
    // =========================
    public void showGameOver(boolean success) {
        if (uiClockTimer != null && uiClockTimer.isRunning()) uiClockTimer.stop();

        int livesBefore = session.getLives();
        int minesRevealed = countRevealedMines(board1) + countRevealedMines(board2);

        int durationSeconds = (controller == null) ? 0 : (int) (controller.getElapsedActiveMillis() / 1000);

        session.convertRemainingLivesToScoreAtEnd();

        board1.revealAllCells();
        board2.revealAllCells();
        refreshView();
        setAllBoardsEnabled(false);

        String resultLabel = success ? "All mines revealed" : "Out of lives";
        String difficultyText = session.getDifficulty().name();

        GameHistory history = new GameHistory();
        history.addEntry(
                player1Name + " & " + player2Name,
                session.getScore(),
                difficultyText,
                resultLabel,
                durationSeconds
        );

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                success ? "You Won! 🎉" : "Game Over",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        center.setBackground(new Color(20, 30, 50));

        JLabel titleLbl = new JLabel(success ? "Great job, team!" : "Better luck next time", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLbl = new JLabel("Final score: " + session.getScore(), SwingConstants.CENTER);
        JLabel livesLbl = new JLabel("Lives left: " + livesBefore, SwingConstants.CENTER);
        JLabel minesLbl = new JLabel("Mines revealed: " + minesRevealed, SwingConstants.CENTER);
        JLabel diffLbl  = new JLabel("Difficulty: " + difficultyText, SwingConstants.CENTER);
        JLabel durLbl   = new JLabel("Duration (sec): " + durationSeconds, SwingConstants.CENTER);

        for (JLabel lbl : new JLabel[]{scoreLbl, livesLbl, minesLbl, diffLbl, durLbl}) {
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lbl.setForeground(new Color(220, 230, 245));
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        center.add(titleLbl);
        center.add(Box.createVerticalStrut(10));
        center.add(scoreLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(livesLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(minesLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(diffLbl);
        center.add(Box.createVerticalStrut(5));
        center.add(durLbl);

        dialog.add(center, BorderLayout.CENTER);

        JButton playAgainBtn = new JButton("Play Again");
        JButton mainMenuBtn  = new JButton("Main Menu");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(new Color(15, 25, 40));
        btnPanel.add(playAgainBtn);
        btnPanel.add(mainMenuBtn);

        dialog.add(btnPanel, BorderLayout.SOUTH);

        playAgainBtn.addActionListener(e -> {
            dialog.dispose();
            parent.startGame(player1Name, player2Name, session.getDifficulty());
        });

        mainMenuBtn.addActionListener(e -> {
            dialog.dispose();
            parent.showMainMenu();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int countRevealedMines(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.getType() == CellType.MINE && cell.isRevealed()) count++;
            }
        }
        return count;
    }

    // ======================================================================
    // UI Components
    // ======================================================================

    /** Pause icon button (drawn) */
    private static class PauseIconButton extends JButton {
        private boolean paused = false;

        PauseIconButton() {
            setPreferredSize(new Dimension(54, 34));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        void setPaused(boolean paused) {
            this.paused = paused;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // rounded dark chip
            g2.setColor(new Color(20, 35, 55, 220));
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // red circle
            int d = Math.min(w, h) - 10;
            int cx = (w - d) / 2;
            int cy = (h - d) / 2;

            g2.setColor(new Color(220, 40, 40));
            g2.fillOval(cx, cy, d, d);

            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, d, d);

            // icon inside
            g2.setColor(Color.BLACK);
            int innerX = cx + d / 3;
            int innerY = cy + d / 4;
            int barW = d / 8;
            int barH = d / 2;

            if (!paused) {
                // pause bars
                g2.fillRoundRect(innerX, innerY, barW, barH, 3, 3);
                g2.fillRoundRect(innerX + barW * 2, innerY, barW, barH, 3, 3);
            } else {
                // play triangle
                Polygon tri = new Polygon();
                tri.addPoint(innerX, innerY);
                tri.addPoint(innerX, innerY + barH);
                tri.addPoint(innerX + barW * 3, innerY + barH / 2);
                g2.fillPolygon(tri);
            }

            g2.dispose();
        }
    }

    /** Pause menu overlay panel like the picture */
    private static class PauseMenuPanel extends JPanel {
        private final Runnable onResume;
        private final Runnable onRestart;
        private final Runnable onMenu;
        private final Runnable onSettings;

        PauseMenuPanel(Runnable onResume, Runnable onRestart, Runnable onMenu, Runnable onSettings) {
            this.onResume = onResume;
            this.onRestart = onRestart;
            this.onMenu = onMenu;
            this.onSettings = onSettings;

            setOpaque(false);
            setLayout(new GridBagLayout());

            JPanel wood = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // wood base
                    g2.setColor(new Color(216, 162, 76));
                    g2.fillRoundRect(0, 0, w, h, 26, 26);

                    // subtle lines
                    g2.setColor(new Color(255, 255, 255, 35));
                    for (int y = 25; y < h; y += 28) {
                        g2.drawLine(18, y, w - 18, y);
                    }

                    // border
                    g2.setColor(new Color(100, 65, 25, 180));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 26, 26);

                    g2.dispose();
                }
            };
            wood.setOpaque(false);
            wood.setPreferredSize(new Dimension(520, 340));
            wood.setLayout(new BoxLayout(wood, BoxLayout.Y_AXIS));
            wood.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

            // PAUSED title
            JPanel titleWrap = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth(), h = getHeight();
                    g2.setColor(new Color(30, 25, 20, 220));
                    g2.fillRoundRect(0, 0, w, h, 12, 12);
                    g2.setColor(new Color(255, 255, 255, 70));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

                    g2.dispose();
                }
            };
            titleWrap.setOpaque(false);
            titleWrap.setMaximumSize(new Dimension(440, 74));
            titleWrap.setPreferredSize(new Dimension(440, 74));
            titleWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleWrap.setLayout(new GridBagLayout());

            JLabel pausedLbl = new JLabel("PAUSED", SwingConstants.CENTER);
            pausedLbl.setFont(new Font("Segoe UI", Font.BOLD, 44));
            pausedLbl.setForeground(new Color(255, 80, 60));
            titleWrap.add(pausedLbl);

            wood.add(titleWrap);
            wood.add(Box.createVerticalStrut(26));

            // three circle buttons
            JPanel circles = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 0));
            circles.setOpaque(false);

            circles.add(new RoundIconButton(RoundIconButton.IconType.RESTART, onRestart));
            circles.add(new RoundIconButton(RoundIconButton.IconType.MENU, onMenu));
            circles.add(new RoundIconButton(RoundIconButton.IconType.SETTINGS, onSettings));

            wood.add(circles);
            wood.add(Box.createVerticalStrut(28));

            // RESUME big button
            JButton resume = new JButton("RESUME") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    g2.setColor(new Color(141, 214, 76));
                    g2.fillRoundRect(0, 0, w, h, 16, 16);

                    g2.setColor(new Color(60, 120, 30));
                    g2.setStroke(new BasicStroke(4f));
                    g2.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);

                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            resume.setOpaque(false);
            resume.setContentAreaFilled(false);
            resume.setBorderPainted(false);
            resume.setFocusPainted(false);
            resume.setFont(new Font("Segoe UI", Font.BOLD, 34));
            resume.setForeground(new Color(30, 70, 20));
            resume.setAlignmentX(Component.CENTER_ALIGNMENT);
            resume.setPreferredSize(new Dimension(320, 86));
            resume.setMaximumSize(new Dimension(320, 86));
            resume.setCursor(new Cursor(Cursor.HAND_CURSOR));
            resume.addActionListener(e -> onResume.run());

            JPanel resumeWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            resumeWrap.setOpaque(false);
            resumeWrap.add(resume);

            wood.add(resumeWrap);

            add(wood);
        }
    }

    /** Round button with drawn icons (so no "rectangle" glyph issues) */
    private static class RoundIconButton extends JButton {
        enum IconType { RESTART, MENU, SETTINGS }
        private final IconType type;

        RoundIconButton(IconType type, Runnable action) {
            this.type = type;

            setPreferredSize(new Dimension(86, 86));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addActionListener(e -> action.run());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // shadow
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(6, 8, w - 10, h - 10);

            // main circle
            g2.setColor(new Color(70, 190, 185));
            g2.fillOval(0, 0, w - 10, h - 10);

            // border
            g2.setColor(new Color(210, 255, 255, 160));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(1, 1, w - 12, h - 12);

            // icon
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = (w - 10) / 2;
            int cy = (h - 10) / 2;

            switch (type) {
                case MENU -> {
                    // three lines (fixes the "rectangle" problem)
                    g2.drawLine(cx - 16, cy - 10, cx + 16, cy - 10);
                    g2.drawLine(cx - 16, cy,      cx + 16, cy);
                    g2.drawLine(cx - 16, cy + 10, cx + 16, cy + 10);
                }
                case SETTINGS -> {
                    // simple gear-ish icon
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawOval(cx - 14, cy - 14, 28, 28);
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3.0;
                        int x1 = (int) (cx + Math.cos(a) * 18);
                        int y1 = (int) (cy + Math.sin(a) * 18);
                        int x2 = (int) (cx + Math.cos(a) * 26);
                        int y2 = (int) (cy + Math.sin(a) * 26);
                        g2.drawLine(x1, y1, x2, y2);
                    }
                    g2.fillOval(cx - 5, cy - 5, 10, 10);
                }
                case RESTART -> {
                    // circular arrow
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawArc(cx - 16, cy - 16, 32, 32, 40, 280);
                    Polygon arrow = new Polygon();
                    arrow.addPoint(cx + 16, cy - 6);
                    arrow.addPoint(cx + 28, cy - 6);
                    arrow.addPoint(cx + 22, cy + 6);
                    g2.fillPolygon(arrow);
                }
            }

            g2.dispose();
        }
    }

    /** Legend chip */
    private static class LegendIconChip extends JComponent {
        private final String text;
        private final Color bg;
        private final Color fg;

        LegendIconChip(String text, Color bg, Color fg) {
            this.text = text;
            this.bg = bg;
            this.fg = fg;
            setPreferredSize(new Dimension(46, 34));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(new Color(255, 255, 255, 90));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));

            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    /** Lives hearts panel */
    private static class LivesHeartsPanel extends JComponent {
        private int lives = MAX_LIVES_DISPLAY;
        private final int maxLives;

        LivesHeartsPanel(int maxLives) {
            this.maxLives = maxLives;
            setPreferredSize(new Dimension(560, 38));
            setOpaque(false);
        }

        void setLives(int lives) {
            this.lives = Math.max(0, Math.min(maxLives, lives));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
         // ⭐ תיקון: צבע הטקסט לפי הת'ים (שחור/לבן)
            g2.setColor(model.ThemeManager.getInstance().getTextColor());
            String label = "Shared Lives: " + lives + " / " + maxLives;
            FontMetrics fm = g2.getFontMetrics();
            int x = 0;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, x, y);

            int startX = fm.stringWidth(label) + 16;
            int centerY = getHeight() / 2;

            int heartW = 18;
            int heartH = 16;
            int gap = 7;

            for (int i = 0; i < maxLives; i++) {
                int hx = startX + i * (heartW + gap);
                boolean filled = i < lives;

             // ⭐ תיקון: צבע הלבבות משתנה לפי הת'ים
                if (filled) {
                    // לב מלא: מקבל את צבע הטקסט הראשי (לבן או שחור)
                    g2.setColor(model.ThemeManager.getInstance().getTextColor());
                    drawHeart(g2, hx, centerY - heartH / 2, heartW, heartH, true);
                } else {
                    // לב ריק: אפור בהיר במצב כהה, או אפור כהה במצב בהיר
                    Color emptyColor = model.ThemeManager.getInstance().isDarkMode()
                            ? new Color(200, 200, 200, 160) // לבן חלש (לרקע כהה)
                            : new Color(0, 0, 0, 100);      // שחור חלש (לרקע בהיר)
                    
                    g2.setColor(emptyColor);
                    drawHeart(g2, hx, centerY - heartH / 2, heartW, heartH, false);
                }
            }

            g2.dispose();
        }

        private void drawHeart(Graphics2D g2, int x, int y, int w, int h, boolean fill) {
            int cx1 = x + w / 4;
            int cx2 = x + (3 * w) / 4;

            Polygon bottom = new Polygon();
            bottom.addPoint(x, y + h / 3);
            bottom.addPoint(x + w, y + h / 3);
            bottom.addPoint(x + w / 2, y + h);

            Shape leftCircle = new java.awt.geom.Ellipse2D.Double(cx1 - w / 4.0, y, w / 2.0, h / 1.6);
            Shape rightCircle = new java.awt.geom.Ellipse2D.Double(cx2 - w / 4.0, y, w / 2.0, h / 1.6);

            java.awt.geom.Area heart = new java.awt.geom.Area(leftCircle);
            heart.add(new java.awt.geom.Area(rightCircle));
            heart.add(new java.awt.geom.Area(bottom));

            if (fill) g2.fill(heart);
            else {
                g2.setStroke(new BasicStroke(2f));
                g2.draw(heart);
            }
        }

    }
    /**
     * פונקציה עזר: יוצרת תווית (Label) שמשנה צבע לפי הת'ים.
     */
    private JLabel createDynamicLabel(String text, Font font) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Color themeColor = ThemeManager.getInstance().getTextColor();
                if (!getForeground().equals(themeColor)) {
                    setForeground(themeColor);
                }
                super.paintComponent(g);
            }
        };
        lbl.setFont(font);
        return lbl;
    }
}
