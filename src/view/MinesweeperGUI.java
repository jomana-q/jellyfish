package view;

import controller.MinesweeperController;
import model.*;
import model.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;

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
    private JButton overlayCloseBtn;

    private Timer overlayAutoHideTimer;

    // כותרות לוחות (נוספו)
    private JLabel boardALabel;
    private JLabel boardBLabel;

    // Overlay theme state (used for Question result overlay)
    private boolean overlayUsingQuestionTheme = false;

    // Default (Surprise / pink) theme
    private static final Color OVERLAY_PINK_BG     = new Color(255, 220, 240, 245);
    private static final Color OVERLAY_PINK_BORDER = new Color(150, 70, 170, 180);

    // Question (blue) theme
    private static final Color OVERLAY_BLUE_BG     = new Color(220, 240, 255, 245);
    private static final Color OVERLAY_BLUE_BORDER = new Color(70, 140, 210, 180);

    public enum OverlayType { GOOD, BAD, INFO }

    // Used icons (after activation)
    private final ImageIcon ICON_QUESTION_USED = loadIcon("/images/question_used.png");
    private final ImageIcon ICON_SURPRISE_USED = loadIcon("/images/gift_open.png");

    // Overlay font sizing
    private static final int OVERLAY_TITLE_MAX = 24;
    private static final int OVERLAY_TITLE_MIN = 16;
    private static final int OVERLAY_TITLE_WIDTH = 340;
    private static final int OVERLAY_SUB_SIZE = 15;

    // Game fields
    private final Board board1;
    private final Board board2;
    private final GameSession session;
    private final Difficulty difficulty;

    private final String player1Name;
    private final String player2Name;

    private final MainMenuGUI parent;
    private MinesweeperController controller;

    private CellButton[][] buttons1;
    private CellButton[][] buttons2;

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
    private JLabel minesLeftALabel;
    private JLabel minesLeftBLabel;
    private JPanel boardPanelA;
    private JPanel boardPanelB;
    private JPanel boardWrapA;
    private JPanel boardWrapB;

    // Gift overlay (full screen)
    private JPanel cardsHolder;
    private JPanel giftOverlay;
    private JLabel giftLabel;

    // Toast (small side message)
    private JPanel toastPanel;
    private JLabel toastLabel;
    private Timer toastTimer;

    // Icons (loaded from /images/)
    private final ImageIcon ICON_QUESTION  = loadIcon("/images/Ques.png");
    private final ImageIcon ICON_SURPRISE  = loadIcon("/images/surprise.png");
    private final ImageIcon ICON_MINE      = loadIcon("/images/Boom.png");
    private final ImageIcon ICON_FLAG      = loadIcon("/images/flag.png");
    // Gift icons (loaded from /images/)
    private final ImageIcon ICON_GIFT_CLOSED = loadIcon("/images/gift_closed.png");
    private final ImageIcon ICON_GIFT_OPEN   = loadIcon("/images/gift_open.png");
    // Overlay emoji icons (loaded from /images/)
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
        this.difficulty = session.getDifficulty();

        initUI();
        refreshView();
    }

    public void setController(MinesweeperController controller) {
        this.controller = controller;
        this.controller.startGameTimer();
        startUiClock();
        updateTurnHighlight();
        refreshView();
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Missing icon resource: " + path);
            return null;
        }
        return new ImageIcon(url);
    }

    // helper: scale any ImageIcon to fixed size
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

    // helper: compute a good center-gift size based on overlay size
    private int computeCenterGiftSize() {
        int w = (overlayRoot != null) ? overlayRoot.getWidth() : 0;
        int h = (overlayRoot != null) ? overlayRoot.getHeight() : 0;

        if (w <= 0 || h <= 0) return 260; // fallback

        int base = Math.min(w, h);
        int size = (int) (base * 0.45);   // 45% מהמסך
        return Math.max(180, Math.min(size, 420)); // clamp
    }

    // Overlay API
    public void showResultOverlay(OverlayType type, String title, String subtitle, int seconds) {
        if (overlayRoot == null) return;

        String safeTitle = (title == null) ? "" : title.trim();
        String safeSub   = (subtitle == null) ? "" : subtitle.trim();

        overlayTitle.setText(safeTitle);
        fitOverlayTitleToWidth(safeTitle, OVERLAY_TITLE_WIDTH, OVERLAY_TITLE_MAX, OVERLAY_TITLE_MIN);

        overlaySub.setText(safeSub.replace(" | ", "\n"));
        overlaySub.setFont(new Font("Segoe UI", Font.PLAIN, OVERLAY_SUB_SIZE));

        if (overlayUsingQuestionTheme) {
            overlayTitle.setForeground(new Color(18, 55, 105));   // Blue (Question)
        } else {
            overlayTitle.setForeground(new Color(120, 45, 160));  // Purple (Surprise)
        }

        // Keep center alignment
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        // Apply theme BEFORE showing
        if (overlayUsingQuestionTheme) {
            OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);
        } else {
            OverlayCardPanel.setTheme(OVERLAY_PINK_BG, OVERLAY_PINK_BORDER);
        }

        // Pick icon based on theme + type
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

    // UI setup
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

                if (toastPanel != null) {
                    int tw = toastPanel.getWidth();
                    int th = toastPanel.getHeight();
                    if (tw <= 0 || th <= 0) { // safety (first layout)
                        Dimension d = toastPanel.getPreferredSize();
                        if (d != null) { tw = d.width; th = d.height; }
                    }
                    int x = w - tw - 20;
                    int y = 90;
                    toastPanel.setBounds(x, y, tw, th);
                }
            }
        };
        layeredPane.setLayout(null);

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        createOverlay();
        layeredPane.add(overlayRoot, JLayeredPane.PALETTE_LAYER);

        createToast();
        layeredPane.add(toastPanel, JLayeredPane.POPUP_LAYER);

        add(layeredPane, BorderLayout.CENTER);
    }

    public void showToast(String msg) {
        showToast(msg, 1600); // default 1.6s
    }

    public void showToast(String msg, int millis) {
        if (toastPanel == null || toastLabel == null) return;

        String text = (msg == null) ? "" : msg;
        toastLabel.setText(text);

        // --- auto width based on text ---
        FontMetrics fm = toastLabel.getFontMetrics(toastLabel.getFont());
        int textW = fm.stringWidth(text);

        int minW = 240;
        int maxW = 520;
        int padding = 28;

        int w = Math.min(maxW, Math.max(minW, textW + padding));

        // height
        int h = 44;

        toastPanel.setPreferredSize(new Dimension(w, h));
        toastPanel.setSize(new Dimension(w, h));
        toastPanel.revalidate();

        if (layeredPane != null) layeredPane.doLayout();

        toastPanel.setVisible(true);
        toastPanel.repaint();

        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        toastTimer = new Timer(Math.max(300, millis), e -> {
            toastPanel.setVisible(false);
            toastPanel.repaint();
            ((Timer) e.getSource()).stop();
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private JPanel buildMainPanel() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setOpaque(false);
        int pad = (difficulty == Difficulty.HARD) ? 12 : 20;
        root.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        header.add(buildPlayersRow());
        header.add(Box.createVerticalStrut(6));
        header.add(buildBoardsTitleRow());

        root.add(header, BorderLayout.NORTH);

        int gap = (difficulty == Difficulty.HARD) ? 16 : 28;
        JPanel boardsContainer = new JPanel(new GridLayout(1, 2, gap, 0));
        boardsContainer.setOpaque(false);

        boardPanelA = buildSingleBoardPanel(board1, true);
        boardPanelB = buildSingleBoardPanel(board2, false);

        boardWrapA = new BoardMattePanel(boardPanelA);
        boardWrapB = new BoardMattePanel(boardPanelB);

        boardsContainer.add(boardWrapA);
        boardsContainer.add(boardWrapB);

        root.add(boardsContainer, BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildPlayersRow() {

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel centerRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerRow.setOpaque(false);

        turnLabel = createDynamicLabel("Turn: " + player1Name, new Font("Segoe UI", Font.BOLD, 18));
        timeLabel = createDynamicLabel("Time: 00:00", new Font("Segoe UI", Font.BOLD, 18));

        pauseBtn = new PauseIconButton();
        pauseBtn.setToolTipText("Pause / Resume");
        pauseBtn.addActionListener(e -> togglePauseFromGUI());

        // Info button (Rules)
        JButton infoBtn = new InfoIconButton();

        infoBtn.addActionListener(e -> {
            Difficulty currentDiff = (session != null) ? session.getDifficulty() : Difficulty.EASY;
            new view.GameRulesDialog(
                    SwingUtilities.getWindowAncestor(this),
                    currentDiff
            ).setVisible(true);
        });

        centerRow.add(turnLabel);
        centerRow.add(Box.createHorizontalStrut(18));
        centerRow.add(timeLabel);
        centerRow.add(Box.createHorizontalStrut(10));
        centerRow.add(infoBtn);
        centerRow.add(Box.createHorizontalStrut(6));
        centerRow.add(pauseBtn);

        center.add(centerRow);

        row.add(center, BorderLayout.CENTER);

        return row;
    }

    private JPanel buildBoardsTitleRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        p1Indicator = new TurnIndicator();
        p2Indicator = new TurnIndicator();

        boardALabel = createDynamicLabel(player1Name + "'s Board", new Font("Segoe UI", Font.BOLD, 16));
        boardBLabel = createDynamicLabel(player2Name + "'s Board", new Font("Segoe UI", Font.BOLD, 16));

        minesLeftALabel = createDynamicLabel("Mines left: 0", new Font("Segoe UI", Font.PLAIN, 14));
        minesLeftBLabel = createDynamicLabel("Mines left: 0", new Font("Segoe UI", Font.PLAIN, 14));
        minesLeftALabel.setForeground(new Color(220, 220, 220));
        minesLeftBLabel.setForeground(new Color(220, 220, 220));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(p1Indicator);
        leftPanel.add(boardALabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(boardBLabel);
        rightPanel.add(p2Indicator);

        JPanel p1Stack = new JPanel(new BorderLayout());
        p1Stack.setOpaque(false);
        p1Stack.add(leftPanel, BorderLayout.NORTH);

        JPanel p1MinesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 35, 0));
        p1MinesPanel.setOpaque(false);
        p1MinesPanel.add(minesLeftALabel);

        p1Stack.add(p1MinesPanel, BorderLayout.SOUTH);

        JPanel p2Stack = new JPanel(new BorderLayout());
        p2Stack.setOpaque(false);
        p2Stack.add(rightPanel, BorderLayout.NORTH);

        JPanel p2MinesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 35, 0));
        p2MinesPanel.setOpaque(false);
        p2MinesPanel.add(minesLeftBLabel);

        p2Stack.add(p2MinesPanel, BorderLayout.SOUTH);

        JPanel legend = createLegendPanel();

        row.add(p1Stack, BorderLayout.WEST);
        row.add(legend, BorderLayout.CENTER);
        row.add(p2Stack, BorderLayout.EAST);

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

        int gap = 18;

        JPanel grid = new JPanel(new GridLayout(1, 5, gap, 0));
        grid.setOpaque(false);

        grid.add(makeLegendItem(new LegendIconChip(ICON_QUESTION,      new Color(140, 190, 255, 90)), "Question"));
        grid.add(makeLegendItem(new LegendIconChip(ICON_SURPRISE,      new Color(255, 170, 210, 90)), "Surprise"));
        grid.add(makeLegendItem(new LegendIconChip(ICON_MINE,          new Color(255, 120, 120, 85)), "Mine"));
        grid.add(makeLegendItem(new LegendIconChip(ICON_QUESTION_USED, new Color(140, 190, 255, 90)), "Used Question"));
        grid.add(makeLegendItem(new LegendIconChip(ICON_SURPRISE_USED, new Color(255, 170, 210, 90)), "Used Surprise"));

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.add(grid);

        return wrap;
    }

    private JPanel makeLegendItem(JComponent chip, String text) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        box.setPreferredSize(new Dimension(78, 52));
        box.setMinimumSize(new Dimension(78, 52));
        box.setMaximumSize(new Dimension(78, 52));

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

    // Overlay creation
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

        // Cards holder
        cardsHolder = new JPanel(overlayCards);
        cardsHolder.setOpaque(false);

        // Message card
        overlayMessageCard = new JPanel(new GridBagLayout());
        overlayMessageCard.setOpaque(false);

        OverlayCardPanel msgCard = new OverlayCardPanel();
        msgCard.setPreferredSize(new Dimension(380, 360));
        msgCard.setLayout(new BorderLayout());
        msgCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        overlayCloseBtn = new CloseIconButton();
        overlayCloseBtn.setToolTipText("Close");
        overlayCloseBtn.addActionListener(e -> hideOverlayNow());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(overlayCloseBtn, BorderLayout.EAST);
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

        // Subtitle as JTextPane with real center alignment
        overlaySub = new JTextPane();
        overlaySub.setFont(new Font("Segoe UI", Font.PLAIN, OVERLAY_SUB_SIZE));
        overlaySub.setEditable(false);
        overlaySub.setFocusable(false);
        overlaySub.setOpaque(false);

        // Center align text (only once!)
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        overlaySub.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        overlaySub.setMaximumSize(new Dimension(340, 110));
        overlaySub.setPreferredSize(new Dimension(340, 110));

        // Build center
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

        // Order matters in OverlayLayout:
        overlayRoot.add(cardsHolder);
        overlayRoot.add(giftOverlay);

        giftOverlay.setAlignmentX(0.5f);
        giftOverlay.setAlignmentY(0.5f);
        cardsHolder.setAlignmentX(0.5f);
        cardsHolder.setAlignmentY(0.5f);
    }

    private void createToast() {
        toastPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                // glass background
                g2.setColor(new Color(10, 20, 35, 190));
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                // subtle border
                g2.setColor(new Color(255, 255, 255, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);

                g2.dispose();
            }
        };
        toastPanel.setOpaque(false);
        toastPanel.setLayout(new BorderLayout());
        toastPanel.setVisible(false);

        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        toastPanel.add(toastLabel, BorderLayout.CENTER);
        toastPanel.setPreferredSize(new Dimension(260, 44));
    }

    // Board build + Pause
    private JPanel buildSingleBoardPanel(Board board, boolean firstBoard) {
        int rows = board.getRows();
        int cols = board.getCols();

        JPanel panel = new JPanel(new GridLayout(rows, cols));
        panel.setOpaque(false);

        int cell = switch (difficulty) {
            case EASY -> 46;
            case MEDIUM -> 34;
            case HARD -> 26;
        };

        CellButton[][] buttons = new CellButton[rows][cols];

        int fontSize = 24;
        if (rows > 15) fontSize = 14;
        else if (rows > 10) fontSize = 18;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellButton btn = new CellButton();
                btn.setFocusPainted(false);
                btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));

                btn.setPreferredSize(new Dimension(cell, cell));
                btn.setMinimumSize(new Dimension(cell, cell));
                btn.setMaximumSize(new Dimension(cell, cell));

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

        panel.setBorder(BorderFactory.createEmptyBorder());

        return panel;
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

    // ⭐ מתודה ציבורית לשימוש מה־controller שלך (נוספה)
    public void setBoardsEnabled(boolean enabled) {
        setAllBoardsEnabled(enabled);
    }

    private void setBoardEnabled(CellButton[][] buttons, boolean enabled) {
        if (buttons == null) return;
        for (CellButton[] row : buttons) {
            for (CellButton b : row) b.setEnabled(enabled);
        }
    }

    // UI Clock
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

        // keep center alignment
        javax.swing.text.SimpleAttributeSet center = new javax.swing.text.SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        overlaySub.setParagraphAttributes(center, true);

        if (overlayEmoji != null) {
            overlayEmoji.setText("");
            if (icon != null) {
                overlayEmoji.setIcon(scaledIcon(icon, 140, 140));
            } else {
                overlayEmoji.setIcon(null);
            }
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
            if (type == OverlayType.GOOD) {
                overlayEmoji.setIcon(scaledIcon(ICON_BLUE_GOOD, s, s));
            } else if (type == OverlayType.BAD) {
                overlayEmoji.setIcon(scaledIcon(ICON_BLUE_BAD, s, s));
            } else {
                overlayEmoji.setIcon(null);
            }
        }

        showResultOverlay(type, title, subtitle, seconds);
    }

    // ⭐ מתודה חדשה עם callback – כמו בגרסה שלך
    public void showQuestionResultOverlayAndThen(
            OverlayType type,
            String title,
            String subtitle,
            int seconds,
            Runnable onDone) {

        overlayUsingQuestionTheme = true;
        OverlayCardPanel.setTheme(OVERLAY_BLUE_BG, OVERLAY_BLUE_BORDER);

        // מציגים את האוברליי בלי טיימר פנימי
        showResultOverlay(type, title, subtitle, 0);

        // אחרי X שניות – סוגרים ומריצים onDone
        if (onDone != null && seconds > 0) {
            Timer t = new Timer(seconds * 1000, e -> {
                hideOverlayNow();
                onDone.run();
                ((Timer) e.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        } else if (onDone != null) {
            onDone.run();
        }
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

        // אם אין אייקונים - פשוט מציג Overlay בלי אנימציה
        if (ICON_GIFT_CLOSED == null || ICON_GIFT_OPEN == null) {
            showResultOverlay(type, title, subtitle, overlaySeconds);
            return;
        }

        setAllBoardsEnabled(false);

        //  קובעים גודל אחיד לפי גודל הכפתור
        int cellSize = Math.min(btn.getWidth(), btn.getHeight());
        if (cellSize <= 0) cellSize = 48;   // fallback אם עוד לא חושב layout
        cellSize = Math.max(22, cellSize - 8);

        ImageIcon closed = scaledIcon(ICON_GIFT_CLOSED, cellSize, cellSize);
        ImageIcon open   = scaledIcon(ICON_GIFT_OPEN,   cellSize, cellSize);

        // מצב 1: מתנה סגורה
        btn.setText("");
        btn.setIcon(null);
        btn.setScaledIcon(closed);

        Timer t1 = new Timer(350, e1 -> {

            // מצב 2: מתנה פתוחה
            btn.setText("");
            btn.setIcon(null);
            btn.setScaledIcon(open);

            Timer t2 = new Timer(450, e2 -> {

                // מצב 3: החזרת מצב UI לפי המודל + הצגת Overlay
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

        if (overlayRoot == null || cardsHolder == null ||
                giftOverlay == null || giftLabel == null ||
                ICON_GIFT_CLOSED == null || ICON_GIFT_OPEN == null) {

            showResultOverlay(type, title, subtitle, overlaySeconds);
            if (onDone != null) SwingUtilities.invokeLater(onDone);
            return;
        }

        setAllBoardsEnabled(false);

        //  מסתירים את ההודעה/הכרטיס בזמן האנימציה
        cardsHolder.setVisible(false);

        //  מציגים רק רקע כהה + מתנה
        overlayRoot.setVisible(true);

        //  אותו גודל לשני האייקונים
        int size = computeCenterGiftSize();
        ImageIcon closed = scaledIcon(ICON_GIFT_CLOSED, size, size);
        ImageIcon open   = scaledIcon(ICON_GIFT_OPEN,   size, size);

        giftLabel.setHorizontalAlignment(SwingConstants.CENTER);
        giftLabel.setVerticalAlignment(SwingConstants.CENTER);

        giftLabel.setIcon(closed);
        giftOverlay.setVisible(true);

        overlayRoot.revalidate();
        overlayRoot.repaint();

        Timer t1 = new Timer(350, e1 -> {
            giftLabel.setIcon(open);
            overlayRoot.repaint();

            Timer t2 = new Timer(650, e2 -> {

                // מסתירים מתנה
                giftOverlay.setVisible(false);

                // עכשיו מציגים הודעה
                cardsHolder.setVisible(true);
                showResultOverlay(type, title, subtitle, overlaySeconds);

                // מחזירים UI
                refreshView();
                updateTurnHighlight();

                if (onDone != null) SwingUtilities.invokeLater(onDone);

                ((Timer) e2.getSource()).stop();
            });

            t2.setRepeats(false);
            t2.start();

            ((Timer) e1.getSource()).stop();
        });

        t1.setRepeats(false);
        t1.start();
    }

    // Overload without callback
    public void playGiftCenterAndShowOverlay(OverlayType type,
                                             String title, String subtitle,
                                             int overlaySeconds) {
        playGiftCenterAndShowOverlay(type, title, subtitle, overlaySeconds, null);
    }

    private int countTotalMines(Board b) {
        int count = 0;
        for (int r = 0; r < b.getRows(); r++) {
            for (int c = 0; c < b.getCols(); c++) {
                if (b.getCell(r, c).getType() == CellType.MINE) count++;
            }
        }
        return count;
    }

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

        // Mines left
        int totalMinesA = countTotalMines(board1);
        int totalMinesB = countTotalMines(board2);
        int revealedA   = countRevealedMines(board1);
        int revealedB   = countRevealedMines(board2);

        int minesLeftA = Math.max(0, totalMinesA - revealedA);
        int minesLeftB = Math.max(0, totalMinesB - revealedB);

        if (minesLeftALabel != null) minesLeftALabel.setText("Mines left: " + minesLeftA);
        if (minesLeftBLabel != null) minesLeftBLabel.setText("Mines left: " + minesLeftB);

        updateTurnIndicatorUI();
        applyBoardHighlight(p1Turn);
    }

    private void updateTurnIndicatorUI() {
        boolean p1Turn = (controller == null) || controller.isPlayer1Turn();
        p1Indicator.setActive(p1Turn);
        p2Indicator.setActive(!p1Turn);
    }

    // Refresh
    private void updateBoardView(Board board, CellButton[][] buttons, Color playerColor, boolean active) {
        if (buttons == null) return;

        int rows = board.getRows();
        int cols = board.getCols();

        ThemeManager tm = ThemeManager.getInstance();
        Color baseColor = active ? playerColor : darker(playerColor, 0.6);

        // Glass look (light + transparent)
        Color glass = tm.isDarkMode()
                ? new Color(255, 255, 255, 45)
                : new Color(255, 255, 255, 35);

        // Mine: light red / transparent
        Color mineGlass = tm.isDarkMode()
                ? new Color(255, 120, 120, 85)
                : new Color(255, 140, 140, 70);

        // Question: light blue / transparent
        Color questionGlass = tm.isDarkMode()
                ? new Color(140, 190, 255, 90)
                : new Color(160, 205, 255, 75);

        // Surprise: light pink / transparent
        Color surpriseGlass = tm.isDarkMode()
                ? new Color(255, 170, 210, 90)
                : new Color(255, 185, 220, 75);

        // Used power: softer glass
        Color usedGlass = tm.isDarkMode()
                ? new Color(255, 255, 255, 30)
                : new Color(255, 255, 255, 25);

        Color textOnGlass = tm.getTextColor();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                Cell cell = board.getCell(r, c);
                CellButton btn = buttons[r][c];

                // NOT revealed
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

                // revealed
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
                            btn.setText("");
                            btn.setFill(usedGlass);
                            btn.setTextColor(textOnGlass);

                            // show broken question icon (instead of USED)
                            btn.setScaledIcon(ICON_QUESTION_USED);

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
                            btn.setText("");
                            btn.setFill(usedGlass);
                            btn.setTextColor(textOnGlass);

                            // show opened gift icon (instead of USED)
                            btn.setScaledIcon(ICON_SURPRISE_USED);

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

    private void applyBoardHighlight(boolean p1Active) {
        if (boardWrapA == null || boardWrapB == null) return;

        ThemeManager tm = ThemeManager.getInstance();
        Color a = tm.getBoardAColor();
        Color b = tm.getBoardBColor();

        Border normal = BorderFactory.createLineBorder(
                tm.isDarkMode() ? new Color(255,255,255,55) : new Color(0,0,0,45),
                2, true
        );

        Border glowA = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(a.getRed(), a.getGreen(), a.getBlue(), 230), 6, true),
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true)
        );

        Border glowB = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(b.getRed(), b.getGreen(), b.getBlue(), 230), 6, true),
                BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true)
        );

        boardWrapA.setBorder(p1Active ? glowA : normal);
        boardWrapB.setBorder(p1Active ? normal : glowB);

        boardWrapA.repaint();
        boardWrapB.repaint();
    }

    public void updateTurnHighlight() {
        boolean p1Active = (controller == null) || controller.isPlayer1Turn();
        boolean paused = controller != null && controller.isPaused();

        if (paused) {
            setAllBoardsEnabled(false);
        } else {
            setAllBoardsEnabled(true); // לא מכבים אף לוח – זה הקוד של החברות שלך
        }

        applyBoardHighlight(p1Active);
        updateTurnIndicatorUI();
    }

    // Game Over
    public void showGameOver(boolean success) {
        if (uiClockTimer != null && uiClockTimer.isRunning()) uiClockTimer.stop();

        int livesBefore = session.getLives();
        int minesRevealed = countRevealedMines(board1) + countRevealedMines(board2);
        int durationSeconds = (controller == null) ? 0 : (int) (controller.getElapsedActiveMillis() / 1000);

        // convert lives -> score AFTER saving livesBefore
        session.convertRemainingLivesToScoreAtEnd();

        board1.revealAllCells();
        board2.revealAllCells();
        refreshView();
        setAllBoardsEnabled(false);

        String resultLabel = success ? "VICTORY" : "GAME OVER";
        String resultSub   = success ? "All mines revealed!" : "Out of shared lives!";
        String difficultyText = session.getDifficulty().name();

        // Save history
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

    // Helpers / Components
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
                // line color derived from border (nicer + matches theme)
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

            // Shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(10, 12, w - 16, h - 16, arc, arc);

            // Glass bg
            g2.setColor(THEME_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // Border
            g2.setColor(THEME_BORDER);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            // Bottom line (theme)
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

            // subtle hover highlight (rounded square, not circle)
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

            g2.setColor(new Color(120, 100, 230));
            g2.fillOval(cx, cy, d, d);

            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, d, d);

            g2.setColor(Color.WHITE);

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

            // בדיקת המצב הנוכחי מה-ThemeManager
            boolean isDark = ThemeManager.getInstance().isDarkMode();

            // צבעים מותאמים אישית למראה זכוכית
            Color boxBg = isDark ? new Color(15, 30, 50, 245) : new Color(255, 255, 255, 235);
            Color accentColor = isDark ? new Color(100, 200, 255) : new Color(40, 100, 180);
            Color borderColor = isDark ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 30);

            // יצירת הכרטיס המרכזי - גודל מינימלי (350x175)
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

            // --- משתני שליטה על גדלים ---
            int boxWidth = 360;      // רוחב המלבן הכללי
            int boxHeight = 170;     // גובה המלבן הכללי
            int resumeBtnW = 300;    // רוחב כפתור ה-RESUME
            int resumeBtnH = 40;     // גובה כפתור ה-RESUME

            // משתנים חדשים לכפתורי האמצע (Restart & Menu)
            int midBtnW = 150;       // רוחב כפתורי האמצע
            int midBtnH = 34;        // גובה כפתורי האמצע

            int emojiFontSize = 18;  // גודל הגופן והאימוג'י

            box.setPreferredSize(new Dimension(boxWidth, boxHeight));
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            // הגדרת מרווח פנימי קבוע (Top, Left, Bottom, Right)
            box.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            // כותרת עם אימוג'י
            JLabel paused = new JLabel("PAUSED ⏸️", SwingConstants.CENTER);
            paused.setAlignmentX(Component.CENTER_ALIGNMENT);
            paused.setFont(new Font("Segoe UI Emoji", Font.BOLD, emojiFontSize + 4));
            paused.setForeground(accentColor);

            // הוספת מרווח ריק (Padding) מעל ומתחת לטקסט כדי למנוע חיתוך של האימוג'י
            paused.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            // פאנל כפתורי אמצע קטנים יותר
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            row.setOpaque(false);

            // יצירת הכפתורים תוך שימוש במשתני הגודל החדשים
            JButton restart = createCompactBtn("Restart 🔄", isDark, emojiFontSize - 4, midBtnW, midBtnH);
            JButton menu = createCompactBtn("Menu 🏠", isDark, emojiFontSize - 4, midBtnW, midBtnH);

            restart.addActionListener(e -> onRestart.run());
            menu.addActionListener(e -> onMenu.run());

            row.add(restart);
            row.add(menu);

            // כפתור המשך גדול ובולט
            JButton resume = new JButton("RESUME ▶️");
            resume.setAlignmentX(Component.CENTER_ALIGNMENT);
            resume.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20)); // פונט גדול וברור
            resume.setForeground(Color.WHITE);
            resume.setBackground(new Color(80, 120, 220));
            resume.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // הגדלת הכפתור התחתון לרוחב כמעט מלא
            resume.setMaximumSize(new Dimension(resumeBtnW, resumeBtnH));
            resume.addActionListener(e -> onResume.run());

            // הוספת הרכיבים עם רווחים אפסיים
            box.add(paused);
            box.add(Box.createVerticalStrut(5));
            box.add(row);
            box.add(Box.createVerticalStrut(8));
            box.add(resume);

            add(box);
        }

        // פונקציה ליצירת כפתור קומפקטי עם שליטה על גודל וגופן
        private JButton createCompactBtn(String text, boolean isDark, int fontSize, int w, int h) {
            JButton b = new JButton(text);
            // הגדרת פונט תומך אימוג'י
            b.setFont(new Font("Segoe UI Emoji", Font.BOLD, fontSize));
            b.setForeground(isDark ? Color.WHITE : Color.BLACK);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // קביעת הגודל המדויק לפי המשתנים שהגדרנו
            b.setPreferredSize(new Dimension(w, h));

            b.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isDark ? new Color(255,255,255,60) : new Color(0,0,0,40), 1, true),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0) // הסרת מרווחים פנימיים מיותרים
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

    private static class BoardMattePanel extends JPanel {
        private final JComponent inner;

        BoardMattePanel(JComponent inner) {
            this.inner = inner;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // רווח מהקצוות
            add(inner, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean dark = ThemeManager.getInstance().isDarkMode();
            Color bg = dark ? new Color(10, 18, 30, 160) : new Color(255, 255, 255, 170); // matte עדין
            Color border = dark ? new Color(255, 255, 255, 55) : new Color(0, 0, 0, 45);

            int arc = 18;
            int w = getWidth();
            int h = getHeight();

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
        }
    }

    private static class InfoIconButton extends JButton {
        private boolean hover = false;
        private boolean down  = false;

        InfoIconButton() {
            setPreferredSize(new Dimension(34, 34));
            setMinimumSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));

            setToolTipText("Game Rules & Info");
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
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

            int w = getWidth(), h = getHeight();

            // background circle (looks clickable)
            g2.setColor(new Color(20, 35, 55, down ? 255 : (hover ? 255 : 200)));
            g2.fillOval(0, 0, w, h);

            // border
            g2.setColor(new Color(255, 255, 255, hover ? 180 : 120));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(1, 1, w - 3, h - 3);

            // draw "i"
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            String s = "i";
            int x = (w - fm.stringWidth(s)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(s, x, y);

            g2.dispose();
        }
    }
}
