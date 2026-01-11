package view;

import controller.MinesweeperController;
import controller.SoundManager;
import model.Board;
import model.Difficulty;
import model.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.ThemeManager;
import java.awt.event.*;
import java.awt.geom.*;

public class MainMenuGUI extends JFrame {

    // ניהול מסכים באמצע החלון
    private final CardLayout centerLayout = new CardLayout();
    private final JPanel centerContainer = new JPanel(centerLayout);

    // המסכים עצמם
    private JPanel mainMenuCenterPanel;     // המסך הראשי (Start Game / Admin)
    private GameSetupPanel setupPanel;      // מסך שמות + קושי
    private MinesweeperGUI gamePanel;       // מסך המשחק (שני לוחות)
    private AdminLoginPanel adminLoginPanel; // מסך התחברות אדמין
    private JPanel adminDashboardPanel;      // מסך דשבורד אדמין
    private SettingsPanel settingsPanel;

    public MainMenuGUI() {
        // 1. הגדרות חלון (מותאם למחשב PC)
        setTitle("Minesweeper - Jellyfish Team");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // גודל רחב יותר למסך מחשב
        setLocationRelativeTo(null); // מרכוז למסך
        // פאנל רקע ראשי
        JPanel mainPanel = new BackgroundImagePanel("/images/background.jpeg", "/images/background_light.jpeg");
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // --- מרכז: Container עם CardLayout ---
        centerContainer.setOpaque(false);

        mainMenuCenterPanel = buildMainMenuCenterPanel(); // המסך הישן – העיצוב נשמר
        centerContainer.add(mainMenuCenterPanel, "MENU");

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // --- חלק תחתון: כפתור יציאה ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 20, 20, 0)); // ריווח מלמטה ומשמאל

        JButton exitBtn = createStyledButton("Exit");

        exitBtn.setPreferredSize(new Dimension(120, 45));
        exitBtn.setMaximumSize(new Dimension(120, 45));

        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        exitBtn.addActionListener(e -> {
            // יצירת דיאלוג אישור יציאה (Exit Confirmation Dialog)
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to exit the game?",
                    "Exit Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            // אם המשתמש לחץ על Yes -> סגור את התוכנית
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        bottomPanel.add(exitBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);

        // ⭐ הפעלת מוזיקת התפריט לאחר בניית ה־GUI
        SoundManager.getInstance().playMenuLoop();
    }

    /**
     * בניית המסך המרכזי המקורי (MINESWEEPER + Start Game + Admin).
     * זה בדיוק ה-centerPanel שהיה קודם – רק הוצאתי לשיטה.
     */
    private JPanel buildMainMenuCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // כפתור הגדרות עליון
        JPanel settingsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        settingsWrapper.setOpaque(false);
        settingsWrapper.setMaximumSize(new Dimension(2000, 60));
        settingsWrapper.setBorder(new EmptyBorder(10, 0, 0, 10));

        JButton settingsBtn = createIconButton("⚙️");
        settingsBtn.setToolTipText("Settings");
        settingsBtn.addActionListener(e -> openSettingsPage());
        settingsWrapper.add(settingsBtn);
        centerPanel.add(settingsWrapper);

        centerPanel.add(Box.createVerticalStrut(10));

        // כותרת
        JLabel titleLabel = new JLabel("MINESWEEPER") {
            @Override
            public void paintComponent(Graphics g) {
                // ⭐ עדכון צבע אוטומטי
                Color themeColor = Color.WHITE;
                if (!getForeground().equals(themeColor)) {
                    setForeground(themeColor);
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // ⭐ שינוי: חיזוק הצל (יותר כהה ומודגש)
                g2.setColor(new Color(0, 0, 0, 180)); // שחור חזק יותר (במקום 50)
                // ציור הצל פעמיים להדגשה
                g2.drawString(getText(), 3, getHeight() - 3);
                g2.drawString(getText(), 5, getHeight() - 5);

                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 60)); // נשאר Bold
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        // 2. תת-כותרת דינמית (מודגשת יותר)
        JLabel subTitleLabel = new JLabel("By Jellyfish Team ") {
            @Override
            public void paintComponent(Graphics g) {
                Color themeColor = model.ThemeManager.getInstance().getTextColor();
                Color subColor = model.ThemeManager.getInstance().isDarkMode() ? new Color(170, 220, 255) : new Color(30, 30, 180);

                if (!getForeground().equals(subColor)) {
                    setForeground(subColor);
                }

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                String text = getText();
                FontMetrics fm = g.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();

                g2.setColor(new Color(0, 255, 255, 80));
                g2.drawString(text, x - 1, y);
                g2.drawString(text, x + 1, y);
                g2.drawString(text, x, y - 1);
                g2.drawString(text, x, y + 1);

                super.paintComponent(g);
            }
        };

        subTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subTitleLabel);

        centerPanel.add(Box.createVerticalStrut(60));

        // === הכפתורים ===

        // 1. Start Game
        JButton startGameBtn = createStyledButton("Start Game");
        startGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameBtn.addActionListener(e -> showSetupScreen());
        centerPanel.add(startGameBtn);

        centerPanel.add(Box.createVerticalStrut(20));

        // ⭐ 2. Top Scores (הכפתור החדש!)
        JButton scoresBtn = createStyledButton("Top Scores 🏆");
        scoresBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        // false = הגענו מהתפריט הראשי
        scoresBtn.addActionListener(e -> showHistoryPanel(false));
        centerPanel.add(scoresBtn);

        centerPanel.add(Box.createVerticalStrut(20));

        // 3. Admin Login
        JButton adminBtn = createStyledButton("Admin Login");
        adminBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        adminBtn.addActionListener(e -> showAdminLogin());
        centerPanel.add(adminBtn);

        return centerPanel;
    }

    /** מעבר למסך התחברות אדמין */
    private void showAdminLogin() {
        if (adminLoginPanel == null) {
            adminLoginPanel = new AdminLoginPanel(this);
            centerContainer.add(adminLoginPanel, "ADMIN_LOGIN");
        }
        centerLayout.show(centerContainer, "ADMIN_LOGIN");
    }

    /** אחרי התחברות מוצלחת – מסך דשבורד אדמין */
    public void showAdminDashboard() {
        if (adminDashboardPanel == null) {
            adminDashboardPanel = buildAdminDashboardPanel();
            centerContainer.add(adminDashboardPanel, "ADMIN_DASH");
        }
        centerLayout.show(centerContainer, "ADMIN_DASH");
    }

    private JPanel buildAdminDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        panel.add(title);

        panel.add(Box.createVerticalStrut(40));

        JButton manageQuestionsBtn = createStyledButton("Manage Questions");
        manageQuestionsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageQuestionsBtn.addActionListener(e -> showQuestionWizard());
        panel.add(manageQuestionsBtn);

        panel.add(Box.createVerticalStrut(20));

        JButton historyBtn = createStyledButton("Game History");
        historyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        // ⭐ שינוי: שולחים true כי אנחנו באדמין
        historyBtn.addActionListener(e -> showHistoryPanel(true));
        panel.add(historyBtn);

        panel.add(Box.createVerticalStrut(40));

        JButton backBtn = createStyledButton("Back to Main Menu");
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> showMainMenu());
        panel.add(backBtn);

        return panel;
    }

    /**
     * פונקציה למעבר למסך ניהול השאלות (Wizard).
     * יוצרת את המסך מחדש בכל כניסה כדי לוודא שהנתונים עדכניים.
     */
    public void showQuestionWizard() {
        // יצירת פאנל הניהול והעברת "this" (החלון הראשי) כפרמטר
        QuestionManagementPanel wizardPanel = new QuestionManagementPanel(this);

        // הוספה ל-CardLayout בשם "WIZARD"
        centerContainer.add(wizardPanel, "WIZARD");

        // מעבר למסך הזה
        centerLayout.show(centerContainer, "WIZARD");
    }

    /**
     * מעבר למסך היסטוריית המשחקים.
     * @param isAdmin האם הכניסה היא דרך האדמין (משפיע על כפתור החזרה)
     */
    public void showHistoryPanel(boolean isAdmin) {
        // שולחים את הפרמטר isAdmin לבנאי החדש
        HistoryPanel historyPanel = new HistoryPanel(this, isAdmin);
        centerContainer.add(historyPanel, "HISTORY");
        centerLayout.show(centerContainer, "HISTORY");
    }

    // ---- ניהול מסכים ----

    /** חזרה למסך הראשי */
    public void showMainMenu() {
        // ⭐ להפעיל את מוזיקת התפריט כשחוזרים למיין מניו
        SoundManager.getInstance().playMenuLoop();
        centerLayout.show(centerContainer, "MENU");
    }

    /**
     * רענון ערכת הנושא (צביעה מחדש של החלון).
     * פונקציה זו נקראת מתוך SettingsPanel כשהמשתמש לוחץ Save.
     */
    public void refreshTheme() {
        this.repaint(); // מצייר מחדש את הרקע עם הצבעים החדשים
    }

    /** מעבר למסך הגדרת משחק (שמות + קושי) */
    private void showSetupScreen() {
        if (setupPanel == null) {
            setupPanel = new GameSetupPanel(this);
            centerContainer.add(setupPanel, "SETUP");
        }
        centerLayout.show(centerContainer, "SETUP");
    }

    /**
     * קריאה מתוך GameSetupPanel אחרי ששני השמות + הקושי נבחרו.
     * כאן נוצרות המחלקות של המודל ומסך המשחק עם שני לוחות.
     */
    public void startGame(String player1Name, String player2Name, Difficulty difficulty) {
        Board board1 = new Board(difficulty);
        Board board2 = new Board(difficulty);
        GameSession session = new GameSession(difficulty);

        gamePanel = new MinesweeperGUI(this, player1Name, player2Name, board1, board2, session);

        MinesweeperController controller =
                new MinesweeperController(board1, board2, session, gamePanel);
        gamePanel.setController(controller);

        centerContainer.add(gamePanel, "GAME");

        Dimension desired = switch (difficulty) {
            case EASY   -> new Dimension(900, 780);
            case MEDIUM -> new Dimension(1100, 900);
            case HARD   -> new Dimension(1250, 1080);
        };

        // גודל המסך (של המשתמש)
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        // משאירים קצת מרווח (כדי לא להידבק לשורת המשימות)
        int maxW = screen.width  - 80;
        int maxH = screen.height - 120;

        // אם desired גדול מדי למסך → נקטין אותו לגודל שמקסימום נכנס
        int finalW = Math.min(desired.width,  maxW);
        int finalH = Math.min(desired.height, maxH);

        Dimension finalSize = new Dimension(finalW, finalH);

        // חשוב: מינימום לא יכול להיות יותר מהמסך!
        setMinimumSize(new Dimension(
                Math.min(desired.width,  maxW),
                Math.min(desired.height, maxH)
        ));

        setSize(finalSize);
        setLocationRelativeTo(null);

        centerLayout.show(centerContainer, "GAME");
    }

    // פונקציות עיצוב כפתורים מתוקנות

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color themeColor = ThemeManager.getInstance().getTextColor();
                Color crystalGlow = new Color(0, 240, 255);

                if (getModel().isRollover()) {

                    setForeground(crystalGlow);

                    g2.setColor(new Color(0, 240, 255, 60));
                    g2.setStroke(new BasicStroke(3));

                } else {

                    if (!getForeground().equals(themeColor)) {
                        setForeground(themeColor);
                    }

                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.setStroke(new BasicStroke(2));
                }

                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                if (getModel().isRollover()) {
                    g2.setColor(crystalGlow);
                } else {
                    g2.setColor(themeColor);
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));

        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setPreferredSize(new Dimension(240, 55));
        btn.setMaximumSize(new Dimension(240, 55));

        return btn;
    }

    private JButton createIconButton(String icon) {
        JButton btn = new JButton("") {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                Color themeColor = ThemeManager.getInstance().getTextColor();
                Color crystalGlow = new Color(0, 240, 255); // سماوي

                if (getModel().isRollover()) {
                    g2.setColor(crystalGlow);
                } else {
                    g2.setColor(themeColor);
                }

                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));

                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(icon);
                int textHeight = fm.getAscent();

                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - 6;

                g2.drawString(icon, x, y);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setPreferredSize(new Dimension(80, 60));
        btn.setMaximumSize(new Dimension(80, 60));

        return btn;
    }

    // --- Placeholder Navigation ---

    /** פתיחת מסך ההגדרות (Settings) */
    public void openSettingsPage() {
        // מוזיקת תפריט גם במסך ההגדרות (ללא שינוי GUI)
        SoundManager.getInstance().playMenuLoop();
        if (settingsPanel == null) {
            settingsPanel = new SettingsPanel(this);
            centerContainer.add(settingsPanel, "SETTINGS");
        }
        centerLayout.show(centerContainer, "SETTINGS");
    }

    private void openAdminDashboard() {
        JOptionPane.showMessageDialog(this, "Admin Login");
    }

    // --- מחלקת הרקע (נשארה כמו קודם) ---
    class BackgroundPanel extends JPanel {
        private final Point[] symbolPositions = new Point[20]; // יותר אלמנטים
        private final String[] symbols = {"💣", "🎁", "❓", "❤️"}; // מגוון סמלים
        private final String[] activeSymbols = new String[20]; // שומר איזה סמל בכל מיקום

        public BackgroundPanel() {
            // אתחול מיקומים וסמלים אקראיים
            for (int i = 0; i < symbolPositions.length; i++) {
                int x = (int) (Math.random() * 900);
                int y = (int) (Math.random() * 700);
                symbolPositions[i] = new Point(x, y);

                // בחירת סמל אקראי
                int randIdx = (int) (Math.random() * symbols.length);
                activeSymbols[i] = symbols[randIdx];
            }
        }

        /**
         * רענון ערכת הנושא (צביעה מחדש של החלון).
         */

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. גרדיאנט (הצבעים נלקחים מ-ThemeManager)
            // אנחנו משתמשים ב-Singleton כדי לקבל את הצבעים הנוכחיים (כהה/בהיר)
            model.ThemeManager theme = model.ThemeManager.getInstance();

            Color color1 = theme.getBackgroundColor1(); // צבע עליון
            Color color2 = theme.getBackgroundColor2(); // צבע תחתון

            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);

            // 2. רשת עדינה
            g2d.setColor(new Color(255, 255, 255, 10));
            int gridSize = 50;
            for (int x = 0; x < w; x += gridSize) g2d.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += gridSize) g2d.drawLine(0, y, w, y);

            // 3. סמלים צפים (מוקשים, מתנות, לבבות)
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            // ⭐ תיקון: בחירת צבע האייקונים לפי הת'ים
            // אם כהה -> לבן שקוף. אם בהיר -> כחול/שחור שקוף.
            Color iconColor = model.ThemeManager.getInstance().isDarkMode()
                    ? new Color(255, 255, 255, 30)  // לבן שקוף (לרקע כהה)
                    : new Color(0, 0, 0, 30);       // שחור שקוף (לרקע בהיר)
            g2d.setColor(iconColor);

            for (int i = 0; i < symbolPositions.length; i++) {
                g2d.drawString(activeSymbols[i], symbolPositions[i].x, symbolPositions[i].y);
            }
        }
    }
}
