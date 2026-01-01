package view;

import model.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameOverDialog extends JDialog {

    // Icons (optional)
    private final ImageIcon ICON_TROPHY = loadIcon("/images/trophy.png");
    private final ImageIcon ICON_SKULL  = loadIcon("/images/skull.png");
    private final ImageIcon ICON_HEART  = loadIcon("/images/heart_good.png");
    private final ImageIcon ICON_MINE   = loadIcon("/images/Boom.png");
    private final ImageIcon ICON_CLOCK  = loadIcon("/images/clock.png");

    private static final String BG_DARK  = "/images/background.jpeg";
    private static final String BG_LIGHT = "/images/background_light.jpeg";

    public GameOverDialog(Window owner,
                          boolean success,
                          String titleBig,
                          String subtitle,
                          String players,
                          int score,
                          int livesLeft,
                          int minesRevealed,
                          String difficulty,
                          int durationSeconds,
                          Runnable onPlayAgain,
                          Runnable onMainMenu) {

        super(owner, ModalityType.APPLICATION_MODAL);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(new BorderLayout());
        setResizable(false);

        Dimension targetSize = (owner != null) ? owner.getSize() : new Dimension(1000, 760);
        setSize(targetSize);
        setLocationRelativeTo(owner);

        ThemeManager tm = ThemeManager.getInstance();

        // ===== Background panel (same as game)
        JPanel bg;
        try {
            bg = new BackgroundImagePanel(BG_DARK, BG_LIGHT);
        } catch (Exception ex) {
            // fallback gradient if image fails
            bg = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setPaint(new GradientPaint(0,0, tm.getBackgroundColor1(), 0,getHeight(), tm.getBackgroundColor2()));
                    g2.fillRect(0,0,getWidth(),getHeight());
                    g2.dispose();
                }
            };
        }
        bg.setLayout(new BorderLayout());

        // ===== Dim overlay above background
        JPanel overlay = new DimOverlayPanel(tm);
        overlay.setLayout(new GridBagLayout());
        overlay.setOpaque(false);

        // ===== Main content (NO CARD)
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        String big = (titleBig == null || titleBig.isBlank())
                ? (success ? "VICTORY" : "GAME OVER")
                : titleBig.trim();

        String sub = (subtitle == null) ? "" : subtitle.trim();
        String ppl = (players == null) ? "" : players.trim();

        // Top-right close (no box)
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JButton close = new XButton();
        close.addActionListener(e -> dispose());
        topRow.add(close, BorderLayout.EAST);
        topRow.setMaximumSize(new Dimension(1000, 46));

        // Icon
        JLabel icon = new JLabel();
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setOpaque(false);

        icon.setPreferredSize(new Dimension(140, 110));
        icon.setMaximumSize(new Dimension(140, 110));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon mainIcon = success ? ICON_TROPHY : ICON_SKULL;
        if (mainIcon != null) {
            icon.setIcon(scale(mainIcon, 92, 92));
        } else {
            icon.setText(success ? "🏆" : "💀");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 78));
            icon.setForeground(new Color(255,255,255,230));
        }

        // Big title (with shadow)
        ShadowLabel title = new ShadowLabel(big);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 56));
        title.setForeground(Color.WHITE);

        // Subtitle + players
        ShadowLabel subLbl = new ShadowLabel(sub);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subLbl.setForeground(new Color(225,235,250));

        ShadowLabel pplLbl = new ShadowLabel(ppl);
        pplLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        pplLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pplLbl.setForeground(new Color(190,205,225));

        // Stats line (NO rectangles)
        String timeTxt = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60);

        JPanel statsLine = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        statsLine.setOpaque(false);
        statsLine.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsLine.add(stat("Score", String.valueOf(score), null));
        statsLine.add(stat("Lives", String.valueOf(livesLeft), ICON_HEART));
        statsLine.add(stat("Mines", String.valueOf(minesRevealed), ICON_MINE));
        statsLine.add(stat("Time", timeTxt, ICON_CLOCK));

        // Difficulty (small text only)
        ShadowLabel diffLbl = new ShadowLabel("Difficulty: " + (difficulty == null ? "" : difficulty));
        diffLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        diffLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        diffLbl.setForeground(new Color(200,215,235));

        // Buttons (still nice, but no card behind)
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playAgain = new FlatGlassButton(success ? "Play Again" : "Try Again", tm.getBoardAColor());
        JButton menu      = new FlatGlassButton("Main Menu", tm.getBoardBColor());

        playAgain.addActionListener(e -> { dispose(); if (onPlayAgain != null) onPlayAgain.run(); });
        menu.addActionListener(e -> { dispose(); if (onMainMenu != null) onMainMenu.run(); });

        btnRow.add(playAgain);
        btnRow.add(menu);

        // Build content
        content.add(topRow);
        content.add(Box.createVerticalStrut(30)); 
        content.add(icon);
        content.add(Box.createVerticalStrut(6));  
        content.add(title);
        content.add(Box.createVerticalStrut(10));
        if (!sub.isEmpty()) content.add(subLbl);
        if (!ppl.isEmpty()) {
            content.add(Box.createVerticalStrut(6));
            content.add(pplLbl);
        }
        content.add(Box.createVerticalStrut(26));
        content.add(statsLine);
        content.add(Box.createVerticalStrut(14));
        content.add(diffLbl);
        content.add(Box.createVerticalStrut(26));
        content.add(btnRow);
        content.add(Box.createVerticalStrut(30));

        // Center in overlay
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        FrameOnlyPanel frame = new FrameOnlyPanel(tm);
        frame.add(content, new GridBagConstraints());

        overlay.add(frame, gbc);

        bg.add(overlay, BorderLayout.CENTER);
        add(bg, BorderLayout.CENTER);

        // ESC close
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Click anywhere outside buttons closes (optional)
        overlay.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                // If you DON'T want this behavior, delete this listener
                dispose();
            }
        });
    }

    // =========================
    // Helpers
    // =========================

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    private ImageIcon scale(ImageIcon src, int w, int h) {
        if (src == null || src.getImage() == null) return src;
        Image img = src.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private JComponent stat(String label, String value, ImageIcon icon) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        if (icon != null) {
            JLabel ic = new JLabel(scale(icon, 18, 18));
            ic.setOpaque(false);
            p.add(ic);
            p.add(Box.createHorizontalStrut(6));
        }

        ShadowLabel txt = new ShadowLabel(label + ": ");
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(new Color(200,215,235));

        ShadowLabel val = new ShadowLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 18));
        val.setForeground(Color.WHITE);

        p.add(txt);
        p.add(val);
        return p;
    }

    // =========================
    // Overlay dim panel
    // =========================
    private static class DimOverlayPanel extends JPanel {
        private final ThemeManager tm;

        DimOverlayPanel(ThemeManager tm) {
            this.tm = tm;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int a = tm.isDarkMode() ? 155 : 120;
            g2.setColor(new Color(0, 0, 0, a));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // soft vignette
            g2.setPaint(new RadialGradientPaint(
                    new Point(getWidth() / 2, getHeight() / 2),
                    Math.max(getWidth(), getHeight()) / 2f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0,0,0,0), new Color(0,0,0, tm.isDarkMode()? 120 : 90)}
            ));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();
        }
    }

    // =========================
    // Shadow label (better readability on background)
    // =========================
    private static class ShadowLabel extends JLabel {
        ShadowLabel(String text) {
            super(text, SwingConstants.CENTER);
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String t = getText();
            if (t == null) t = "";

            FontMetrics fm = g2.getFontMetrics(getFont());
            int x = (getWidth() - fm.stringWidth(t)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            // shadow
            g2.setColor(new Color(0, 0, 0, 170));
            g2.drawString(t, x + 2, y + 2);

            // main
            g2.setFont(getFont());
            g2.setColor(getForeground());
            g2.drawString(t, x, y);

            g2.dispose();
        }
    }

    // =========================
    // Close button (simple X, no box)
    // =========================
    private static class XButton extends JButton {
        private boolean hover = false;

        XButton() {
            setPreferredSize(new Dimension(44, 44));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255,255,255, hover ? 255 : 210));

            int pad = 16;
            g2.drawLine(pad, pad, w - pad, h - pad);
            g2.drawLine(w - pad, pad, pad, h - pad);

            g2.dispose();
        }
    }

    // =========================
    // Flat glass button (no big rectangle panel behind)
    // =========================
    private static class FlatGlassButton extends JButton {
        private final Color tint;
        private boolean hover = false;
        private boolean down = false;

        FlatGlassButton(String text, Color tint) {
            super(text);
            this.tint = tint;

            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(200, 46));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { down = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int r = 18;
            int lift = down ? 2 : 0;

            // subtle glass background
            int baseA = hover ? 22 : 16;
            g2.setColor(new Color(255,255,255, baseA));
            g2.fillRoundRect(0, lift, w, h - 2, r, r);

            // thin tinted outline
            g2.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), hover ? 170 : 140));
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(1, lift + 1, w - 3, h - 4, r, r);

            // text
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent() + lift;
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }
    
    private static class FrameOnlyPanel extends JPanel {
        private final ThemeManager tm;

        FrameOnlyPanel(ThemeManager tm) {
            this.tm = tm;
            setOpaque(false);
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28)); // מרווח פנימי
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 26;

            // מסגרת עדינה לבנה
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(new Color(255, 255, 255, tm.isDarkMode() ? 70 : 90));
            g2.drawRoundRect(1, 1, w - 3, h - 3, r, r);

            // glow ממש עדין מסביב
            g2.setStroke(new BasicStroke(8f));
            g2.setColor(new Color(120, 180, 255, tm.isDarkMode() ? 18 : 12));
            g2.drawRoundRect(2, 2, w - 5, h - 5, r, r);

            g2.dispose();
        }
    }
}
