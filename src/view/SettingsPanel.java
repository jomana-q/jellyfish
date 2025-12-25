package view;

import javax.swing.*;
import java.awt.*;

/**
 * מסך הגדרות (Settings) - כולל שליטה בווליום, ערכת נושא (Theme) וכפתורי שמירה.
 * עודכן כדי לתמוך באימוג'י (Segoe UI Emoji).
 */
public class SettingsPanel extends JPanel {

    private final MainMenuGUI parent;
    
    // רכיבי ה-GUI
    private JSlider volumeSlider;
    private JCheckBox muteCheckBox;
    private JComboBox<String> themeBox;
    private JButton saveBtn;
    private JButton backBtn;

    public SettingsPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // 1. כותרת העמוד (עם אימוג'י של גלגל שיניים)
        JLabel title = new JLabel("Settings ⚙️", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        // שימוש בפונט Emoji כדי שהאייקון יופיע צבעוני ויפה
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // 2. אזור המרכז - כרטיס מעוצב חצי שקוף
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(true);
        card.setBackground(new Color(0, 0, 0, 100)); // רקע שחור שקוף למחצה
        card.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- הגדרות שמע (Volume) ---
        JLabel volumeLabel = new JLabel("Music Volume 🔊:");
        styleLabel(volumeLabel);
        
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        
        muteCheckBox = new JCheckBox("Mute All Sounds 🔇");
        styleCheckBox(muteCheckBox);

        // --- הגדרות ערכת נושא (Theme) ---
        JLabel themeLabel = new JLabel("Game Theme 🎨:");
        styleLabel(themeLabel);
        
        String[] themes = {"Dark Ocean 🌊 (Default)", "Light Mode ☀️", "High Contrast 👁️"};
        themeBox = new JComboBox<>(themes);
        // פונט תומך אימוג'י בתוך הרשימה
        themeBox.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        // הוספת הרכיבים לתוך הכרטיס
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(volumeLabel, gbc);
        
        gbc.gridx = 1;
        card.add(volumeSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        card.add(muteCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        card.add(themeLabel, gbc);
        
        gbc.gridx = 1;
        card.add(themeBox, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // 3. כפתורים למטה (Save / Back)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonsPanel.setOpaque(false);

        saveBtn = new JButton("Save Changes ✅");
        styleButton(saveBtn, new Color(60, 140, 60)); // ירוק

        backBtn = new JButton("Back 🔙");
        styleButton(backBtn, new Color(180, 60, 60)); // אדום

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);

        // --- לוגיקה וכפתורים ---
        
        // חזרה לתפריט הראשי
        backBtn.addActionListener(e -> parent.showMainMenu());

        // שמירה (סימולציה)
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Settings Saved! \nההגדרות נשמרו בהצלחה! ✅");
            parent.showMainMenu();
        });
    }

    // --- פונקציות עזר לעיצוב (עם פונט Emoji) ---

    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.WHITE);
        // שינוי לפונט Emoji
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(Color.WHITE);
        // שינוי לפונט Emoji
        cb.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        cb.setFocusPainted(false);
    }

    private void styleButton(JButton btn, Color bg) {
        // שינוי לפונט Emoji
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}