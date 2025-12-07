package view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class AdminLoginPanel extends JPanel {

    private final MainMenuGUI parent;

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JButton loginBtn = new JButton("Login");
    private final JButton backBtn = new JButton("Back");

    // אפשר לשנות אם תרצו
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

        // כותרת למעלה
        JLabel title = new JLabel("Admin Login", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // ===== מרכז: כרטיס קטן עם הטופס =====
        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));

        // כרטיס
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(new Color(0, 0, 0, 40)); // שחור שקוף קצת
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        for (JLabel lbl : new JLabel[]{userLabel, passLabel}) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        }

        // שדות – גודל קבוע, לא נמתחים
        Dimension fieldSize = new Dimension(220, 28);
        userField.setPreferredSize(fieldSize);
        passField.setPreferredSize(fieldSize);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

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

        // שורה 3 – כפתורים: Back משמאל, Login מימין
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

        // הוספת הכרטיס למרכז, ממורכז אנכית
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerWrapper.add(Box.createVerticalGlue());
        centerWrapper.add(card);
        centerWrapper.add(Box.createVerticalGlue());

        add(centerWrapper, BorderLayout.CENTER);

        // ---- לוגיקה (אותו דבר כמו קודם) ----
        backBtn.addActionListener(e -> parent.showMainMenu());

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateLoginButton(); }
            @Override public void removeUpdate(DocumentEvent e) { updateLoginButton(); }
            @Override public void changedUpdate(DocumentEvent e) { updateLoginButton(); }
        };
        userField.getDocument().addDocumentListener(dl);
        passField.getDocument().addDocumentListener(dl);

        loginBtn.addActionListener(e -> onLogin());
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(60, 120, 200));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(80, 80, 80));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
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
            // כניסה מוצלחת – ניקוי השדות ומעבר לדשבורד
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
