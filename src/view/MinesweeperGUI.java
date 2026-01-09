package view;

import controller.MinesweeperController;
import model.*;
import model.ThemeManager;
import view.LegendIconChip;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MinesweeperGUI extends JPanel {

    private static final int MAX_LIVES_DISPLAY = 10;

    // Overlay
    private JLayeredPane layeredPane;
    private JPanel overlayRoot;
    private CardLayout overlayCards;

    private JPanel overlayMessageCard;
    private JPanel overlayPauseCard;

    private JLabel overlayTitle;
    private JTextPane overlaySub;
    private JLabel overlayEmoji;
    @SuppressWarnings("unused")
    private JButton overlayCloseBtn;
    @SuppressWarnings("unused")
    private OverlayIcon overlayIcon;

    private Timer overlayAutoHideTimer;
    // Overlay theme state (used for Question result overlay)
    private boolean overlayUsingQuestionTheme = false;

    // Default (Surprise / pink) theme
    private static final Color OVERLAY_PINK_BG     = new Color(255, 220, 240, 245);
    private static final Color OVERLAY_PINK_BORDER = new Color(150, 70, 170, 180);

    // Question (blue) theme
    private static final Color OVERLAY_BLUE_BG     = new Color(220, 240, 255, 245);
    private static final Color OVERLAY_BLUE_BORDER = new Color(70, 140, 210, 180);

    public enum OverlayType { GOOD, BAD, INFO }

    // Overlay font sizing
    private static final int OVERLAY_TITLE_MAX = 24;
    private static final int OVERLAY_TITLE_MIN = 16;
    private static final int OVERLAY_TITLE_WIDTH = 340;
    private static final int OVERLAY_SUB_SIZE = 15;

    // Game fields
    private final Board board1;
    private final Board board2;
    private final GameSession session;

    private final String player1Name;
    private final String player2Name;

    private final MainMenuGUI parent;
    private MinesweeperController controller;

    private CellButton[][] buttons1;
    private CellButton[][] buttons2;

    // ⭐ חדשים – פאנלים של הלוחות עצמם (בשביל מסגרת זהב מונפשת)
    private AnimatedBoardPanel boardPanel1;
    private AnimatedBoardPanel boardPanel2;

    // UI components
    private JLabel playerALabel;
    private JLabel playerBLabel;
    private JLabel turnLabel;
    private JLabel timeLabel;
    private TurnIndicator p1Indicator;
    private TurnIndicator p2Indicator;
    private PauseIconButton pauseBtn;
    private LivesHeartsPanel livesHeartsPanel;
    private JLabel scoreChip;

    // Gift overlay (full screen)
    private JPanel cardsHolder;
    private JPanel giftOverlay;
    private JLabel giftLabel;

    // Icons (loaded from /images/)
    private final ImageIcon ICON_QUESTION  = loadIcon("/images/Ques.png");
    private final ImageIcon ICON_SURPRISE  = loadIcon("/images/surprise.png");
    private final ImageIcon ICON_MINE      = loadIcon("/images/Boom.png");
    private final ImageIcon ICON_FLAG      = loadIcon("/images/flag.png");
    // Gift icons
    private final ImageIcon ICON_GIFT_CLOSED = loadIcon("/images/gift_closed.png");
    private final ImageIcon ICON_GIFT_OPEN   = loadIcon("/images/gift_open.png");
    // Overlay emoji icons
    private final ImageIcon ICON_GOOD_HEART = loadIcon("/images/heart_good.png");
    private final ImageIcon ICON_BAD_HEART  = loadIcon("/images/heart_bad.png");
    // Question result emoji icons (blue)
    private final ImageIcon ICON_BLUE_GOOD = loadIcon("/images/blue_good.png");
    private final ImageIcon ICON_BLUE_BAD  = loadIcon("/images/blue_bad.png");

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
        this.controller.startGameTimer();
        startUiClock();
        updateTurnHighlight(); // יעדכן גם את המסגרת הזהובה
        refreshView();
    }

    // ========== helpers ==========

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Missing icon resource: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    private ImageIcon scaledIcon(ImageIcon src, int w, int h) {
        if (src == null || src.getImage() == null) return src;
        Image img = src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void fitOverlayTitleToWidth(String text, int maxWidth, int maxSize, int minSize) {
        if (overlayTitle == null) return;

        int size = maxSize;
        while (size >= minSize) {
            Font f = new Font("Segoe UI", Font.BOLD, size);
            FontMetrics fm = overlayTitle.getFontMetrics(f);
            if (fm.stringWidth(text) <= maxWidth) {
                overlayTitle.setFont(f);
                return;
            }
            size--;
        }
        overlayTitle.setFont(new Font("Segoe UI", Font.BOLD, minSize));
    }

    private int computeCenterGiftSize() {
        int w = (overlayRoot != null) ? overlayRoot.getWidth() : 0;
        int h = (overlayRoot != null) ? overlayRoot.getHeight() : 0;

        if (w <= 0 || h <= 0) return 260; // fallback

        int base = Math.min(w, h);
        int size = (int) (base * 0.45);   // 45% מהמסך
        return Math.max(180, Math.min(size, 420));
    }

    // ========== Overlay API ==========

    public void showResultOverlay(OverlayType type, String title, String subtitle, int seconds) {
        if (overlayRoot == null) return;

        String safeTitle = (title == null) ? "" : title.trim();
        String safeSub   = (subtitle == null) ? "" : subtitle.trim();

        overlayTitle.setText(safeTitle);
        fitOverlayTitleToWidth(safeTitle, OVERLAY_TITLE_WIDTH, OVERLAY_TITLE_MAX, OVERLAY_TITLE_MIN);

        overlaySub.setText(safeSub.replace(" | ", "\n"));
        overlaySub.setFont(new Font("Segoe UI", Font.PLAIN, OVERLAY_SUB_SIZE));

        if (overlayUsingQuestionTheme) {
            overlayTitle.setForeground(new Color(18, 55, 105));
        } else {
            overlayTitle.setForeground(new Color(120, 45, 160));
        }

        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        if (overlayUsingQuestionTheme) {
            OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);
        } else {
            OverlayCardPanel.setTheme(OVERLAY_PINK_BG, OVERLAY_PINK_BORDER);
        }

        if (overlayEmoji != null) {
            overlayEmoji.setText("");
            int s = 140;

            ImageIcon icon = null;
            if (overlayUsingQuestionTheme) {
                if (type == OverlayType.GOOD) icon = ICON_BLUE_GOOD;
                else if (type == OverlayType.BAD) icon = ICON_BLUE_BAD;
            } else {
                if (type == OverlayType.GOOD) icon = ICON_GOOD_HEART;
                else if (type == OverlayType.BAD) icon = ICON_BAD_HEART;
            }

            overlayEmoji.setIcon(icon == null ? null : scaledIcon(icon, s, s));
        }

        if (overlayCards != null && cardsHolder != null) {
            overlayCards.show(cardsHolder, "MSG");
            cardsHolder.setVisible(true);
        }

        overlayRoot.setVisible(true);
        overlayRoot.repaint();

        if (overlayAutoHideTimer != null && overlayAutoHideTimer.isRunning()) {
            overlayAutoHideTimer.stop();
        }
        if (seconds > 0) {
            overlayAutoHideTimer = new Timer(seconds * 1000, e -> hideOverlayNow());
            overlayAutoHideTimer.setRepeats(false);
            overlayAutoHideTimer.start();
        }
    }

    public void showResultOverlay(String title, String subtitle, int seconds) {
        OverlayStyle style = OverlayStyle.fromTitle(title);
        OverlayType type = switch (style) {
            case POSITIVE -> OverlayType.GOOD;
            case NEGATIVE -> OverlayType.BAD;
            default -> OverlayType.INFO;
        };
        showResultOverlay(type, title, subtitle, seconds);
    }

    public void showTemporaryOverlay(String message) {
        String t = (message == null) ? "" : message;
        String[] lines = t.split("\n", 2);
        String title = lines[0];
        String sub = (lines.length > 1) ? lines[1] : "";
        showResultOverlay(title, sub, 2);
    }

    public void showTemporaryOverlay(String message, int seconds) {
        String t = (message == null) ? "" : message;
        String[] lines = t.split("\n", 2);
        String title = lines[0];
        String sub = (lines.length > 1) ? lines[1] : "";
        showResultOverlay(title, sub, seconds);
    }

    private void hideOverlayNow() {
        if (overlayAutoHideTimer != null) overlayAutoHideTimer.stop();
        if (overlayRoot == null) return;

        overlayUsingQuestionTheme = false;
        OverlayCardPanel.setTheme(OVERLAY_PINK_BG, OVERLAY_PINK_BORDER);

        overlayRoot.setVisible(false);
        overlayRoot.repaint();
    }

    private void showPauseOverlay() {
        if (overlayRoot == null) return;
        overlayCards.show(cardsHolder, "PAUSE");
        overlayRoot.setVisible(true);
        overlayRoot.repaint();
    }

    private void hidePauseOverlay() {
        if (overlayRoot == null) return;
        overlayRoot.setVisible(false);
        overlayRoot.repaint();
    }

    // ========== UI setup ==========

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

        // --- header (Players + Turn + Time) ---
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(buildPlayersRow());
        header.add(Box.createVerticalStrut(10));
        header.add(buildBoardsTitleRow());

        root.add(header, BorderLayout.NORTH);

        // --- שני הלוחות (עם מסגרת זהב מונפשת) ---
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, 28, 0));
        boardsContainer.setOpaque(false);

        // grid בלבד
        JPanel grid1 = buildBoardGridPanel(board1, true);
        JPanel grid2 = buildBoardGridPanel(board2, false);

        // עטיפה בפאנל מונפש
        boardPanel1 = new AnimatedBoardPanel(grid1);
        boardPanel2 = new AnimatedBoardPanel(grid2);

        boardsContainer.add(boardPanel1);
        boardsContainer.add(boardPanel2);

        root.add(boardsContainer, BorderLayout.CENTER);

        // --- שורת תחתונה (לבבות + ניקוד) ---
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildPlayersRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        playerALabel = createDynamicLabel(player1Name, new Font("Segoe UI", Font.BOLD, 14));
        playerBLabel = createDynamicLabel(player2Name, new Font("Segoe UI", Font.BOLD, 14));

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        center.setOpaque(false);

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
        scoreChip.setOpaque(true);
        scoreChip.setBackground(new Color(30, 55, 85));
        scoreChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        bottom.add(livesHeartsPanel, BorderLayout.WEST);
        bottom.add(scoreChip, BorderLayout.EAST);
        return bottom;
    }

    private JPanel createLegendPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        p.setOpaque(false);

        p.add(makeLegendItem(new LegendIconChip(ICON_QUESTION, new Color(140, 190, 255, 90)), "Question"));
        p.add(makeLegendItem(new LegendIconChip(ICON_SURPRISE, new Color(255, 170, 210, 90)), "Surprise"));
        p.add(makeLegendItem(new LegendIconChip(ICON_MINE,     new Color(255, 120, 120, 85)), "Mine"));

        return p;
    }

    private JPanel makeLegendItem(JComponent chip, String text) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        chip.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(text);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(ThemeManager.getInstance().getTextColor());

        box.add(chip);
        box.add(Box.createVerticalStrut(3));
        box.add(lbl);
        return box;
    }

    // ========== Overlay creation ==========

    private void createOverlay() {
        overlayCards = new CardLayout();

        overlayRoot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlayRoot.setOpaque(false);
        overlayRoot.setVisible(false);
        overlayRoot.setLayout(new OverlayLayout(overlayRoot));

        cardsHolder = new JPanel(overlayCards);
        cardsHolder.setOpaque(false);

        // Message card
        overlayMessageCard = new JPanel(new GridBagLayout());
        overlayMessageCard.setOpaque(false);

        OverlayCardPanel msgCard = new OverlayCardPanel();
        msgCard.setPreferredSize(new Dimension(380, 360));
        msgCard.setLayout(new BorderLayout());
        msgCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JButton closeBtn = new CloseIconButton();
        overlayCloseBtn = closeBtn;
        closeBtn.setToolTipText("Close");
        closeBtn.addActionListener(e -> hideOverlayNow());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(closeBtn, BorderLayout.EAST);
        msgCard.add(topRow, BorderLayout.NORTH);

        JPanel centerRow = new JPanel();
        centerRow.setOpaque(false);
        centerRow.setLayout(new BoxLayout(centerRow, BoxLayout.Y_AXIS));
        centerRow.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        overlayEmoji = new JLabel("", SwingConstants.CENTER);
        overlayEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlayEmoji.setPreferredSize(new Dimension(140, 140));
        overlayEmoji.setMaximumSize(new Dimension(140, 140));
        overlayEmoji.setOpaque(false);

        overlayTitle = new JLabel("", SwingConstants.CENTER);
        overlayTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        overlayTitle.setFont(new Font("Segoe UI", Font.BOLD, OVERLAY_TITLE_MAX));
        overlayTitle.setOpaque(false);

        overlaySub = new JTextPane();
        overlaySub.setFont(new Font("Segoe UI", Font.PLAIN, OVERLAY_SUB_SIZE));
        overlaySub.setEditable(false);
        overlaySub.setFocusable(false);
        overlaySub.setOpaque(false);

        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        overlaySub.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        overlaySub.setMaximumSize(new Dimension(340, 110));
        overlaySub.setPreferredSize(new Dimension(340, 110));

        centerRow.add(overlayEmoji);
        centerRow.add(Box.createVerticalStrut(10));
        centerRow.add(overlayTitle);
        centerRow.add(Box.createVerticalStrut(6));
        centerRow.add(overlaySub);

        msgCard.add(centerRow, BorderLayout.CENTER);
        overlayMessageCard.add(msgCard);

        // Pause card
        overlayPauseCard = new PauseMenuPanel(
                this::togglePauseFromGUI,
                () -> parent.startGame(player1Name, player2Name, session.getDifficulty()),
                parent::showMainMenu
        );

        cardsHolder.add(overlayMessageCard, "MSG");
        cardsHolder.add(overlayPauseCard, "PAUSE");

        // Gift overlay
        giftOverlay = new JPanel(new GridBagLayout());
        giftOverlay.setOpaque(false);
        giftOverlay.setVisible(false);

        giftLabel = new JLabel();
        giftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        giftLabel.setVerticalAlignment(SwingConstants.CENTER);
        giftOverlay.add(giftLabel);

        overlayRoot.add(cardsHolder);
        overlayRoot.add(giftOverlay);

        giftOverlay.setAlignmentX(0.5f);
        giftOverlay.setAlignmentY(0.5f);
        cardsHolder.setAlignmentX(0.5f);
        cardsHolder.setAlignmentY(0.5f);
    }

    // ========== Board build + Pause ==========

    // בונה רק את ה-grid של הכפתורים, בלי מסגרת זהב
    private JPanel buildBoardGridPanel(Board board, boolean firstBoard) {
        int rows = board.getRows();
        int cols = board.getCols();

        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setOpaque(false);

        // ⭐ רווח פנימי כדי שהקו הזהב יהיה מחוץ למשבצות
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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

        return panel; // בלי setBorder של הקו – זה ב-AnimatedBoardPanel
    }


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
    // ✅ פונקציה ציבורית כדי שה-controller יוכל לנעול/לשחרר לוחות
    public void setBoardsEnabled(boolean enabled) {
        setAllBoardsEnabled(enabled);
    }

    private void setBoardEnabled(CellButton[][] buttons, boolean enabled) {
        if (buttons == null) return;
        for (CellButton[] row : buttons) {
            for (CellButton b : row) b.setEnabled(enabled);
        }
    }

    // ========== UI Clock ==========

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

    private void showResultOverlayWithIcon(ImageIcon icon, String title, String subtitle, int seconds) {
        if (overlayRoot == null) return;

        overlayTitle.setText((title == null) ? "" : title.trim());
        fitOverlayTitleToWidth(overlayTitle.getText(), OVERLAY_TITLE_WIDTH, OVERLAY_TITLE_MAX, OVERLAY_TITLE_MIN);

        overlaySub.setText(((subtitle == null) ? "" : subtitle.trim()).replace(" | ", "\n"));
        overlaySub.setFont(new Font("Segoe UI", Font.PLAIN, OVERLAY_SUB_SIZE));

        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        if (overlayEmoji != null) {
            overlayEmoji.setText("");
            if (icon != null) overlayEmoji.setIcon(scaledIcon(icon, 140, 140));
            else overlayEmoji.setIcon(null);
        }

        if (overlayCards != null && cardsHolder != null) {
            overlayCards.show(cardsHolder, "MSG");
            cardsHolder.setVisible(true);
        }

        overlayRoot.setVisible(true);
        overlayRoot.repaint();

        if (overlayAutoHideTimer != null && overlayAutoHideTimer.isRunning()) overlayAutoHideTimer.stop();
        if (seconds > 0) {
            overlayAutoHideTimer = new Timer(seconds * 1000, e -> hideOverlayNow());
            overlayAutoHideTimer.setRepeats(false);
            overlayAutoHideTimer.start();
        }
    }

    public void showQuestionResultOverlay(OverlayType type,
                                          String title,
                                          String subtitle,
                                          int seconds) {

        overlayUsingQuestionTheme = true;
        OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);

        int s = 140;
        if (overlayEmoji != null) {
            if (type == OverlayType.GOOD) overlayEmoji.setIcon(scaledIcon(ICON_BLUE_GOOD, s, s));
            else if (type == OverlayType.BAD) overlayEmoji.setIcon(scaledIcon(ICON_BLUE_BAD, s, s));
            else overlayEmoji.setIcon(null);
        }

        showResultOverlay(type, title, subtitle, seconds);
    }
    /**
     * כמו showQuestionResultOverlay, אבל רק אחרי שההודעה נסגרת
     * הוא מפעיל callback (למשל endTurn).
     */
    public void showQuestionResultOverlayAndThen(OverlayType type,
                                                 String title,
                                                 String subtitle,
                                                 int seconds,
                                                 Runnable onDone) {

        overlayUsingQuestionTheme = true;
        OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);

        int s = 140;
        if (overlayEmoji != null) {
            if (type == OverlayType.GOOD) {
                overlayEmoji.setIcon(scaledIcon(ICON_BLUE_GOOD, s, s));
            } else if (type == OverlayType.BAD) {
                overlayEmoji.setIcon(scaledIcon(ICON_BLUE_BAD, s, s));
            } else {
                overlayEmoji.setIcon(null);
            }
        }

        // מציגים את האוברליי *בלי* טיימר פנימי (seconds = 0)
        // כדי שנשלוט אנחנו בזמן הסגירה.
        showResultOverlay(type, title, subtitle, 0);

        if (seconds <= 0) {
            if (onDone != null) onDone.run();
            return;
        }

        // אחרי X שניות – סוגרים את האוברליי ורק אז מפעילים endTurn
        javax.swing.Timer t = new javax.swing.Timer(seconds * 1000, e -> {
            hideOverlayNow();           // אותו helper פרטי שקיים כבר במחלקה
            if (onDone != null) onDone.run();
            ((javax.swing.Timer) e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }
    /**
     * מציג אוברליי רגיל ורק אחרי X שניות סוגר אותו ומריץ callback.
     * (ללא ערכת הצבע הכחולה של השאלות)
     */
    public void showResultOverlayAndThen(OverlayType type,
                                         String title,
                                         String subtitle,
                                         int seconds,
                                         Runnable onDone) {

        // נשתמש בערכת ברירת מחדל (ורודה)
        overlayUsingQuestionTheme = false;
        OverlayCardPanel.setTheme(OVERLAY_PINK_BG, OVERLAY_PINK_BORDER);

        // מציגים את האוברליי בלי טיימר פנימי
        showResultOverlay(type, title, subtitle, 0);

        if (seconds <= 0) {
            hideOverlayNow();
            if (onDone != null) onDone.run();
            return;
        }

        javax.swing.Timer t = new javax.swing.Timer(seconds * 1000, e -> {
            hideOverlayNow();
            if (onDone != null) onDone.run();
            ((javax.swing.Timer) e.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }



    public void showNotEnoughPointsOverlay(boolean isQuestionTile, int cost, int currentScore) {

        overlayUsingQuestionTheme = isQuestionTile;

        if (overlayUsingQuestionTheme) {
            OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);
        } else {
            OverlayCardPanel.setTheme(OVERLAY_PINK_BG, OVERLAY_PINK_BORDER);
        }

        int need = Math.max(0, cost - currentScore);

        String title = "NOT ENOUGH SCORE";
        String subtitle = "Need " + need + " pts to activate";

        ImageIcon sadIcon = overlayUsingQuestionTheme ? ICON_BLUE_BAD : ICON_BAD_HEART;

        fitOverlayTitleToWidth(title, 340, 30, 18);

        showResultOverlayWithIcon(sadIcon, title, subtitle, 2);
    }

    public void playGiftRevealAndShowOverlay(boolean isFirstBoard, int r, int c,
                                             OverlayType type,
                                             String title, String subtitle,
                                             int overlaySeconds) {

        if (buttons1 == null || buttons2 == null) return;
        CellButton[][] grid = isFirstBoard ? buttons1 : buttons2;

        if (r < 0 || c < 0 || r >= grid.length || c >= grid[0].length) return;

        CellButton btn = grid[r][c];
        if (btn == null) return;

        if (ICON_GIFT_CLOSED == null || ICON_GIFT_OPEN == null) {
            showResultOverlay(type, title, subtitle, overlaySeconds);
            return;
        }

        setAllBoardsEnabled(false);

        int cellSize = Math.min(btn.getWidth(), btn.getHeight());
        if (cellSize <= 0) cellSize = 48;
        cellSize = Math.max(22, cellSize - 8);

        ImageIcon closed = scaledIcon(ICON_GIFT_CLOSED, cellSize, cellSize);
        ImageIcon open   = scaledIcon(ICON_GIFT_OPEN,   cellSize, cellSize);

        btn.setText("");
        btn.setIcon(null);
        btn.setScaledIcon(closed);

        Timer t1 = new Timer(350, e1 -> {

            btn.setText("");
            btn.setIcon(null);
            btn.setScaledIcon(open);

            Timer t2 = new Timer(450, e2 -> {

                refreshView();
                updateTurnHighlight();

                showResultOverlay(type, title, subtitle, overlaySeconds);

                ((Timer) e2.getSource()).stop();
            });

            t2.setRepeats(false);
            t2.start();

            ((Timer) e1.getSource()).stop();
        });

        t1.setRepeats(false);
        t1.start();
    }

    public void playGiftCenterAndShowOverlay(OverlayType type,
            String title, String subtitle,
            int overlaySeconds,
            Runnable onDone) {

// 🔁 מקרה נפילה – אין משאבים, אין גיפט במרכז
if (overlayRoot == null || cardsHolder == null ||
giftOverlay == null || giftLabel == null ||
ICON_GIFT_CLOSED == null || ICON_GIFT_OPEN == null) {

// מציגים את האוברליי בלי טיימר פנימי
showResultOverlay(type, title, subtitle, 0);

if (onDone != null && overlaySeconds > 0) {
Timer t = new Timer(overlaySeconds * 1000, e -> {
hideOverlayNow();   // סוגר את האוברליי
onDone.run();       // endTurn → קו זהב לשחקן הבא
((Timer) e.getSource()).stop();
});
t.setRepeats(false);
t.start();
} else if (onDone != null) {
onDone.run();
}
return;
}

// נועל לוחות בזמן האנימציה
setAllBoardsEnabled(false);

cardsHolder.setVisible(false);
overlayRoot.setVisible(true);

int size = computeCenterGiftSize();
ImageIcon closed = scaledIcon(ICON_GIFT_CLOSED, size, size);
ImageIcon open   = scaledIcon(ICON_GIFT_OPEN,   size, size);

giftLabel.setHorizontalAlignment(SwingConstants.CENTER);
giftLabel.setVerticalAlignment(SwingConstants.CENTER);

giftLabel.setIcon(closed);
giftOverlay.setVisible(true);

overlayRoot.revalidate();
overlayRoot.repaint();

// שלב 1 – תיבת מתנה סגורה → פתוחה
Timer t1 = new Timer(350, e1 -> {
giftLabel.setIcon(open);
overlayRoot.repaint();

// שלב 2 – אחרי שהגיפט פתוח רגע, עוברים לאוברליי תוצאה
Timer t2 = new Timer(650, e2 -> {

giftOverlay.setVisible(false);
cardsHolder.setVisible(true);

// ⚠️ שימי לב: כאן אנחנו מציגים את האוברליי בלי טיימר פנימי
// (seconds = 0) כדי לשלוט בעצמנו מתי הוא נסגר.
showResultOverlay(type, title, subtitle, 0);

overlayRoot.revalidate();
overlayRoot.repaint();

// ⏲️ עכשיו טיימר חיצוני לסגירת האוברליי + מעבר תור
if (onDone != null && overlaySeconds > 0) {
Timer t3 = new Timer(overlaySeconds * 1000, e3 -> {
hideOverlayNow();   // סוגר את האוברליי
onDone.run();       // endTurn → updateTurnHighlight → קו זהב
((Timer) e3.getSource()).stop();
});
t3.setRepeats(false);
t3.start();
} else if (onDone != null) {
onDone.run();
}

((Timer) e2.getSource()).stop();
});

t2.setRepeats(false);
t2.start();

((Timer) e1.getSource()).stop();
});

t1.setRepeats(false);
t1.start();
}


    public void playGiftCenterAndShowOverlay(OverlayType type,
                                             String title, String subtitle,
                                             int overlaySeconds) {
        playGiftCenterAndShowOverlay(type, title, subtitle, overlaySeconds, null);
    }

    // ========== refresh boards ==========

    public void refreshView() {
        boolean p1Turn = (controller == null) || controller.isPlayer1Turn();

        ThemeManager tm = ThemeManager.getInstance();
        Color board1Color = tm.getBoardAColor();
        Color board2Color = tm.getBoardBColor();

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

        ThemeManager tm = ThemeManager.getInstance();
        Color baseColor = active ? playerColor : darker(playerColor, 0.6);

        Color glass = tm.isDarkMode()
                ? new Color(255, 255, 255, 45)
                : new Color(255, 255, 255, 35);

        Color mineGlass = tm.isDarkMode()
                ? new Color(255, 120, 120, 85)
                : new Color(255, 140, 140, 70);

        Color questionGlass = tm.isDarkMode()
                ? new Color(140, 190, 255, 90)
                : new Color(160, 205, 255, 75);

        Color surpriseGlass = tm.isDarkMode()
                ? new Color(255, 170, 210, 90)
                : new Color(255, 185, 220, 75);

        Color usedGlass = tm.isDarkMode()
                ? new Color(255, 255, 255, 30)
                : new Color(255, 255, 255, 25);

        Color textOnGlass = tm.getTextColor();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                Cell cell = board.getCell(r, c);
                CellButton btn = buttons[r][c];

                if (!cell.isRevealed()) {
                    btn.setFill(baseColor);
                    btn.setTextColor(Color.WHITE);

                    if (cell.isFlagged()) {
                        btn.setScaledIcon(ICON_FLAG);
                    } else {
                        btn.setIcon(null);
                        btn.setText("");
                    }
                    continue;
                }

                switch (cell.getType()) {

                    case MINE -> {
                        btn.setIcon(null);
                        btn.setText("");
                        btn.setFill(mineGlass);
                        btn.setScaledIcon(ICON_MINE);
                        btn.setTextColor(Color.WHITE);
                    }

                    case NUMBER -> {
                        btn.setIcon(null);
                        btn.setText(String.valueOf(cell.getAdjacentMines()));
                        btn.setFill(glass);
                        btn.setTextColor(textOnGlass);
                        btn.setFont(new Font("Segoe UI", Font.BOLD, btn.getFont().getSize()));
                    }

                    case EMPTY -> {
                        btn.setIcon(null);
                        btn.setText("");
                        btn.setFill(glass);
                        btn.setTextColor(textOnGlass);
                    }

                    case QUESTION -> {
                        if (cell.isPowerUsed()) {
                            btn.setIcon(null);
                            btn.setText("USED");
                            btn.setFill(usedGlass);
                            btn.setTextColor(textOnGlass);
                            btn.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, btn.getFont().getSize() - 6)));
                        } else {
                            btn.setIcon(null);
                            btn.setText("");
                            btn.setFill(questionGlass);
                            btn.setScaledIcon(ICON_QUESTION);
                            btn.setTextColor(Color.WHITE);
                        }
                    }

                    case SURPRISE -> {
                        if (cell.isPowerUsed()) {
                            btn.setIcon(null);
                            btn.setText("USED");
                            btn.setFill(usedGlass);
                            btn.setTextColor(textOnGlass);
                            btn.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, btn.getFont().getSize() - 6)));
                        } else {
                            btn.setIcon(null);
                            btn.setText("");
                            btn.setFill(surpriseGlass);
                            btn.setScaledIcon(ICON_SURPRISE);
                            btn.setTextColor(Color.WHITE);
                        }
                    }
                }
            }
        }
    }

    private Color darker(Color c, double factor) {
        return new Color(
                (int) (c.getRed() * factor),
                (int) (c.getGreen() * factor),
                (int) (c.getBlue() * factor)
        );
    }

    // ⭐ כאן הקסם של המסגרת הזהובה
    public void updateTurnHighlight() {
        boolean p1Active = (controller == null) || controller.isPlayer1Turn();
        boolean paused   = controller != null && controller.isPaused();

        if (paused) {
            setAllBoardsEnabled(false);
        } else {
            setBoardEnabled(buttons1, p1Active);
            setBoardEnabled(buttons2, !p1Active);
        }
        updateTurnIndicatorUI();

        // ⭐ טריגר לאנימציית פתיחת-תור (חד-פעמית)
        if (boardPanel1 != null) boardPanel1.setActive(p1Active);
        if (boardPanel2 != null) boardPanel2.setActive(!p1Active);
    }


    // ========== Game Over ==========

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

        String resultLabel = success ? "VICTORY" : "GAME OVER";
        String resultSub   = success ? "All mines revealed!" : "Out of shared lives!";
        String difficultyText = session.getDifficulty().name();

        GameHistory history = new GameHistory();
        history.addEntry(
                player1Name + " & " + player2Name,
                session.getScore(),
                difficultyText,
                success ? "All mines revealed" : "Out of lives",
                durationSeconds
        );

        Window owner = SwingUtilities.getWindowAncestor(this);
        GameOverDialog dialog = new GameOverDialog(
                owner,
                success,
                resultLabel,
                resultSub,
                player1Name + " & " + player2Name,
                session.getScore(),
                livesBefore,
                minesRevealed,
                difficultyText,
                durationSeconds,
                () -> parent.startGame(player1Name, player2Name, session.getDifficulty()),
                parent::showMainMenu
        );
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

    // ========== helpers / inner components ==========

    private enum OverlayStyle {
        POSITIVE(new Color(90, 200, 120)),
        NEGATIVE(new Color(235, 90, 90)),
        INFO(new Color(120, 170, 240));

        final Color accent;
        OverlayStyle(Color accent) { this.accent = accent; }

        static OverlayStyle fromTitle(String title) {
            String t = (title == null) ? "" : title.toUpperCase();
            if (t.contains("GOOD") || t.contains("CORRECT") || t.contains("SUCCESS")) return POSITIVE;
            if (t.contains("BAD") || t.contains("WRONG") || t.contains("NOT") || t.contains("NO ")) return NEGATIVE;
            return INFO;
        }
    }

    private static class OverlayCardPanel extends JPanel {

        private static Color THEME_BG     = new Color(255, 220, 240, 245);
        private static Color THEME_BORDER = new Color(150, 70, 170, 180);
        private static Color THEME_LINE   = new Color(160, 60, 190, 180);

        static void setTheme(Color bg, Color border) {
            if (bg != null) THEME_BG = bg;
            if (border != null) {
                THEME_BORDER = border;
                THEME_LINE = new Color(border.getRed(), border.getGreen(), border.getBlue(), 160);
            }
        }

        OverlayCardPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 12;

            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(10, 12, w - 16, h - 16, arc, arc);

            g2.setColor(THEME_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(THEME_BORDER);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.setColor(THEME_LINE);
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(24, h - 20, w - 24, h - 20);

            g2.dispose();
        }
    }

    private static class OverlayIcon extends JComponent {
        private OverlayStyle style = OverlayStyle.INFO;

        void setStyle(OverlayStyle style) {
            this.style = (style == null) ? OverlayStyle.INFO : style;
            repaint();
        }
        OverlayStyle getStyle() { return style; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillOval(2, 2, w - 4, h - 4);

            g2.setColor(new Color(style.accent.getRed(), style.accent.getGreen(), style.accent.getBlue(), 230));
            g2.setStroke(new BasicStroke(5f));
            g2.drawOval(4, 4, w - 8, h - 8);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            int cx = w / 2;
            int cy = h / 2;

            if (style == OverlayStyle.POSITIVE) {
                g2.drawLine(cx - 14, cy + 2, cx - 4, cy + 12);
                g2.drawLine(cx - 4, cy + 12, cx + 16, cy - 10);
            } else if (style == OverlayStyle.NEGATIVE) {
                g2.drawLine(cx - 14, cy - 14, cx + 14, cy + 14);
                g2.drawLine(cx + 14, cy - 14, cx - 14, cy + 14);
            } else {
                g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, cy - 10, cx, cy + 12);
                g2.fillOval(cx - 4, cy - 20, 8, 8);
            }

            g2.dispose();
        }
    }

    private static class CloseIconButton extends JButton {
        private boolean hover = false;
        private boolean down  = false;

        CloseIconButton() {
            setPreferredSize(new Dimension(36, 36));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ down = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (hover) {
                g2.setColor(new Color(255, 255, 255, down ? 70 : 40));
                g2.fillRoundRect(5, 5, w - 10, h - 10, 10, 10);
            }

            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.WHITE);

            int pad = 12;
            g2.drawLine(pad, pad, w - pad, h - pad);
            g2.drawLine(w - pad, pad, pad, h - pad);

            g2.dispose();
        }
    }

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

            g2.setColor(new Color(20, 35, 55, 220));
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            int d = Math.min(w, h) - 10;
            int cx = (w - d) / 2;
            int cy = (h - d) / 2;

            g2.setColor(new Color(220, 40, 40));
            g2.fillOval(cx, cy, d, d);

            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, d, d);

            g2.setColor(Color.BLACK);
            int innerX = cx + d / 3;
            int innerY = cy + d / 4;
            int barW = d / 8;
            int barH = d / 2;

            if (!paused) {
                g2.fillRoundRect(innerX, innerY, barW, barH, 3, 3);
                g2.fillRoundRect(innerX + barW * 2, innerY, barW, barH, 3, 3);
            } else {
                Polygon tri = new Polygon();
                tri.addPoint(innerX, innerY);
                tri.addPoint(innerX, innerY + barH);
                tri.addPoint(innerX + barW * 3, innerY + barH / 2);
                g2.fillPolygon(tri);
            }

            g2.dispose();
        }
    }

    private static class PauseMenuPanel extends JPanel {
        PauseMenuPanel(Runnable onResume, Runnable onRestart, Runnable onMenu) {
            setOpaque(false);
            setLayout(new GridBagLayout());

            boolean isDark = ThemeManager.getInstance().isDarkMode();

            Color boxBg = isDark ? new Color(15, 30, 50, 245) : new Color(255, 255, 255, 235);
            Color accentColor = isDark ? new Color(100, 200, 255) : new Color(40, 100, 180);
            Color borderColor = isDark ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 30);

            JPanel box = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(boxBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 30, 30);
                    g2.dispose();
                }
            };

            int boxWidth = 360;
            int boxHeight = 170;
            int resumeBtnW = 300;
            int resumeBtnH = 40;
            int midBtnW = 150;
            int midBtnH = 34;
            int emojiFontSize = 18;

            box.setPreferredSize(new Dimension(boxWidth, boxHeight));
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel paused = new JLabel("PAUSED ⏸️", SwingConstants.CENTER);
            paused.setAlignmentX(Component.CENTER_ALIGNMENT);
            paused.setFont(new Font("Segoe UI Emoji", Font.BOLD, emojiFontSize + 4));
            paused.setForeground(accentColor);
            paused.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            row.setOpaque(false);

            JButton restart = createCompactBtn("Restart 🔄", isDark, emojiFontSize - 4, midBtnW, midBtnH);
            JButton menu = createCompactBtn("Menu 🏠", isDark, emojiFontSize - 4, midBtnW, midBtnH);

            restart.addActionListener(e -> onRestart.run());
            menu.addActionListener(e -> onMenu.run());

            row.add(restart);
            row.add(menu);

            JButton resume = new JButton("RESUME ▶️");
            resume.setAlignmentX(Component.CENTER_ALIGNMENT);
            resume.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
            resume.setForeground(Color.WHITE);
            resume.setBackground(new Color(40, 160, 80));
            resume.setFocusPainted(false);
            resume.setCursor(new Cursor(Cursor.HAND_CURSOR));
            resume.setMaximumSize(new Dimension(resumeBtnW, resumeBtnH));
            resume.addActionListener(e -> onResume.run());

            box.add(paused);
            box.add(Box.createVerticalStrut(5));
            box.add(row);
            box.add(Box.createVerticalStrut(8));
            box.add(resume);

            add(box);
        }

        private JButton createCompactBtn(String text, boolean isDark, int fontSize, int w, int h) {
            JButton b = new JButton(text);
            b.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));
            b.setForeground(isDark ? Color.WHITE : Color.BLACK);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(w, h));
            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isDark ? new Color(255,255,255,60) : new Color(0,0,0,40), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
            return b;
        }
    }

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
            g2.setColor(ThemeManager.getInstance().getTextColor());

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

                if (filled) {
                    g2.setColor(ThemeManager.getInstance().getTextColor());
                    drawHeart(g2, hx, centerY - heartH / 2, heartW, heartH, true);
                } else {
                    Color emptyColor = ThemeManager.getInstance().isDarkMode()
                            ? new Color(200, 200, 200, 160)
                            : new Color(0, 0, 0, 100);
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

    // ⭐ מסגרת זהב מבריקה + אנימציית wavy
    // ⭐ מסגרת זהב מבריקה + גל וכוכביות
    // ⭐ פאנל לוח עם אנימציית "פתיחת תור":
    //    – קו זהב מבריק
    //    – כוכביות שיוצאות מהקו
    //    – גל אור שעובר על המשבצות
    // האנימציה רצה רק לכמה שניות בכל מעבר תור, ואז נעלמת.
    // ⭐ מסגרת זהב מבריקה + גל וכוכביות
    // פאנל הלוח עושה:
    // 1. קו זהב שמתחיל מהפינה ורץ סביב כל המסגרת.
    // 2. כוכבים קטנים על הקטע שכבר נפתח.
    // 3. גל אור חזק שעובר מעל המשבצות.
    // האנימציה רצה רק כשיש מעבר תור, ואז נעלמת.
    // ⭐ מסגרת זהב מבריקה – שני קווים שיוצאים מהפינה
    //    ועוד גל אור על המשבצות.
    private static class AnimatedBoardPanel extends JPanel {

        private boolean introPlaying = false;
        private long introStartMs = 0L;

        // משך האנימציה (קצר יותר)
        private static final long INTRO_DURATION_MS = 1200L;

        private float phase = 0f;
        private  Timer animTimer;

        AnimatedBoardPanel(JComponent content) {
            setLayout(new BorderLayout());
            setOpaque(false);
            add(content, BorderLayout.CENTER);

            animTimer = new Timer(40, e -> {
                if (!introPlaying) {
                    animTimer.stop();
                    return;
                }
                phase += 0.18f;
                if (phase > Math.PI * 2) {
                    phase -= (float) (Math.PI * 2);
                }

                long now = System.currentTimeMillis();
                if (now - introStartMs >= INTRO_DURATION_MS) {
                    stopIntro();
                }
                repaint();
            });
        }

        // נקרא מ-updateTurnHighlight
        void setActive(boolean isCurrent) {
            if (isCurrent) {
                startIntro();
            } else {
                stopIntro();
            }
        }

        private void startIntro() {
            introStartMs = System.currentTimeMillis();
            introPlaying = true;
            if (!animTimer.isRunning()) {
                animTimer.start();
            }
            repaint();
        }

        private void stopIntro() {
            introPlaying = false;
            if (animTimer.isRunning()) {
                animTimer.stop();
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!introPlaying) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int inset = 6;
            int x = inset;
            int y = inset;
            int w = getWidth() - 2 * inset - 1;
            int h = getHeight() - 2 * inset - 1;
            int arc = 22;

            // 0..1 – כמה רחוק האנימציה
            long now = System.currentTimeMillis();
            float progress = (float) (now - introStartMs) / (float) INTRO_DURATION_MS;
            progress = Math.max(0f, Math.min(1f, progress));

            // גל אור – ברור אבל לא ענק
            drawWaveBand(g2, x, y, w, h, arc, progress);

            // שני קווים שיוצאים מהפינה ונסגרים בפינה האלכסונית
            drawTwoWayGoldBorder(g2, x, y, w, h, progress);

            // כוכביות על הקווים
            drawSparklesOnTwoWays(g2, x, y, w, h, progress);

            g2.dispose();
        }

        // גל אור גבוה ~1/3 מהלוח
        private void drawWaveBand(Graphics2D g2,
                                  int x, int y, int w, int h,
                                  int arc,
                                  float progress) {

            int bandHeight = h / 3;
            int bandY = (int) (y - bandHeight + (h + bandHeight * 2) * progress);

            Shape oldClip = g2.getClip();
            Shape boardShape = new java.awt.geom.RoundRectangle2D.Float(
                    x, y, w, h, arc, arc);
            g2.setClip(boardShape);

            Color waveTop    = new Color(255, 255, 255, 60);
            Color waveMid    = new Color(255, 255, 210, 210);
            Color waveBottom = new Color(255, 255, 255, 50);

            GradientPaint gp1 = new GradientPaint(
                    x, bandY,
                    waveTop,
                    x, bandY + bandHeight / 2,
                    waveMid, true);
            g2.setPaint(gp1);
            g2.fillRect(x, bandY, w, bandHeight / 2);

            GradientPaint gp2 = new GradientPaint(
                    x, bandY + bandHeight / 2,
                    waveMid,
                    x, bandY + bandHeight,
                    waveBottom, true);
            g2.setPaint(gp2);
            g2.fillRect(x, bandY + bandHeight / 2, w, bandHeight / 2);

            g2.setClip(oldClip);
        }

        // שני קווים: אחד עם כיוון השעון, אחד נגד, שניהם יוצאים מהפינה
        // ונפגשים בפינה האלכסונית.
        private void drawTwoWayGoldBorder(Graphics2D g2,
                                          int x, int y, int w, int h,
                                          float progress) {

            float perimeter = 2f * (w + h);
            float halfPerimeter = w + h;       // מרחק מפינה לפינה האלכסונית

            float len = halfPerimeter * progress; // אורך כל קו

            g2.setStroke(new BasicStroke(4f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 215, 0, 235));

            // קו ראשון: מתחיל ב־t=0 ומתקדם עם כיוון השעון
            drawBorderRange(g2, x, y, w, h, 0f, len);

            // קו שני: מתחיל גם מהפינה, אבל נגד כיוון השעון
            float perimeterStart = perimeter - len;
            drawBorderRange(g2, x, y, w, h, perimeterStart, perimeter);
        }

        // מצייר חלק מההיקף בין tStart ל-tEnd (0..perimeter)
        private void drawBorderRange(Graphics2D g2,
                                     int x, int y, int w, int h,
                                     float tStart, float tEnd) {

            float perimeter = 2f * (w + h);
            tStart = Math.max(0f, Math.min(perimeter, tStart));
            tEnd   = Math.max(0f, Math.min(perimeter, tEnd));
            if (tEnd <= tStart) return;

            // קטעים: עליון, ימני, תחתון, שמאלי
            float[] edgeLen = { w, h, w, h };
            float[] edgeStart = new float[4];
            edgeStart[0] = 0;
            for (int i = 1; i < 4; i++) {
                edgeStart[i] = edgeStart[i - 1] + edgeLen[i - 1];
            }

            for (int edge = 0; edge < 4; edge++) {
                float segStart = edgeStart[edge];
                float segEnd = segStart + edgeLen[edge];

                float a = Math.max(tStart, segStart);
                float b = Math.min(tEnd, segEnd);
                if (b <= a) continue;

                int x1, y1, x2, y2;

                switch (edge) {
                    case 0 -> { // top: left→right
                        x1 = x + Math.round(a - segStart);
                        y1 = y;
                        x2 = x + Math.round(b - segStart);
                        y2 = y;
                    }
                    case 1 -> { // right: top→bottom
                        x1 = x + w;
                        y1 = y + Math.round(a - segStart);
                        x2 = x + w;
                        y2 = y + Math.round(b - segStart);
                    }
                    case 2 -> { // bottom: right→left
                        x1 = x + w - Math.round(a - segStart);
                        y1 = y + h;
                        x2 = x + w - Math.round(b - segStart);
                        y2 = y + h;
                    }
                    default -> { // left: bottom→top
                        x1 = x;
                        y1 = y + h - Math.round(a - segStart);
                        x2 = x;
                        y2 = y + h - Math.round(b - segStart);
                    }
                }
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // כוכביות על שני הכיוונים של הקו
        private void drawSparklesOnTwoWays(Graphics2D g2,
                                           int x, int y, int w, int h,
                                           float progress) {

            float perimeter = 2f * (w + h);
            float halfPerimeter = w + h;
            float len = halfPerimeter * progress;
            if (len <= 0f) return;

            int starCount = 16;

            for (int i = 0; i < starCount; i++) {
                // חצי כוכבים על הקו עם כיוון השעון
                float t;
                if (i < starCount / 2) {
                    float ratio = i / (float) (starCount / 2);
                    t = len * ratio;
                } else { // חצי שני על הקו הנגדי
                    float ratio = (i - starCount / 2) / (float) (starCount / 2);
                    t = perimeter - len + len * ratio;
                }

                Point p = pointOnRectPerimeter(x, y, w, h, t, perimeter);

                float alpha = 0.4f + 0.6f *
                        (float) Math.abs(Math.sin(phase * 1.4f + i));
                g2.setColor(new Color(255, 255, 210, (int) (alpha * 255)));

                int size = 5;
                int cx = p.x;
                int cy = p.y;

                g2.setStroke(new BasicStroke(1.4f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - size, cy, cx + size, cy);
                g2.drawLine(cx, cy - size, cx, cy + size);
            }
        }

        // נקודה על היקף הריבוע לפי אורך t
        private Point pointOnRectPerimeter(int x, int y, int w, int h,
                                           float t, float perimeter) {

            float d = t % perimeter;

            // עליון
            if (d <= w) {
                return new Point(x + Math.round(d), y);
            }
            d -= w;

            // ימני
            if (d <= h) {
                return new Point(x + w, y + Math.round(d));
            }
            d -= h;

            // תחתון
            if (d <= w) {
                return new Point(x + w - Math.round(d), y + h);
            }
            d -= w;

            // שמאלי
            return new Point(x, y + h - Math.round(d));
        }
    }




}
