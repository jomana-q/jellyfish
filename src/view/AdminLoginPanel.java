package view;

import model.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AdminLoginPanel extends JPanel {

    private final MainMenuGUI parent;

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JButton loginBtn = new JButton("Login");
    private final JButton backBtn = new JButton("Back");

    // Creds
    private static final String ADMIN_USER = "ADMIN";
    private static final String ADMIN_PASS = "ADMIN";

    public AdminLoginPanel(MainMenuGUI parent) {
        this.parent = parent;
        buildUi();
    }

    private void buildUi() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // 1. כותרת למעלה (Title) - דינמית
        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                // בדיקת צבע טקסט מה-ThemeManager
                setForeground(ThemeManager.getInstance().getTextColor());
                super.paintComponent(g);
            }
        };
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // ===== מרכז: כרטיס קטן עם הטופס =====
        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));

        // 2. כרטיס (Card) - רקע משתנה לפי הת'ים
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                
                // בדיקה האם המצב כהה או בהיר
                boolean isDark = ThemeManager.getInstance().isDarkMode();

                // רקע הכרטיס: כהה שקוף או לבן שקוף
                Color bgColor = isDark ? new Color(0, 0, 0, 60) : new Color(255, 255, 255, 200);
                // מסגרת: לבנה עדינה או אפורה עדינה
                Color borderColor = isDark ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 50);

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, w, h, 20, 20);

                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

                g2.dispose();
                // super.paintComponent(g); // לא צריך, ציירנו רקע ידנית
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        // 3. תוויות (Labels) - דינמיות
        JLabel userLabel = createDynamicLabel("Username:");
        JLabel passLabel = createDynamicLabel("Password:");

        // 4. עיצוב השדות (Fields Styling)
        styleTextField(userField);
        styleTextField(passField); // עובד גם על PasswordField

        // שורה 1 – Username
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(userLabel, gbc);
        gbc.gridx = 1;
        card.add(userField, gbc);

        // שורה 2 – Password
        gbc.gridx = 0; gbc.gridy = 1;
        card.add(passLabel, gbc);
        gbc.gridx = 1;
        card.add(passField, gbc);

        // שורה 3 – כפתורים
        JPanel buttonsRow = new JPanel(new BorderLayout(10, 0));
        buttonsRow.setOpaque(false);
        styleSecondaryButton(backBtn);
        stylePrimaryButton(loginBtn);
        loginBtn.setEnabled(false);

        buttonsRow.add(backBtn, BorderLayout.WEST);
        buttonsRow.add(loginBtn, BorderLayout.EAST);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(buttonsRow, gbc);

        // הוספת הכרטיס למרכז
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(Box.createVerticalGlue());
        centerWrapper.add(card);
        centerWrapper.add(Box.createVerticalGlue());

        add(centerWrapper, BorderLayout.CENTER);

        // ---- לוגיקה ----
        backBtn.addActionListener(e -> parent.showMainMenu());

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateLoginButton(); }
            @Override public void removeUpdate(DocumentEvent e) { updateLoginButton(); }
            @Override public void changedUpdate(DocumentEvent e) { updateLoginButton(); }
        };
        userField.getDocument().addDocumentListener(dl);
        passField.getDocument().addDocumentListener(dl);

        userField.addActionListener(e -> onLogin());
        passField.addActionListener(e -> onLogin());
        
        loginBtn.addActionListener(e -> onLogin());
    }

    // --- Helper Methods ---

    /** יצירת תווית שמשנה צבע לפי הת'ים */
    private JLabel createDynamicLabel(String text) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getTextColor());
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return lbl;
    }

    /** עיצוב שדות הקלט (כמו ב-New Game) */
    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setPreferredSize(new Dimension(220, 34));

        boolean isDark = ThemeManager.getInstance().isDarkMode();

        // צבעים
        Color textColor = isDark ? Color.WHITE : Color.BLACK;
        Color bgColor   = isDark ? new Color(30, 45, 60) : new Color(245, 245, 245);
        Color borderColor = isDark ? new Color(255, 255, 255, 70) : new Color(0, 0, 0, 50);
        Color caretColor = isDark ? Color.WHITE : Color.BLACK;

        tf.setForeground(textColor);
        tf.setBackground(bgColor);
        tf.setCaretColor(caretColor);

        // גבולות
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        );
        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 255), 2, true),
                BorderFactory.createEmptyBorder(3, 7, 3, 7)
        );

        tf.setBorder(normal);

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(focused); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(normal); }
        });
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 120, 200));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(80, 80, 80));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateLoginButton() {
        boolean filled =
                !userField.getText().trim().isEmpty() &&
                passField.getPassword().length > 0;
        loginBtn.setEnabled(filled);
    }

    private void onLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (ADMIN_USER.equalsIgnoreCase(user) && ADMIN_PASS.equals(pass)) {
            userField.setText("");
            passField.setText("");
            parent.showAdminDashboard();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username or password.",
                    "Login failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}