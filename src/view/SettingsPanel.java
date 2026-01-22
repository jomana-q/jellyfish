package view;

import controller.SoundManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;

/**
 * מסך הגדרות (Settings) - מאפשר שליטה בווליום, בחירת קובץ מוזיקה, השתקה ושינוי ערכת נושא,
 * וכן דף עזרה (Game Help).
 */
public class SettingsPanel extends JPanel {

    private final MainMenuGUI parent;

    // רכיבי הממשק (GUI Components)
    private JSlider volumeSlider;
    private JCheckBox muteCheckBox;
    private JButton themeToggle;       // כפתור להחלפת ערכת נושא (Toggle)
    private JButton selectMusicBtn;    // כפתור לבחירת מוזיקה מהמחשב
    private JButton saveBtn;
    private JButton backBtn;

    // הפאנל הפנימי (הרקע השקוף) - נשמר כמשתנה כדי שנוכל לרענן אותו
    private JPanel cardPanel;

    public SettingsPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // 1. כותרת העמוד
        JLabel title = new JLabel("Settings ⚙️", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // 2. אזור המרכז (מעטפת)
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // הכרטיס השקוף שבו יושבים הכפתורים
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

        // --- A. הגדרות שמע (Volume) ---
        JLabel volumeLabel = new JLabel("Music Volume 🔊:");
        styleLabel(volumeLabel);

        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);

        volumeSlider.addChangeListener(e -> {
            SoundManager.getInstance().setVolume(volumeSlider.getValue());
            cardPanel.repaint(); // תיקון גרפי
        });

        // --- B. בחירת מוזיקה אישית ---
        JLabel customMusicLabel = new JLabel("Custom Music 🎵:");
        styleLabel(customMusicLabel);

        selectMusicBtn = new JButton("Choose File... 📂");
        styleButton(selectMusicBtn, new Color(70, 130, 180)); // כחול
        selectMusicBtn.addActionListener(e -> chooseMusicFile());

        // --- C. השתקה (Mute) ---
        muteCheckBox = new JCheckBox("Mute All Sounds 🔇");
        styleCheckBox(muteCheckBox);
        muteCheckBox.addActionListener(e ->
                SoundManager.getInstance().setMuted(muteCheckBox.isSelected())
        );

        // --- D. ערכת נושא (Theme) ---
        JLabel themeLabel = new JLabel("Game Theme 🎨:");
        styleLabel(themeLabel);

        themeToggle = new JButton();
        themeToggle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        themeToggle.setFocusPainted(false);
        themeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boolean currentMode = model.ThemeManager.getInstance().isDarkMode();
        updateThemeButtonLook(themeToggle, currentMode);

        themeToggle.addActionListener(e -> {
            boolean isCurrentlyDark = themeToggle.getText().contains("Dark");
            updateThemeButtonLook(themeToggle, !isCurrentlyDark);
        });

        // הוספת הרכיבים לתוך ה-Grid
        gbc.gridx = 0; gbc.gridy = 0;
        cardPanel.add(volumeLabel, gbc);
        gbc.gridx = 1;
        cardPanel.add(volumeSlider, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        cardPanel.add(customMusicLabel, gbc);
        gbc.gridx = 1;
        cardPanel.add(selectMusicBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        cardPanel.add(muteCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        cardPanel.add(themeLabel, gbc);
        gbc.gridx = 1;
        cardPanel.add(themeToggle, gbc);

        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // 3. כפתורים למטה (Save / Back / Help)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonsPanel.setOpaque(false);

        saveBtn = new JButton("Save Changes");
        styleButton(saveBtn, new Color(80, 120, 220));

        backBtn = new JButton("Back");
        styleButton(backBtn, new Color(70, 80, 100));

        
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);

        // לוגיקת כפתורים
        backBtn.addActionListener(e -> parent.showMainMenu());

        saveBtn.addActionListener(e -> {
            boolean isDark = themeToggle.getText().contains("Dark");
            model.ThemeManager.getInstance().setDarkMode(isDark);
            JOptionPane.showMessageDialog(this,
                    "Settings Saved! \nההגדרות נשמרו בהצלחה! ✅");
            parent.refreshTheme();
            parent.showMainMenu();
        });

        
    }

    /**
     * פונקציה לפתיחת חלון בחירת קובץ מוזיקה (WAV בלבד).
     */
    private void chooseMusicFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Background Music (.wav)");

        FileNameExtensionFilter filter =
                new FileNameExtensionFilter("WAV Sound Files", "wav");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            SoundManager.getInstance().stopMusic();
            SoundManager.getInstance().playBackgroundMusic(selectedFile.getAbsolutePath());

            JOptionPane.showMessageDialog(this,
                    "Now Playing: \n" + selectedFile.getName() + " 🎶");
        }
    }

    // פונקציות עזר לעיצוב
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

    /**
     * עדכון עיצוב כפתור הערכה (Dark/Light)
     */
    private void updateThemeButtonLook(JButton btn, boolean isDark) {
        if (isDark) {
            btn.setText("Dark Mode 🌙");
            btn.setBackground(new Color(60, 60, 80));
            btn.setForeground(new Color(220, 220, 255));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        } else {
            btn.setText("Light Mode ☀️");
            btn.setBackground(new Color(255, 250, 240));
            btn.setForeground(new Color(220, 110, 160));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(240, 180, 210), 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        }
    }

    // ==========================
    // Help Dialog (inner class)
    // ==========================
    private class HelpDialog extends JDialog {

        HelpDialog() {
            super(SwingUtilities.getWindowAncestor(SettingsPanel.this),
                    "How to Play – Minesweeper",
                    ModalityType.APPLICATION_MODAL);

            setSize(650, 650);
            setLocationRelativeTo(SettingsPanel.this);

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(new Color(30, 30, 30));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("▦  How to Play", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
            title.setForeground(Color.WHITE);
            panel.add(title, BorderLayout.NORTH);

            JTextPane text = new JTextPane();
            text.setEditable(false);
            text.setOpaque(false);
            text.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            text.setForeground(Color.WHITE);

            // 🎯 Objective
            appendSection(text, "🎯", new Color(255, 215, 0),
                    " Objective:\n",
                    "Cooperate to achieve the highest team score.\n"
                            + "Reveal tiles, manage shared lives, and use Question/Surprise stations wisely.\n");

            // 🌀 Turn System
            appendSection(text, "🌀", new Color(120, 200, 255),
                    " Turn System:\n",
                    "Players alternate turns based on the performed action:\n"
                            + "• Reveal a tile (Left Click) → TURN SWITCHES.\n"
                            + "• Place a flag 🚩 → TURN SWITCHES.\n"
                            + "• Remove a flag 🚫 (unflag) → TURN STAYS.\n"
                            + "• Activate a Question ❓ / Surprise 🎁 station → TURN STAYS.\n"
                            + "• If a mine 💣 is revealed due to flagging → TURN STAYS.\n"
                            + "\n"
                            + "Note: Question/Surprise tiles can be activated only once (then marked as USED).\n");

            // 🧩 Tile Types
            appendSection(text, "🧩", new Color(255, 170, 255),
                    " Tile Types:\n",
                    "• Empty – safe tile.\n"
                            + "• Number – shows how many mines touch the tile.\n"
                            + "• Mine 💣 – revealing by click costs 1 shared life.\n"
                            + "• Question ❓ – answer a question for rewards or penalties.\n"
                            + "• Surprise 🎁 – random good or bad effect.\n");

            // ❤️ Shared Lives
            appendSection(text, "❤️", new Color(255, 100, 140),
                    " Shared Lives:\n",
                    "Both players share the same hearts ❤️.\n"
                            + "Revealing a mine by clicking decreases 1 life.\n"
                            + "When lives reach 0 → GAME OVER.\n"
                            + "\n"
                            + "Maximum lives: 10.\n"
                            + "Any heart gained above the maximum is automatically converted into score.\n"
                            + "Each extra heart equals the activation cost of a Question/Surprise station.\n");

            // ⭐ Scoring & Activation Cost
            appendSection(text, "⭐", new Color(255, 230, 120),
                    " Scoring & Activation Cost:\n",
                    "Score is shared by both players.\n"
                            + "• Revealing safe tiles increases score.\n"
                            + "• Question and Surprise stations have an activation cost of 5 POINTS.\n"
                            + "• Correct answers and positive surprises increase score.\n"
                            + "• Wrong answers and negative surprises may reduce score or lives.\n");

            // 🎮 Difficulty Levels & Board Sizes
            appendSection(text, "🎮", new Color(120, 255, 180),
                    " Difficulty Levels & Board Sizes:\n",
                    "The game includes three difficulty levels:\n"
                            + "• EASY – Board size: 9 × 9 (81 tiles)\n"
                            + "• MEDIUM – Board size: 13 × 13 (169 tiles)\n"
                            + "• HARD – Board size: 16 × 16 (256 tiles)\n"
                            + "\n"
                            + "Higher difficulty means more mines, fewer lives, and higher risk.\n");

            // 🏁 Game End
            appendSection(text, "🏁", new Color(255, 215, 0),
                    " Game End:\n",
                    "The game ends immediately when ONE of the following occurs:\n"
                            + "1) One player reveals ALL mines on their own board.\n"
                            + "2) Shared lives ❤️ reach ZERO.\n"
                            + "\n"
                            + "At game end:\n"
                            + "• Both boards are revealed automatically.\n"
                            + "• Remaining hearts ❤️ are converted into score.\n"
                            + "  Each remaining heart equals 5 points (activation cost).\n");

            JScrollPane scroll = new JScrollPane(text);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            panel.add(scroll, BorderLayout.CENTER);
            
            text.setCaretPosition(0);

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

        private void appendSection(JTextPane pane, String icon, Color iconColor,
                                   String title, String body) {

            StyledDocument doc = pane.getStyledDocument();

            try {
                Style iconStyle = pane.addStyle("icon", null);
                StyleConstants.setForeground(iconStyle, iconColor);
                StyleConstants.setBold(iconStyle, true);
                StyleConstants.setFontSize(iconStyle, 20);

                Style titleStyle = pane.addStyle("title", null);
                StyleConstants.setForeground(titleStyle, Color.WHITE);
                StyleConstants.setBold(titleStyle, true);
                StyleConstants.setFontSize(titleStyle, 16);

                Style bodyStyle = pane.addStyle("body", null);
                StyleConstants.setForeground(bodyStyle, Color.WHITE);
                StyleConstants.setFontSize(bodyStyle, 14);

                doc.insertString(doc.getLength(), icon + " ", iconStyle);
                doc.insertString(doc.getLength(), title + "\n", titleStyle);
                doc.insertString(doc.getLength(), body + "\n\n", bodyStyle);

            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }
}
