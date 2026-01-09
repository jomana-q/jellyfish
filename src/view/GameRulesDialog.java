package view;

import model.Difficulty;
import model.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * חלון חוקים מעודצב (Lilac/Pink Theme)
 */
public class GameRulesDialog extends JDialog {

    public GameRulesDialog(Window owner, Difficulty diff) {
        super(owner, "Level Rules", ModalityType.APPLICATION_MODAL);
        
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent for rounded corners
        setSize(450, 560); // קצת יותר רחב וגבוה
        setLocationRelativeTo(owner);

        // --- Main Panel with Blur/Glass Effect ---
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isDark = ThemeManager.getInstance().isDarkMode();
                
                // 1. רקע כהה שקוף (יותר אלגנטי)
                Color bg = isDark ? new Color(30, 32, 40, 240) : new Color(245, 245, 255, 240);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                // 2. מסגרת לילך/וורוד עדינה (Lilac Border)
                Color border = isDark ? new Color(180, 120, 255, 100) : new Color(140, 80, 200, 80);
                g2.setColor(border);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);

                g2.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel title = new JLabel("Level Info: " + diff.name());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        // צבע כותרת ראשית: לבן או כחול כהה
        title.setForeground(ThemeManager.getInstance().isDarkMode() ? Color.WHITE : new Color(40, 40, 90));
        
        // כפתור סגירה (X) מצויר ידנית
        JButton closeBtn = new CloseButton();
        closeBtn.addActionListener(e -> dispose());

        header.add(title, BorderLayout.CENTER);
        header.add(closeBtn, BorderLayout.EAST);

        // --- Info List ---
        JPanel infoContainer = new JPanel();
        infoContainer.setOpaque(false);
        infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));

        // צבע הכותרות: לילך בהיר בכהה, סגול עמוק בבהיר
        Color headerColor = ThemeManager.getInstance().isDarkMode() 
                ? new Color(220, 160, 255)  // Light Lilac
                : new Color(120, 60, 180);  // Deep Purple

        infoContainer.add(Box.createVerticalStrut(20));
        
        addSectionTitle(infoContainer, "📊 Board Statistics", headerColor);
        addInfoRow(infoContainer, "Mines (Bombs)", String.valueOf(diff.getMines()));
        addInfoRow(infoContainer, "Questions", String.valueOf(diff.getQuestionCount()));
        addInfoRow(infoContainer, "Surprises", String.valueOf(diff.getSurpriseCount()));

        infoContainer.add(Box.createVerticalStrut(15));
        
        addSectionTitle(infoContainer, "💰 Economy Costs", headerColor);
        addInfoRow(infoContainer, "Activation Cost", "-" + diff.getPowerCost() + " pts");
        addInfoRow(infoContainer, "Surprise Value", "+/- " + diff.getSurprisePoints() + " pts");
        
        infoContainer.add(Box.createVerticalStrut(15));
        
        addSectionTitle(infoContainer, "🚩 Flagging Rules", headerColor);
        addInfoRow(infoContainer, "Flag a Mine", "+1 pt & Reveal");
        addInfoRow(infoContainer, "Flag Safe Cell", "-3 pts (Penalty)");
        
        infoContainer.add(Box.createVerticalStrut(15));

        // הוספתי אייקון לב כאן
        addSectionTitle(infoContainer, "❤️ Lives & Score", headerColor);
        addInfoRow(infoContainer, "Max Lives", "10 (Extra = Points)");
        addInfoRow(infoContainer, "Game Over", "Shared Lives = 0");

        infoContainer.add(Box.createVerticalGlue());

        // --- Bottom Button ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10,0,0,0));
        
        JButton okBtn = new GlassButton("Got it!");
        okBtn.addActionListener(e -> dispose());
        bottomPanel.add(okBtn);

        contentPanel.add(header, BorderLayout.NORTH);
        contentPanel.add(infoContainer, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel);
    }

    // --- Helpers ---

    private void addSectionTitle(JPanel p, String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 17)); // Emoji font support
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(6));
        
        // קו הפרדה עדין מתחת לכותרת
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(1000, 1));
        sep.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sep);
        p.add(Box.createVerticalStrut(6));
    }

    private void addInfoRow(JPanel p, String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(1000, 22));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel k = new JLabel("• " + key);
        k.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        k.setForeground(ThemeManager.getInstance().getTextColor());
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 14));
        v.setForeground(ThemeManager.getInstance().getTextColor());

        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        
        p.add(row);
    }

    // --- Custom Components ---

    // כפתור סגירה מעוצב (במקום ריבוע)
    private static class CloseButton extends JButton {
        private boolean hover = false;
        
        CloseButton() {
            setPreferredSize(new Dimension(30, 30));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (hover) {
                g2.setColor(new Color(255, 100, 100, 100));
                g2.fillOval(0, 0, getWidth(), getHeight());
            }
            
            g2.setColor(ThemeManager.getInstance().getTextColor());
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = 8;
            g2.drawLine(pad, pad, getWidth()-pad, getHeight()-pad);
            g2.drawLine(getWidth()-pad, pad, pad, getHeight()-pad);
            
            g2.dispose();
        }
    }

    // כפתור זכוכית (Glassy) כמו בתפריט הראשי
    private static class GlassButton extends JButton {
        private boolean hover = false;
        
        GlassButton(String text) {
            super(text);
            setPreferredSize(new Dimension(120, 40));
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // צבע רקע: לילך/כחול
            Color base = hover ? new Color(120, 80, 220) : new Color(100, 60, 200);
            g2.setColor(base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            
            // ברק זכוכיתי למעלה
            g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,100), 0, getHeight()/2, new Color(255,255,255,0)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            
            // מסגרת זוהרת
            if (hover) {
                g2.setColor(new Color(200, 200, 255));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 30, 30);
            }
            
            // טקסט
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }
}