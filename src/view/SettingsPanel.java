package view;

import controller.SoundManager;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * מסך הגדרות (Settings) - מאפשר שליטה בווליום, בחירת קובץ מוזיקה, השתקה ושינוי ערכת נושא.
 */
public class SettingsPanel extends JPanel {

    private final MainMenuGUI parent;
    
    // רכיבי הממשק (GUI Components)
    private JSlider volumeSlider;
    private JCheckBox muteCheckBox;
    private JButton themeToggle; // כפתור להחלפת ערכת נושא (Toggle)
    private JButton selectMusicBtn; // כפתור לבחירת מוזיקה מהמחשב
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
        
        // הוספת מאזין לשינוי הווליום + תיקון הבעיה הגרפית (repaint)
        volumeSlider.addChangeListener(e -> {
            SoundManager.getInstance().setVolume(volumeSlider.getValue());
            cardPanel.repaint(); // תיקון קריטי: מונע מריחות בגרפיקה
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
        muteCheckBox.addActionListener(e -> {
            SoundManager.getInstance().setMuted(muteCheckBox.isSelected());
        });

     // --- D. ערכת נושא (Theme) ---
        JLabel themeLabel = new JLabel("Game Theme 🎨:");
        styleLabel(themeLabel);

        // יצירת כפתור טוגל (Toggle) במקום רשימה נפתחת
        themeToggle = new JButton();
        themeToggle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        themeToggle.setFocusPainted(false);
        themeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // קבלת המצב הנוכחי מהמנהל (ThemeManager)
        boolean currentMode = model.ThemeManager.getInstance().isDarkMode();
        
        // עדכון עיצוב הכפתור בהתאם למצב ההתחלתי
        updateThemeButtonLook(themeToggle, currentMode);

        // מאזין ללחיצה על הכפתור - שינוי חזותי בלבד
        themeToggle.addActionListener(e -> {
            // בדיקה האם המצב כרגע הוא "כהה" לפי הטקסט
            boolean isCurrentlyDark = themeToggle.getText().contains("Dark");
            
            // הפיכת המצב (אם כהה -> להפוך לבהיר, ולהפך)
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
        cardPanel.add(themeToggle, gbc); // הוספת כפתור הטוגל לפאנל
        
        centerWrapper.add(cardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // 3. כפתורים למטה (Save / Back)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonsPanel.setOpaque(false);

        saveBtn = new JButton("Save Changes");
        styleButton(saveBtn, new Color(80, 120, 220)); 

        backBtn = new JButton("Back");
        styleButton(backBtn, new Color(70, 80, 100)); // אדום

        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);

        // לוגיקת כפתורים
        backBtn.addActionListener(e -> parent.showMainMenu());
     // שמירה ועדכון הת'ים
        saveBtn.addActionListener(e -> {
            // בדיקה מה המשתמש בחר: אינדקס 0 = Dark, אינדקס 1 = Light
        	boolean isDark = themeToggle.getText().contains("Dark");            
            // עדכון המנהל (ThemeManager)
            model.ThemeManager.getInstance().setDarkMode(isDark);
            
            // הודעה למשתמש
            JOptionPane.showMessageDialog(this, "Settings Saved! \nההגדרות נשמרו בהצלחה! ✅");
            
            // ⭐ קריאה לפונקציה בחלון הראשי שתרענן את הצבעים
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
        
        // סינון קבצים - הצגת קבצי WAV בלבד
        FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV Sound Files", "wav");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // הפעלת השיר הנבחר
            SoundManager.getInstance().stopMusic(); 
            SoundManager.getInstance().playBackgroundMusic(selectedFile.getAbsolutePath());
            
            JOptionPane.showMessageDialog(this, "Now Playing: \n" + selectedFile.getName() + " 🎶");
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
     * פונקציית עזר לעדכון עיצוב כפתור הערכה (Dark/Light)
     * משנה את הטקסט והצבעים בהתאם למצב הנבחר.
     */
    private void updateThemeButtonLook(JButton btn, boolean isDark) {
        if (isDark) {
            // עיצוב עבור מצב כהה (Dark Mode) 🌙
            btn.setText("Dark Mode 🌙");
            btn.setBackground(new Color(60, 60, 80)); // כחול כהה
            btn.setForeground(new Color(220, 220, 255)); // טקסט בהיר
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        } else {
            // עיצוב עבור מצב בהיר (Light Mode) ☀️
            btn.setText("Light Mode ☀️");
            btn.setBackground(new Color(255, 250, 240)); // לבן שמנת
            btn.setForeground(new Color(220, 110, 160));   // כתום/זהב
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(240, 180, 210), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
            ));
        }
    }
}