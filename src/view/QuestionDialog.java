package view;

import model.Question;

import javax.swing.*;
import java.awt.*;

/**
 * מסך השאלה – דיאלוג מודאלי שמקבל Question ומחזיר true/false אם המשתמש צדק.
 */
public class QuestionDialog {

    /**
     * מציג חלון שאלה ומחזיר:
     *  true  – אם המשתמש לחץ על התשובה הנכונה
     *  false – אחרת
     */
    public static boolean showQuestionDialog(Component parent, Question question) {
        final boolean[] correct = new boolean[1];

        // דיאלוג מודאלי מעל החלון של המשחק
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(parent),
                "Question",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // כותרת
        JLabel title = new JLabel("Question", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // טקסט השאלה – עטוף ב-HTML כדי שישבר שורות יפה
        JLabel questionLabel = new JLabel(
                "<html><div style='text-align:center; width:350px;'>"
                        + question.getQuestionText() +
                "</div></html>",
                SwingConstants.CENTER
        );
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(questionLabel, BorderLayout.CENTER);
        dialog.add(topPanel, BorderLayout.NORTH);

        // תשובות – 4 כפתורים בגריד 2x2
        JPanel answersPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        answersPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        String[] answers = question.getAnswers();

        for (int i = 0; i < answers.length; i++) {
            String text = answers[i];
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            btn.setFocusPainted(false);

            final int answerIndex = i;
            btn.addActionListener(e -> {
                correct[0] = (answerIndex == question.getCorrectAnswerIndex());
                dialog.dispose();
            });

            answersPanel.add(btn);
        }

        dialog.add(answersPanel, BorderLayout.CENTER);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true); // חוסם עד שסוגרים

        return correct[0];
    }
}
