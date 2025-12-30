package view;

import model.Difficulty;
import model.ThemeManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class GameSetupPanel extends JPanel {

    private final JTextField player1Field = new JTextField();
    private final JTextField player2Field = new JTextField();
    private final JComboBox<Difficulty> difficultyBox = new JComboBox<>(Difficulty.values());

    private  JButton nextBtn = new JButton("Next");
    private  JButton backBtn = new JButton("Back");

    private final MainMenuGUI parent;

    public GameSetupPanel(MainMenuGUI parent) {
        this.parent = parent;
        buildUi();
    }

    private void buildUi() {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Card (rounded + shadow)
        JPanel card = new JPanel();
        card.setOpaque(false); 
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(26, 30, 24, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(14, 10, 14, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.weightx = 1.0;

        GlowTitle title = new GlowTitle("New Game");
        title.setPreferredSize(new Dimension(360, 48));
        card.add(title, gc);

        // subtitle
        gc.gridy++;
        JLabel subtitle = createDynamicLabel("Enter players and choose difficulty", new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setForeground(new Color(200, 215, 235));
        card.add(subtitle, gc);

        // Labels (dynamic for theme)
        JLabel p1Label = createDynamicLabel("Player 1 name:", new Font("Segoe UI", Font.BOLD, 15));
        JLabel p2Label = createDynamicLabel("Player 2 name:", new Font("Segoe UI", Font.BOLD, 15));
        JLabel diffLabel = createDynamicLabel("Difficulty:", new Font("Segoe UI", Font.BOLD, 15));

        gc.gridwidth = 1;

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        card.add(p1Label, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        styleTextField(player1Field);
        card.add(player1Field, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        card.add(p2Label, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        styleTextField(player2Field);
        card.add(player2Field, gc);

        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        card.add(diffLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        styleCombo(difficultyBox);
        card.add(difficultyBox, gc);

        // Buttons
        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(16, 10, 6, 10);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttons.setOpaque(false);
        
        backBtn = createStyledButton("Back");
        nextBtn = createStyledButton("Next");

        nextBtn.setEnabled(false);
        nextBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        buttons.add(backBtn);
        buttons.add(nextBtn);

        card.add(buttons, gc);

        // add card to panel
        add(card, new GridBagConstraints());

        // Logic
        backBtn.addActionListener(e -> parent.showMainMenu());

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateNextButton(); }
            @Override public void removeUpdate(DocumentEvent e) { updateNextButton(); }
            @Override public void changedUpdate(DocumentEvent e) { updateNextButton(); }
        };
        player1Field.getDocument().addDocumentListener(dl);
        player2Field.getDocument().addDocumentListener(dl);

        player1Field.addActionListener(e -> onNext());
        player2Field.addActionListener(e -> onNext());
        
        nextBtn.addActionListener(e -> onNext());
    }

    //Validation
    private void updateNextButton() {
        boolean filled =
                !player1Field.getText().trim().isEmpty() &&
                !player2Field.getText().trim().isEmpty();

        setPrimaryEnabled(nextBtn, filled);
    }

    //Next
    private void onNext() {
        String p1 = player1Field.getText().trim();
        String p2 = player2Field.getText().trim();
        Difficulty diff = (Difficulty) difficultyBox.getSelectedItem();

        if (p1.isEmpty() || p2.isEmpty() || diff == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill both player names and choose difficulty.",
                    "Missing data",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        parent.startGame(p1, p2, diff);
    }

    // Dynamic label (theme-aware)
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

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setPreferredSize(new Dimension(260, 42));

        // בדיקה האם המצב כהה או בהיר (Dark/Light Check)
        boolean isDark = ThemeManager.getInstance().isDarkMode();

        // קביעת צבעים דינמית לפי המצב
        Color textColor = isDark ? Color.WHITE : Color.BLACK;
        Color bgColor = isDark ? new Color(22, 36, 56) : new Color(240, 240, 240); // כהה או אפור בהיר
        Color caretColor = isDark ? Color.WHITE : Color.BLACK;
        
        // צבע מסגרת: לבן שקוף בכהה, שחור שקוף בבהיר
        Color borderColor = isDark ? new Color(255, 255, 255, 70) : new Color(0, 0, 0, 70);

        tf.setForeground(textColor);
        tf.setCaretColor(caretColor);
        tf.setBackground(bgColor);

        // גבול רגיל (Border)
        javax.swing.border.Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        // גבול בפוקוס (Focus) - נשאר כחול בשני המצבים
        javax.swing.border.Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 255, 170), 2, true),
                BorderFactory.createEmptyBorder(9, 11, 9, 11)
        );

        tf.setBorder(normal);

        // מאזין לשינוי גבול בעת פוקוס
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(focused); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(normal); }
        });
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.BOLD, 15));
        box.setFocusable(false);
        box.setPreferredSize(new Dimension(260, 42));

        // בדיקה האם המצב כהה (בודקים מול המנהל)
        boolean isDark = ThemeManager.getInstance().isDarkMode();

        // קביעת צבעים דינמית
        Color bgColor = isDark ? new Color(22, 36, 56) : new Color(240, 240, 240); // רקע
        Color textColor = isDark ? Color.WHITE : Color.BLACK;                      // טקסט
        Color borderColor = isDark ? new Color(255, 255, 255, 70) : new Color(0, 0, 0, 70); // מסגרת
        Color arrowColor = isDark ? new Color(220, 230, 245) : Color.BLACK;        // חץ

        box.setForeground(textColor);
        box.setBackground(bgColor);

        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // עיצוב כפתור החץ (UI)
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton("▼");
                arrow.setBorderPainted(false);
                arrow.setFocusPainted(false);
                arrow.setContentAreaFilled(false);
                arrow.setOpaque(false);
                arrow.setForeground(arrowColor); // צבע החץ משתנה לפי הת'ים
                arrow.setFont(new Font("Segoe UI", Font.BOLD, 13));
                arrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return arrow;
            }
        });

        // עיצוב הרשימה הנפתחת (Renderer)
        box.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel r = new JLabel(value == null ? "" : value.toString());
            r.setOpaque(true);
            r.setFont(new Font("Segoe UI", Font.BOLD, 15));
            
            // צבעים בתוך הרשימה עצמה
            if (isSelected) {
                // צבע בחירה (תמיד כחול כהה כדי שיהיה ברור)
                r.setBackground(new Color(40, 70, 105));
                r.setForeground(Color.WHITE);
            } else {
                // צבע רגיל תואם לרקע
                r.setBackground(bgColor);
                r.setForeground(textColor);
            }
            
            r.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return r;
        });
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(80, 145, 230));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(60, 120, 200));
            }
        });

        btn.setBackground(new Color(60, 120, 200));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(70, 70, 70));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setPrimaryEnabled(JButton btn, boolean enabled) {
        btn.setEnabled(enabled);
        if (enabled) {
            btn.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            btn.setForeground(Color.GRAY);
        }
    }

    // Custom UI components

    /** Rounded card with shadow */
    private static class RoundedCardPanel extends JPanel {
        RoundedCardPanel() { setOpaque(false); }

     // בתוך המחלקה הפנימית RoundedCardPanel (בסוף הקובץ)

        @Override
        protected void paintComponent(Graphics g) {
            // خليناها فاضية عشان ما ترسم ولا اشي (شفافة بالكامل)
        }
    }

    private static class GlowTitle extends JComponent {
        private final String text;
        private final Font font = new Font("Segoe UI", Font.BOLD, 30);

        GlowTitle(String text) {
            this.text = text;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // ⭐ תיקון: בדיקה האם המצב כהה או בהיר
            boolean isDark = ThemeManager.getInstance().isDarkMode();

            // צבע הטקסט הראשי: לבן בכהה, אפור-כהה בבהיר
            Color mainColor = isDark ? Color.WHITE : new Color(30, 30, 30);
            
            // צבע הצל/הילה: כחול בכהה, צל אפור עדין בבהיר
            Color glowColor = isDark ? new Color(0, 150, 255, 70) : new Color(0, 0, 0, 30);

            // ציור הצל (Shadow)
            g2.setColor(glowColor);
            g2.drawString(text, x + 2, y + 2);

            // ציור הטקסט (Main Text)
            g2.setColor(mainColor);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 0, 0, 150));
        
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false); 
        
        button.setPreferredSize(new Dimension(140, 45));
        
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 200, 255), 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(100, 200, 255)); // يصير سماوي
                    button.setForeground(Color.BLACK);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(0, 0, 0, 150)); // يرجع أسود شفاف
                    button.setForeground(Color.WHITE);
                }
            }
        });
        return button;
    }
}
