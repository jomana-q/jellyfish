package view;

import model.Question;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * מסך השאלה – דיאלוג מודאלי שמקבל Question ומחזיר true/false אם המשתמש צדק.
 * כולל טיימר של 30 שניות ומשוב צבעוני (ירוק/אדום) על הכפתורים.
 */
public class QuestionDialog {

    /**
     * מציג חלון שאלה ומחזיר:
     *  true  – אם המשתמש לחץ על התשובה הנכונה
     *  false – אם טעה או שהזמן נגמר
     */
    public static boolean showQuestionDialog(Component parent, Question question) {

        final boolean[] answered = new boolean[1];
        final boolean[] correct  = new boolean[1];
        answered[0] = false;
        correct[0]  = false;

        // דיאלוג מודאלי מעל החלון של המשחק
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Question",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // ======= TOP BAR: אייקון + כותרת + טיימר =======
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        topPanel.setBackground(new Color(245, 249, 255));

        JLabel iconLabel = new JLabel("?", SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(220, 235, 255));
        iconLabel.setForeground(new Color(30, 95, 160));
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        iconLabel.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 245)));

        JLabel titleLabel = new JLabel("Question", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(30, 30, 30));

        JLabel timerLabel = new JLabel("Time: 30s", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timerLabel.setForeground(new Color(120, 120, 120));

        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);

        dialog.add(topPanel, BorderLayout.NORTH);

        // ======= טקסט השאלה =======
        JLabel questionLabel = new JLabel(
                "<html><div style='text-align:center; width:350px;'>"
                        + question.getQuestionText() +
                        "</div></html>",
                SwingConstants.CENTER
        );
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        dialog.add(questionLabel, BorderLayout.CENTER);

        // ======= תשובות =======
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        answersPanel.setBackground(Color.WHITE);

        String[] answers = question.getAnswers();
        final int correctIndex = question.getCorrectAnswerIndex();

        // נשתמש ברשימה כדי שנוכל לשנות צבעים אחרי הבחירה
        List<JButton> answerButtons = new ArrayList<>();

        // טיימר – 30 שניות
        final int[] remainingSeconds = {30};
        Timer timer = new Timer(1000, e -> {
            remainingSeconds[0]--;
            timerLabel.setText("Time: " + remainingSeconds[0] + "s");

            if (remainingSeconds[0] <= 0) {
                ((Timer) e.getSource()).stop();
                if (!answered[0]) {
                    answered[0] = true;
                    correct[0] = false;
                }
                dialog.dispose();
            }
        });

        // יצירת כפתורי תשובות
        for (int i = 0; i < answers.length; i++) {
            String text = answers[i];
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            btn.setFocusPainted(false);
            btn.setBackground(new Color(240, 245, 252));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 235)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));

            final int answerIndex = i;
            btn.addActionListener(e -> {
                if (answered[0]) return; // כבר נענתה

                answered[0] = true;
                correct[0] = (answerIndex == correctIndex);

                // עצירת טיימר
                timer.stop();

                // השבתת כל הכפתורים
                for (JButton b : answerButtons) {
                    b.setEnabled(false);
                }

                // צבעים: ירוק לתשובה הנכונה, אדום לתשובה השגויה שנבחרה
                Color green = new Color(120, 190, 120);
                Color red   = new Color(220, 110, 110);

                if (correct[0]) {
                    JButton correctBtn = answerButtons.get(correctIndex);
                    correctBtn.setBackground(green);
                    correctBtn.setForeground(Color.WHITE);
                } else {
                    JButton clickedBtn = answerButtons.get(answerIndex);
                    clickedBtn.setBackground(red);
                    clickedBtn.setForeground(Color.WHITE);

                    JButton correctBtn = answerButtons.get(correctIndex);
                    correctBtn.setBackground(green);
                    correctBtn.setForeground(Color.WHITE);
                }

                // מחכים קצת כדי שהשחקן יראה את הצבעים ואז סוגרים
                Timer closeTimer = new Timer(700, ev -> {
                    ((Timer) ev.getSource()).stop();
                    dialog.dispose();
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            });

            answerButtons.add(btn);
            answersPanel.add(btn);
        }

        dialog.add(answersPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        // להתחיל את הטיימר רק כשהדיאלוג מוכן
        timer.start();
        dialog.setVisible(true); // בלוקינג עד שסוגרים

        return correct[0];
    }
}
