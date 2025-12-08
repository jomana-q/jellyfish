package view;

import controller.MinesweeperController;
import model.Board;
import model.Difficulty;
import model.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
   
    public MainMenuGUI() {
        // 1. הגדרות חלון (מותאם למחשב PC)
        setTitle("Minesweeper - Jellyfish Team");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700); // גודל רחב יותר למסך מחשב
        setLocationRelativeTo(null); // מרכוז למסך

        // פאנל רקע ראשי
        JPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BorderLayout());
        setContentPane(mainPanel);

        // --- חלק עליון: כפתור הגדרות ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(10, 0, 0, 10)); // ריווח קטן

        JButton settingsBtn = createIconButton("⚙️");
        settingsBtn.setToolTipText("Settings");
        settingsBtn.addActionListener(e -> openSettingsPage());
        topPanel.add(settingsBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);

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
        // הקטנת כפתור היציאה מעט
        exitBtn.setMaximumSize(new Dimension(100, 40));
        exitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        exitBtn.addActionListener(e -> System.exit(0)); // סגירת התוכנית

        bottomPanel.add(exitBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * בניית המסך המרכזי המקורי (MINESWEEPER + Start Game + Admin).
     * זה בדיוק ה-centerPanel שהיה קודם – רק הוצאתי לשיטה.
     */
    private JPanel buildMainMenuCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // רווח עליון גמיש
        centerPanel.add(Box.createVerticalStrut(50));

        // 1. כותרת ראשית עם הצללה (Shadow Effect)
        JLabel titleLabel = new JLabel("MINESWEEPER") {
            // אוברייד כדי לצייר צל לטקסט
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // ציור הצל (שחור)
                g2.setColor(new Color(0, 0, 0, 100));
                g2.drawString(getText(), 4, getHeight() - 4); // הזזה קלה לצל

                // ציור הטקסט עצמו
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 60)); // פונט גדול ועבה
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);

        // 2. תת-כותרת (שם הקבוצה)
        JLabel subTitleLabel = new JLabel("By Jellyfish Team ");
        subTitleLabel.setFont(new Font("Segoe UI", Font.ITALIC, 22));
        subTitleLabel.setForeground(new Color(135, 206, 250)); // תכלת בהיר
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subTitleLabel);

        centerPanel.add(Box.createVerticalStrut(60)); // רווח

        // 3. כפתורים ראשיים (טקסט בלבד)
        JButton startGameBtn = createStyledButton("Start Game");
        startGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameBtn.addActionListener(e -> showSetupScreen());
        centerPanel.add(startGameBtn);

        centerPanel.add(Box.createVerticalStrut(20)); // רווח בין כפתורים

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
        
        // --- השינוי כאן: הוספת פעולה לכפתור שפותחת את אשף השאלות ---
        manageQuestionsBtn.addActionListener(e -> showQuestionWizard());
        
        panel.add(manageQuestionsBtn);

        panel.add(Box.createVerticalStrut(20));

        JButton historyBtn = createStyledButton("Game History");
        historyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        historyBtn.addActionListener(e -> showHistoryPanel());        panel.add(historyBtn);

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
     */
    public void showHistoryPanel() {
        HistoryPanel historyPanel = new HistoryPanel(this);
        centerContainer.add(historyPanel, "HISTORY");
        centerLayout.show(centerContainer, "HISTORY");
    }
    // ---- ניהול מסכים ----

    /** חזרה למסך הראשי */
    public void showMainMenu() {
        centerLayout.show(centerContainer, "MENU");
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

        // יצירת הבקר וחיבורו ל-GUI (MVC)
        MinesweeperController controller =
                new MinesweeperController(board1, board2, session, gamePanel);
        gamePanel.setController(controller);

        centerContainer.add(gamePanel, "GAME");
        centerLayout.show(centerContainer, "GAME");
    }



    // --- פונקציות עיצוב כפתורים מתוקנות (Fix for White Box Issue) ---

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            // תיקון הבעיה הגרפית: מציירים את הרקע ידנית
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    // צבע בהיר יותר כשעוברים עם העכבר
                    g2.setColor(new Color(255, 255, 255, 50));
                    g2.setStroke(new BasicStroke(2)); // מסגרת עבה יותר
                } else {
                    // צבע רגיל (שקוף למחצה)
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.setStroke(new BasicStroke(1));
                }

                // ציור הרקע (מלבן עם פינות עגולות)
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                // ציור המסגרת
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); // ביטול הציור האוטומטי של ג'אווה (מונע ריבוע לבן)
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); // אנחנו מציירים גבול ידנית למעלה
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // גודל אחיד לכפתורים
        btn.setPreferredSize(new Dimension(220, 50));
        btn.setMaximumSize(new Dimension(220, 50));

        return btn;
    }

    private JButton createIconButton(String icon) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // אפקט מעבר עכבר לאייקון
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                btn.setForeground(Color.CYAN); // שינוי צבע לתכלת
            }

            public void mouseExited(MouseEvent evt) {
                btn.setForeground(Color.WHITE);
            }
        });

        return btn;
    }

    // --- Placeholder Navigation ---

    private void openSettingsPage() {
        JOptionPane.showMessageDialog(this, "Settings Page");
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. גרדיאנט כהה
            Color color1 = new Color(10, 25, 40);
            Color color2 = new Color(25, 50, 60);
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
            g2d.setColor(new Color(255, 255, 255, 20)); // שקיפות

            for (int i = 0; i < symbolPositions.length; i++) {
                g2d.drawString(activeSymbols[i], symbolPositions[i].x, symbolPositions[i].y);
            }
        }
    }
}
