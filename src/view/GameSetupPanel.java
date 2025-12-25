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

    private final JButton nextBtn = new JButton("Next");
    private final JButton backBtn = new JButton("Back");

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
        RoundedCardPanel card = new RoundedCardPanel();
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

        styleSecondaryButton(backBtn);
        stylePrimaryButton(nextBtn);
        setPrimaryEnabled(nextBtn, false);

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
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBackground(new Color(22, 36, 56));

        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        Border focused = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 255, 170), 2, true),
                BorderFactory.createEmptyBorder(9, 11, 9, 11)
        );

        tf.setBorder(normal);

        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { tf.setBorder(focused); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { tf.setBorder(normal); }
        });
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(new Font("Segoe UI", Font.BOLD, 15));
        box.setForeground(Color.WHITE);
        box.setBackground(new Color(22, 36, 56));
        box.setFocusable(false);
        box.setPreferredSize(new Dimension(260, 42));

        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        // arrow button
        box.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton("▼");
                arrow.setBorderPainted(false);
                arrow.setFocusPainted(false);
                arrow.setContentAreaFilled(false);
                arrow.setOpaque(false);
                arrow.setForeground(new Color(220, 230, 245));
                arrow.setFont(new Font("Segoe UI", Font.BOLD, 13));
                arrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return arrow;
            }
        });

        box.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel r = new JLabel(value == null ? "" : value.toString());
            r.setOpaque(true);
            r.setFont(new Font("Segoe UI", Font.BOLD, 15));
            r.setForeground(Color.WHITE);
            r.setBackground(isSelected ? new Color(40, 70, 105) : new Color(22, 36, 56));
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
            btn.setBackground(new Color(60, 120, 200));
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
                    BorderFactory.createEmptyBorder(10, 26, 10, 26)
            ));
        } else {
            btn.setBackground(new Color(60, 80, 105));
            btn.setForeground(new Color(190, 200, 215));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        }
    }

    // Custom UI components

    /** Rounded card with shadow */
    private static class RoundedCardPanel extends JPanel {
        RoundedCardPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(10, 12, w - 20, h - 18, 24, 24);

            g2.setColor(new Color(10, 20, 35, 220));
            g2.fillRoundRect(6, 6, w - 12, h - 12, 22, 22);

            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(6, 6, w - 12, h - 12, 22, 22);

            g2.dispose();
            super.paintComponent(g);
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

            g2.setColor(new Color(0, 150, 255, 70));
            g2.drawString(text, x + 2, y + 2);

            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}
