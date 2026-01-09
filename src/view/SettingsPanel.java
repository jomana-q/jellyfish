package view;

import controller.SoundManager;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;



import java.awt.*;



/**
 * מסך הגדרות (Settings)
 * מאפשר שליטה בווליום, השתקה ושינוי ערכת נושא.
 */
public class SettingsPanel extends JPanel {

    private final MainMenuGUI parent;

    // GUI Components
    private JSlider volumeSlider;
    private JCheckBox muteCheckBox;
    private JComboBox<String> themeBox;
    private JButton saveBtn;
    private JButton backBtn;

    // פנל פנימי (כרטיס שקוף)
    private JPanel cardPanel;

    public SettingsPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // כותרת
        JLabel title = new JLabel("Settings ⚙️", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // מרכז
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        cardPanel.setOpaque(false);
        cardPanel.setBackground(new Color(0, 0, 0, 150));
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- A. Volume ---
        JLabel volumeLabel = new JLabel("Music Volume 🔊:");
        styleLabel(volumeLabel);

        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);

        volumeSlider.addChangeListener(e -> {
            SoundManager.getInstance().setVolume(volumeSlider.getValue());
            cardPanel.repaint();
        });

        // --- B. Mute ---
        muteCheckBox = new JCheckBox("Mute All Sounds 🔇");
        styleCheckBox(muteCheckBox);
        muteCheckBox.addActionListener(e ->
                SoundManager.getInstance().setMuted(muteCheckBox.isSelected())
        );

        // --- C. Theme ---
        JLabel themeLabel = new JLabel("Game Theme 🎨:");
        styleLabel(themeLabel);

        String[] themes = {"Dark Mode 🌙", "Light Mode ☀️"};
        themeBox = new JComboBox<>(themes);
        themeBox.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        // הוספת רכיבים לגריד
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        cardPanel.add(volumeLabel, gbc);

        gbc.gridx = 1;
        cardPanel.add(volumeSlider, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        cardPanel.add(muteCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        cardPanel.add(themeLabel, gbc);

        gbc.gridx = 1;
        cardPanel.add(themeBox, gbc);

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // כפתורים למטה
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonsPanel.setOpaque(false);

        saveBtn = new JButton("Save Changes ✅");
        styleButton(saveBtn, new Color(60, 140, 60));

        backBtn = new JButton("Back 🔙");
        styleButton(backBtn, new Color(180, 60, 60));

        // 🔹 כפתור דף העזרה
        JButton helpBtn = new JButton("Game Help ❔");
        styleButton(helpBtn, new Color(70, 120, 200));

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);
        buttonsPanel.add(helpBtn);
        add(buttonsPanel, BorderLayout.SOUTH);

        // פעולות
        backBtn.addActionListener(e -> parent.showMainMenu());

        saveBtn.addActionListener(e -> {
            boolean isDark = (themeBox.getSelectedIndex() == 0);

            model.ThemeManager.getInstance().setDarkMode(isDark);

            JOptionPane.showMessageDialog(
                    this,
                    "Settings Saved! \nההגדרות נשמרו בהצלחה! ✅"
            );

            parent.refreshTheme();
            parent.showMainMenu();
        });

        // פתיחת דף העזרה
        helpBtn.addActionListener(e -> {
            HelpDialog dlg = new HelpDialog();
            dlg.setVisible(true);
        });
    }

    // Helpers לעיצוב
    private void styleLabel(JLabel lbl) {
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
    }

    private void styleCheckBox(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        cb.setFocusPainted(false);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ==========================
    //  Help Dialog (inner class)
    // ==========================
    // ==========================
    //  Help Dialog (inner class)
    // ==========================
    private class HelpDialog extends JDialog {

        HelpDialog() {
            super(SwingUtilities.getWindowAncestor(SettingsPanel.this),
                    "How to Play – Minesweeper", ModalityType.APPLICATION_MODAL);

            setSize(650, 650);
            setLocationRelativeTo(SettingsPanel.this);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 30));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // כותרת למעלה
            JLabel title = new JLabel("▦  How to Play", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
            title.setForeground(Color.WHITE);
            panel.add(title, BorderLayout.NORTH);

            // טקסט גלילה – עם צבעים שונים לאייקונים
            JTextPane text = new JTextPane();
            text.setEditable(false);
            text.setOpaque(false);
            text.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            text.setForeground(Color.WHITE);

            // בניית התוכן בעזרת סטיילים
            appendSection(
                    text,
                    "🎯", new Color(255, 215, 0),           // זהב
                    " Objective:\n",
                    "Work together to reveal all safe tiles while avoiding mines.\n" +
                    "Use questions and surprises wisely to maximize your final score.\n\n"
            );

            appendSection(
                    text,
                    "🌀", new Color(120, 200, 255),         // טורקיז/כחול
                    " Turn System:\n",
                    "- Players take turns one after another.\n" +
                    "- A turn is finished only after the player completes everything\n" +
                    "  related to a Question or a Surprise (answer + result animation).\n\n"
            );

            appendSection(
                    text,
                    "🧩", new Color(255, 170, 255),         // ורוד-סגול
                    " Tile Types:\n",
                    "• Empty tile – reveals an empty area with no mines around it.\n" +
                    "• Number tile – shows how many mines are touching this tile.\n" +
                    "• Mine 💣 – reduces your shared lives when revealed.\n" +
                    "• Question tile ❓ – opens a quiz question. Correct answers award\n" +
                    "  points (and sometimes bonuses), wrong answers may have a cost.\n" +
                    "• Surprise tile 🎁 – triggers a random effect such as bonus score,\n" +
                    "  extra lives or other special events defined for the level.\n\n"
            );

            appendSection(
                    text,
                    "❤️", new Color(255, 100, 140),         // אדום-ורוד
                    " Shared Lives:\n",
                    "The team has a shared pool of lives (hearts). Revealing a mine\n" +
                    "usually removes one heart. When you run out of hearts, the game ends.\n\n"
            );

            appendSection(
                    text,
                    "⭐", new Color(255, 230, 120),         // צהוב-בהיר
                    " Scoring:\n",
                    "Revealing safe tiles carefully and answering questions correctly\n" +
                    "increases your score. Some surprises can grant extra bonuses.\n\n"
            );

            appendSection(
                    text,
                    "🏆", new Color(255, 215, 0),           // זהב
                    " Victory:\n",
                    "You win when all required mines are correctly identified and the\n" +
                    "team still has at least one heart left, or when you meet the\n" +
                    "special win conditions defined for the chosen difficulty.\n\n" +
                    "Tip: Communicate with your teammate, plan your moves, and think\n" +
                    "about the numbers around you before clicking!\n"
            );

            JScrollPane scroll = new JScrollPane(text);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            panel.add(scroll, BorderLayout.CENTER);

            JButton closeBtn = new JButton("Close ✖");
            closeBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
            closeBtn.setBackground(new Color(180, 60, 60));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFocusPainted(false);
            closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> dispose());

            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.add(closeBtn);
            panel.add(bottom, BorderLayout.SOUTH);

            setContentPane(panel);
        }

        /**
         * מוסיף מקטע (אייקון צבעוני + כותרת + טקסט) ל-JTextPane.
         */
        private void appendSection(JTextPane pane,
                String icon,
                Color iconColor,
                String title,
                String body) {

StyledDocument doc = pane.getStyledDocument();

try {
// סגנון לאייקון
Style iconStyle = pane.addStyle("icon", null);
StyleConstants.setForeground(iconStyle, iconColor);
StyleConstants.setBold(iconStyle, true);
StyleConstants.setFontSize(iconStyle, 20);

// סגנון לכותרת
Style titleStyle = pane.addStyle("title", null);
StyleConstants.setForeground(titleStyle, Color.WHITE);
StyleConstants.setBold(titleStyle, true);
StyleConstants.setFontSize(titleStyle, 16);

// סגנון לטקסט הרגיל
Style bodyStyle = pane.addStyle("body", null);
StyleConstants.setForeground(bodyStyle, Color.WHITE);
StyleConstants.setFontSize(bodyStyle, 14);

// הכנסת הטקסט למסמך
doc.insertString(doc.getLength(), icon + " ", iconStyle);
doc.insertString(doc.getLength(), title + "\n", titleStyle);
doc.insertString(doc.getLength(), body + "\n\n", bodyStyle);

} catch (BadLocationException ex) {
ex.printStackTrace();
}
}

    }


    

}
