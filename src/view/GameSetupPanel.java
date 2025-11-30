package view;

import model.Difficulty;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * מסך הגדרת משחק בתוך אותו חלון (לא JDialog).
 *  - שם שחקן 1
 *  - שם שחקן 2
 *  - רמת קושי
 *  - Back -> חזרה לתפריט
 *  - Next -> התחלת משחק (שני לוחות)
 */
public class GameSetupPanel extends JPanel {

    private final JTextField player1Field = new JTextField();
    private final JTextField player2Field = new JTextField();
    private final JComboBox<Difficulty> difficultyBox =
            new JComboBox<>(Difficulty.values());

    private final JButton nextBtn = new JButton("Next");
    private final JButton backBtn = new JButton("Back");

    private final MainMenuGUI parent;

    public GameSetupPanel(MainMenuGUI parent) {
        this.parent = parent;
        buildUi();
    }

    private void buildUi() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        // כותרת
        JLabel title = new JLabel("New Game", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        // טופס – שמות + רמת קושי
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 20));
        form.setOpaque(false);

        JLabel p1Label = new JLabel("Player 1 name:");
        JLabel p2Label = new JLabel("Player 2 name:");
        JLabel diffLabel = new JLabel("Difficulty:");

        for (JLabel lbl : new JLabel[]{p1Label, p2Label, diffLabel}) {
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }

        styleTextField(player1Field);
        styleTextField(player2Field);
        difficultyBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        form.add(p1Label);
        form.add(player1Field);
        form.add(p2Label);
        form.add(player2Field);
        form.add(diffLabel);
        form.add(difficultyBox);

        add(form, BorderLayout.CENTER);

        // כפתורי Back (שמאל) ו-Next (ימין תחתון)
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        styleSecondaryButton(backBtn);
        leftPanel.add(backBtn);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        stylePrimaryButton(nextBtn);
        nextBtn.setEnabled(false);  // בהתחלה מכובה
        rightPanel.add(nextBtn);

        buttonsPanel.add(leftPanel, BorderLayout.WEST);
        buttonsPanel.add(rightPanel, BorderLayout.EAST);

        add(buttonsPanel, BorderLayout.SOUTH);

        // --- לוגיקה ---

        // Back – לחזרה לתפריט הראשי
        backBtn.addActionListener(e -> parent.showMainMenu());

        // הפעלת כפתור Next רק כששני השמות מלאים
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateNextButton(); }
            @Override public void removeUpdate(DocumentEvent e) { updateNextButton(); }
            @Override public void changedUpdate(DocumentEvent e) { updateNextButton(); }
        };
        player1Field.getDocument().addDocumentListener(dl);
        player2Field.getDocument().addDocumentListener(dl);

        // מה קורה כשנלחץ Next
        nextBtn.addActionListener(e -> onNext());
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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

    /** מפעיל / מכבה את Next לפי האם השדות מלאים */
    private void updateNextButton() {
        boolean filled =
                !player1Field.getText().trim().isEmpty() &&
                !player2Field.getText().trim().isEmpty();
        nextBtn.setEnabled(filled);
    }

    /** לוגיקה של Next – רק אם הכול מלא, מתחילים משחק */
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

        // כאן כל המידע קיים – מתחילים משחק באותו חלון
        parent.startGame(p1, p2, diff);
    }
}
