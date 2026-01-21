package view;

import model.Question;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class QuestionDialog {

    // size & time
    private static final int CARD_W = 660;
    private static final int CARD_H = 440;
    private static final int TOTAL_SECONDS = 20;

    // theme (blue glass)
    private static final Color CARD_BG     = new Color(225, 242, 255, 235);
    private static final Color CARD_BORDER = new Color(90, 155, 220, 170);

    // text
    private static final Color TITLE_TXT    = new Color(18, 55, 105);
    private static final Color QUESTION_TXT = new Color(25, 55, 95);

    // answer card
    private static final Color ANS_BG       = new Color(255, 255, 255, 215);
    private static final Color ANS_BG_HOVER = new Color(240, 248, 255, 240);
    private static final Color ANS_BG_DOWN  = new Color(230, 243, 255, 245);
    private static final Color ANS_BORDER   = new Color(120, 175, 230, 150);

    // badge
    private static final Color BADGE_BG  = new Color(210, 235, 255, 255);
    private static final Color BADGE_TXT = new Color(18, 55, 105);

    // overlay
    private static final Color OVERLAY_DARK = new Color(0, 0, 0, 155);

    // fonts 
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_QUESTION = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_ANS      = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_BADGE    = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_SMALL    = new Font("Segoe UI", Font.BOLD, 14);
    
    // result colors (correct/incorrect)
    private static final Color CORRECT_BG     = new Color(210, 255, 220, 245);
    private static final Color CORRECT_BORDER = new Color(60, 180, 95, 220);
    private static final Color WRONG_BG       = new Color(255, 220, 220, 245);
    private static final Color WRONG_BORDER   = new Color(215, 70, 70, 220);


    public static boolean showQuestionDialog(Component parent, Question q) {
        Window owner = SwingUtilities.getWindowAncestor(parent);

        final JDialog dialog = new JDialog(owner, "Question", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // ===== Full-screen overlay panel =====
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                applyTextHints(g2);
                g2.setColor(OVERLAY_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        overlay.setBorder(new EmptyBorder(24, 24, 24, 24));

        // center card
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Card 
        GlassCard card = new GlassCard();
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 14, 18));

        // Header 
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // left (icon + title)
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("?");
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(42, 42));
        icon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icon.setForeground(TITLE_TXT);
        icon.setOpaque(true);
        icon.setBackground(new Color(255, 255, 255, 175));
        icon.setBorder(BorderFactory.createLineBorder(new Color(120, 175, 230, 140), 1, true));

        JLabel title = new JLabel("Question");
        title.setFont(FONT_TITLE);
        title.setForeground(TITLE_TXT);

        left.add(icon);
        left.add(title);

     // right (level + timer + close)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(270, 42)); // was 180 -> caused wrapping

        LevelChip levelChip = new LevelChip(levelLabel(q.getLevel()));
        TimeChip timeChip   = new TimeChip(TOTAL_SECONDS);
        CloseIconButton close = new CloseIconButton(); // keep default size (34x34)

        right.add(levelChip);
        right.add(timeChip);
        right.add(close);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        header.setPreferredSize(new Dimension(1, 52));

        // Question text 
        JLabel questionLbl = new JLabel(wrapHtml(safe(q.getQuestionText()), 46), SwingConstants.CENTER);
        questionLbl.setFont(FONT_QUESTION);
        questionLbl.setForeground(QUESTION_TXT);
        questionLbl.setBorder(new EmptyBorder(14, 22, 10, 22));
        questionLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // divider line
        JComponent divider = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyTextHints(g2);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(90, 155, 220, 120));
                int w = getWidth();
                g2.fillRoundRect(70, 0, Math.max(0, w - 140), 2, 2, 2);
                g2.dispose();
            }
        };
        divider.setPreferredSize(new Dimension(1, 12));
        divider.setOpaque(false);

        // Answers 
        JPanel answers = new JPanel(new GridLayout(2, 2, 14, 14));
        answers.setOpaque(false);
        answers.setBorder(new EmptyBorder(10, 12, 10, 12));

        String[] ans = (q.getAnswers() == null) ? new String[]{"", "", "", ""} : q.getAnswers();
        String a = safe(ans.length > 0 ? ans[0] : "");
        String b = safe(ans.length > 1 ? ans[1] : "");
        String c = safe(ans.length > 2 ? ans[2] : "");
        String d = safe(ans.length > 3 ? ans[3] : "");

        AnswerCard btnA = new AnswerCard("A", a);
        AnswerCard btnB = new AnswerCard("B", b);
        AnswerCard btnC = new AnswerCard("C", c);
        AnswerCard btnD = new AnswerCard("D", d);

        answers.add(btnA);
        answers.add(btnB);
        answers.add(btnC);
        answers.add(btnD);

        // Progress bar
        TimeBar timeBar = new TimeBar(TOTAL_SECONDS);
        timeBar.setPreferredSize(new Dimension(1, 10));

        // center stack
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(questionLbl);
        center.add(divider);
        center.add(Box.createVerticalStrut(8));
        center.add(answers);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(timeBar, BorderLayout.SOUTH);

        overlay.add(card, gbc);
        dialog.setContentPane(overlay);

        // Logic
        final boolean[] answered = { false };
        final boolean[] result   = { false };

        close.addActionListener(e -> {
            answered[0] = true;
            result[0] = false;
            dialog.dispose();
        });

        // Timer
        final int[] leftSec = { TOTAL_SECONDS };
        timeChip.setSeconds(leftSec[0]);
        timeBar.setSecondsLeft(leftSec[0]);

        Timer t = new Timer(1000, e -> {
            leftSec[0]--;
            timeChip.setSeconds(leftSec[0]);
            timeBar.setSecondsLeft(leftSec[0]);

            if (leftSec[0] <= 0) {
                ((Timer) e.getSource()).stop();
                answered[0] = true;
                result[0] = false;
                dialog.dispose();
            }
        });
        t.setInitialDelay(1000);
        t.start();

        ActionListener pick = ae -> {
            if (answered[0]) return;
            answered[0] = true;

            // stop timer so user sees the result
            if (t.isRunning()) t.stop();

            btnA.setEnabled(false);
            btnB.setEnabled(false);
            btnC.setEnabled(false);
            btnD.setEnabled(false);

            AnswerCard[] cards = { btnA, btnB, btnC, btnD };

            int chosenIdx = -1;
            Object src = ae.getSource();
            if (src == btnA) chosenIdx = 0;
            if (src == btnB) chosenIdx = 1;
            if (src == btnC) chosenIdx = 2;
            if (src == btnD) chosenIdx = 3;

            // always show the correct one in green
            int correctIdx = -1;
            for (int i = 0; i < 4; i++) {
                if (q.isCorrect(i)) { correctIdx = i; break; }
            }
            if (correctIdx != -1) cards[correctIdx].markCorrect();

            // mark chosen (green if correct, red if wrong)
            if (chosenIdx != -1) {
                cards[chosenIdx].setChosen(true);
                if (!q.isCorrect(chosenIdx)) {
                    cards[chosenIdx].markWrong();
                } else {
                    cards[chosenIdx].markCorrect();
                }
            }

//     result[0] = (chosenIdx != -1) && q.isCorrect(chosenIdx);

            result[0] = (chosenIdx != -1)&& q.isCorrect(chosenIdx);

            // keep dialog open briefly so colors are visible
            Timer closeLater = new Timer(900, ev -> dialog.dispose());
            closeLater.setRepeats(false);
            closeLater.start();
        };

        btnA.addActionListener(pick);
        btnB.addActionListener(pick);
        btnC.addActionListener(pick);
        btnD.addActionListener(pick);

        // ESC closes
        dialog.getRootPane().registerKeyboardAction(
                e -> close.doClick(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (t.isRunning()) t.stop();
            }
        });

        // full overlay size
        if (owner != null) {
            dialog.setSize(owner.getSize());
            dialog.setLocation(owner.getLocationOnScreen());
        } else {
            dialog.setSize(900, 600);
            dialog.setLocationRelativeTo(parent);
        }

        dialog.setVisible(true);
        return result[0];
    }

    // helpers

    private static String safe(String s) { return (s == null) ? "" : s.trim(); }

    private static String wrapHtml(String text, int maxCharsPerLine) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder("<html><div style='text-align:center; line-height:1.25;'>");
        int count = 0;
        for (String word : text.split("\\s+")) {
            if (count + word.length() > maxCharsPerLine) {
                sb.append("<br>");
                count = 0;
            }
            sb.append(word).append(" ");
            count += word.length() + 1;
        }
        sb.append("</div></html>");
        return sb.toString();
    }

    private static void applyTextHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    // UI components

    private static class GlassCard extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            // shadow
            g2.setColor(new Color(0, 0, 0, 65));
            g2.fillRoundRect(10, 12, w - 16, h - 16, arc, arc);

            // glass bg
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            // border
            g2.setColor(CARD_BORDER);
            understandingStroke(g2, 3f);
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            g2.dispose();
        }
    }

    private static void understandingStroke(Graphics2D g2, float w) {
        g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    // timer chip
    private static class TimeChip extends JComponent {
        private int seconds;

        TimeChip(int seconds) {
            this.seconds = seconds;
            setPreferredSize(new Dimension(92, 34));
            setMinimumSize(new Dimension(92, 34));
            setOpaque(false);
            setToolTipText("Time left");
        }

        void setSeconds(int s) {
            seconds = Math.max(0, s);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, 190));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.setColor(new Color(120, 175, 230, 150));
            understandingStroke(g2, 1.5f);
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.setColor(new Color(70, 140, 210, 220));
            g2.fillOval(10, 9, 16, 16);
            g2.setColor(new Color(255, 255, 255, 235));
            understandingStroke(g2, 2f);
            g2.drawLine(18, 12, 18, 17);
            g2.drawLine(18, 17, 22, 17);

            String txt = seconds + "s";
            g2.setFont(FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int tx = 34;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(TITLE_TXT);
            g2.drawString(txt, tx, ty);

            g2.dispose();
        }
    }

    // close button
    private static class CloseIconButton extends JButton {
        private boolean hover = false;
        private boolean down  = false;

        CloseIconButton() {
            setPreferredSize(new Dimension(34, 34));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText("Close");

            setRolloverEnabled(false);
            setBorder(BorderFactory.createEmptyBorder());

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ down = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (hover) {
                g2.setColor(new Color(120, 175, 230, down ? 70 : 35));
                g2.fillRoundRect(4, 4, w - 8, h - 8, 10, 10);
            }

            g2.setColor(TITLE_TXT);
            understandingStroke(g2, 2.6f);
            int pad = 11;
            g2.drawLine(pad, pad, w - pad, h - pad);
            g2.drawLine(w - pad, pad, pad, h - pad);

            g2.dispose();
        }
    }
    
    private static String[] wrapTwoLines(String text, FontMetrics fm, int maxW) {
        if (text == null) return new String[]{"", ""};
        text = text.trim();
        if (text.isEmpty()) return new String[]{"", ""};

        java.util.List<String> words = java.util.Arrays.asList(text.split("\\s+"));
        StringBuilder line1 = new StringBuilder();
        StringBuilder line2 = new StringBuilder();

        // build line1
        int i = 0;
        for (; i < words.size(); i++) {
            String w = words.get(i);
            String candidate = line1.length() == 0 ? w : (line1 + " " + w);
            if (fm.stringWidth(candidate) <= maxW) {
                line1.setLength(0);
                line1.append(candidate);
            } else {
                break;
            }
        }

        // build line2
        for (; i < words.size(); i++) {
            String w = words.get(i);
            String candidate = line2.length() == 0 ? w : (line2 + " " + w);
            if (fm.stringWidth(candidate) <= maxW) {
                line2.setLength(0);
                line2.append(candidate);
            } else {
                break;
            }
        }

        // if still remaining words -> ellipsis on line2
        if (i < words.size()) {
            String base = line2.toString();
            if (base.isEmpty()) base = line1.toString();
            if (base.isEmpty()) base = words.get(0);

            String trimmed = base;
            while (trimmed.length() > 2 && fm.stringWidth(trimmed + "...") > maxW) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            line2.setLength(0);
            line2.append(trimmed).append("...");
        }

        return new String[]{line1.toString(), line2.toString()};
    }

    private static String cutToWidth(String text, FontMetrics fm, int maxW) {
        if (text == null) return "";
        text = text.trim();
        if (fm.stringWidth(text) <= maxW) return text;
        String s = text;
        while (s.length() > 2 && fm.stringWidth(s + "...") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    private static class AnswerCard extends JButton {
        private boolean hover = false;
        private boolean down  = false;
        private boolean chosen = false;

        private boolean showCorrect = false;
        private boolean showWrong   = false;

        private final String badge;
        private final String text;

        AnswerCard(String badge, String text) {
            this.badge = badge;
            this.text  = text;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(TITLE_TXT);
            setRolloverEnabled(false);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ down = false; repaint(); }
            });
        }

        public void setChosen(boolean v) {
            chosen = v;
            repaint();
        }

        public void markCorrect() {
            showCorrect = true;
            showWrong = false;
            repaint();
        }

        public void markWrong() {
            showWrong = true;
            showCorrect = false;
            repaint();
        }

        public void clearMarks() {
            showCorrect = false;
            showWrong = false;
            chosen = false;
            repaint();
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(250, 92);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 16;

            // background + border based on state
            Color bg = ANS_BG;
            Color border = ANS_BORDER;
            float strokeW = 2f;

            if (down) bg = ANS_BG_DOWN;
            else if (hover) bg = ANS_BG_HOVER;

            if (chosen) {
                bg = new Color(220, 242, 255, 245);
                border = new Color(70, 140, 210, 200);
                strokeW = 3f;
            }

            // result overrides (highest priority)
            if (showCorrect) {
                bg = CORRECT_BG;
                border = CORRECT_BORDER;
                strokeW = 3.2f;
            } else if (showWrong) {
                bg = WRONG_BG;
                border = WRONG_BORDER;
                strokeW = 3.2f;
            }

            // shadow
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRoundRect(6, 8, w - 12, h - 12, arc, arc);

            // card
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

            g2.setColor(border);
            understandingStroke(g2, strokeW);
            g2.drawRoundRect(1, 1, w - 3, h - 3, arc, arc);

            // badge
            int r = 30;
            int bx = 16;
            int by = (h - r) / 2;

            Color badgeBg = BADGE_BG;
            Color badgeTxt = BADGE_TXT;
            Color badgeBorder = new Color(120, 175, 230, 150);

            if (showCorrect) {
                badgeBg = new Color(200, 255, 210, 255);
                badgeTxt = new Color(25, 120, 55);
                badgeBorder = CORRECT_BORDER;
            } else if (showWrong) {
                badgeBg = new Color(255, 210, 210, 255);
                badgeTxt = new Color(150, 35, 35);
                badgeBorder = WRONG_BORDER;
            }

            g2.setColor(badgeBg);
            g2.fillOval(bx, by, r, r);

            g2.setColor(badgeBorder);
            understandingStroke(g2, 2f);
            g2.drawOval(bx, by, r, r);

            g2.setColor(badgeTxt);
            g2.setFont(FONT_BADGE);
            FontMetrics fmB = g2.getFontMetrics();
            int tx = bx + (r - fmB.stringWidth(badge)) / 2;
            int ty = by + (r - fmB.getHeight()) / 2 + fmB.getAscent();
            g2.drawString(badge, tx, ty);

            // text
            g2.setColor(QUESTION_TXT);
            g2.setFont(FONT_ANS);
            FontMetrics fm = g2.getFontMetrics();

            String raw = (text == null) ? "" : text;

            int textX = bx + r + 16;
            int maxW  = w - textX - 18;

            String[] lines = wrapTwoLines(raw, fm, maxW);
            String l1 = lines[0];
            String l2 = lines[1];

            if (l2 == null || l2.isEmpty()) {
                l1 = cutToWidth(l1, fm, maxW);
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(l1, textX, textY);
            } else {
                int lineH = fm.getHeight();
                int totalH = lineH * 2 - 2;
                int startY = (h - totalH) / 2 + fm.getAscent();

                g2.drawString(cutToWidth(l1, fm, maxW), textX, startY);
                g2.drawString(cutToWidth(l2, fm, maxW), textX, startY + lineH - 2);
            }

            g2.dispose();
        }
    }

    private static class TimeBar extends JComponent {
        private final int total;
        private int left;

        TimeBar(int totalSeconds) {
            this.total = Math.max(1, totalSeconds);
            this.left  = totalSeconds;
            setOpaque(false);
        }

        void setSecondsLeft(int s) {
            left = Math.max(0, Math.min(total, s));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int pad = 30;

            g2.setColor(new Color(70, 140, 210, 70));
            g2.fillRoundRect(pad, 3, w - pad * 2, 5, 8, 8);

            double ratio = (double) left / (double) total;
            int fw = (int) ((w - pad * 2) * ratio);

            g2.setColor(new Color(70, 140, 210, 200));
            g2.fillRoundRect(pad, 3, Math.max(0, fw), 5, 8, 8);

            g2.dispose();
        }
    }
    
    private static String levelLabel(model.QuestionLevel lvl) {
        if (lvl == null) return "LEVEL";
        return switch (lvl) {
            case EASY -> "EASY";
            case MEDIUM -> "MEDIUM";
            case HARD -> "HARD";
            case EXPERT -> "EXPERT";
        };
    }
    
    private static class LevelChip extends JComponent {
        private final String text;

        LevelChip(String text) {
            this.text = (text == null) ? "" : text.trim();
            setPreferredSize(new Dimension(92, 34));
            setMinimumSize(new Dimension(92, 34));
            setOpaque(false);
            setToolTipText("Question level");
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyTextHints(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, 190));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.setColor(new Color(120, 175, 230, 150));
            understandingStroke(g2, 1.5f);
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.setFont(FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(TITLE_TXT);
            g2.drawString(text, tx, ty);

            g2.dispose();
        }
    }
}
