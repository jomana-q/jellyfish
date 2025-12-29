package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Game Over / Victory dialog (full-window overlay + centered card).
 * Put this class in view/ package (MVC friendly).
 */
public class GameOverDialog extends JDialog {

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

        // Make the dialog cover the whole owner window (real overlay feel)
        if (owner != null) {
            setSize(owner.getSize());
            setLocationRelativeTo(owner);
        } else {
            setSize(900, 700);
            setLocationRelativeTo(null);
        }

        // Accent by result
        Color accent = success ? new Color(82, 201, 130) : new Color(235, 86, 86);
        String big = (titleBig == null || titleBig.isBlank()) ? (success ? "VICTORY" : "GAME OVER") : titleBig;
        String sub = (subtitle == null) ? "" : subtitle;
        String ppl = (players == null) ? "" : players;

        // === Overlay background
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // dim background
                g2.setColor(new Color(0, 0, 0, 175));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // subtle vignette
                g2.setPaint(new RadialGradientPaint(
                        new Point(getWidth() / 2, getHeight() / 2),
                        Math.max(getWidth(), getHeight()) / 2f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 120)}
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        // === Card
        ResultCard card = new ResultCard(accent, success);
        card.setPreferredSize(new Dimension(720, 420));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // Top row: title + close
        JButton close = new CloseIconButton();
        close.setToolTipText("Close");
        close.addActionListener(e -> dispose());

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(close, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // Center content
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(big, SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 44));
        title.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel(sub, SwingConstants.CENTER);
        subtitleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLbl.setForeground(new Color(200, 215, 235));

        JLabel playersLbl = new JLabel(ppl, SwingConstants.CENTER);
        playersLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        playersLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        playersLbl.setForeground(new Color(165, 185, 210));

        center.add(Box.createVerticalStrut(10));
        center.add(title);
        center.add(Box.createVerticalStrut(6));
        center.add(subtitleLbl);
        center.add(Box.createVerticalStrut(4));
        center.add(playersLbl);
        center.add(Box.createVerticalStrut(18));

        // Stats row (clean 4 chips)
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);

        stats.add(new StatChip("Score", String.valueOf(score), accent));
        stats.add(new StatChip("Lives", String.valueOf(livesLeft), accent));
        stats.add(new StatChip("Mines", String.valueOf(minesRevealed), accent));
        stats.add(new StatChip("Time", durationSeconds + "s", accent));

        center.add(stats);
        center.add(Box.createVerticalStrut(14));

        // Difficulty pill
        JLabel diff = new JLabel("Difficulty: " + difficulty, SwingConstants.CENTER);
        diff.setAlignmentX(Component.CENTER_ALIGNMENT);
        diff.setFont(new Font("Segoe UI", Font.BOLD, 13));
        diff.setForeground(new Color(225, 235, 250));
        diff.setOpaque(true);
        diff.setBackground(new Color(255, 255, 255, 18));
        diff.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 45), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        center.add(diff);
        center.add(Box.createVerticalGlue());

        card.add(center, BorderLayout.CENTER);

        // Bottom buttons
        JButton playAgain = new ModernButton(success ? "Play Again" : "Try Again", accent, true);
        JButton menu = new ModernButton("Main Menu", new Color(120, 170, 240), false);

        playAgain.addActionListener(e -> {
            dispose();
            if (onPlayAgain != null) onPlayAgain.run();
        });
        menu.addActionListener(e -> {
            dispose();
            if (onMainMenu != null) onMainMenu.run();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnRow.setOpaque(false);
        btnRow.add(playAgain);
        btnRow.add(menu);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(Box.createVerticalStrut(10));
        bottom.add(btnRow);
        bottom.add(Box.createVerticalStrut(6));

        card.add(bottom, BorderLayout.SOUTH);

        // Center the card inside overlay
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 20, 0, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        overlay.add(card, gbc);

        add(overlay, BorderLayout.CENTER);

        // Close on ESC
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Optional: click outside the card to close
        overlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(overlay, e.getPoint(), card);
                if (p.x < 0 || p.y < 0 || p.x > card.getWidth() || p.y > card.getHeight()) {
                    dispose();
                }
            }
        });
    }

    // =========================
    // UI Components
    // =========================

    private static class ResultCard extends JPanel {
        private final Color accent;
        private final boolean success;

        ResultCard(Color accent, boolean success) {
            this.accent = accent;
            this.success = success;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 26;

            // shadow
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillRoundRect(10, 12, w - 20, h - 20, r, r);

            // card fill (slight vertical gradient)
            Color top = new Color(18, 28, 45);
            Color bottom = new Color(14, 22, 36);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRoundRect(0, 0, w - 10, h - 10, r, r);

            // border
            g2.setColor(new Color(255, 255, 255, 55));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(0, 0, w - 11, h - 11, r, r);

            // accent bar (top)
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(28, 20, w - 40, 20);

            // watermark icon in background
            g2.setComposite(AlphaComposite.SrcOver.derive(0.08f));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 130));
            String wm = success ? "🏆" : "💀";
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(wm)) / 2 - 10;
            int ty = 140;
            g2.drawString(wm, tx, ty);

            g2.dispose();
        }
    }

    private static class StatChip extends JPanel {
        private final Color accent;

        StatChip(String label, String value, Color accent) {
            this.accent = accent;
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(150, 86));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            JLabel l = new JLabel(label);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(170, 190, 215));

            JLabel v = new JLabel(value);
            v.setFont(new Font("Segoe UI", Font.BOLD, 22));
            v.setForeground(Color.WHITE);

            add(l, BorderLayout.NORTH);
            add(v, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // chip bg
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRoundRect(0, 0, w, h, 18, 18);

            // border
            g2.setColor(new Color(255, 255, 255, 30));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

            // small accent underline
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(12, h - 12, w - 12, h - 12);

            g2.dispose();

            super.paintComponent(g);
        }
    }

    private static class ModernButton extends JButton {
        private final Color base;
        private boolean hover = false;
        private boolean primary;

        ModernButton(String text, Color base, boolean primary) {
            super(text);
            this.base = base;
            this.primary = primary;

            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(180, 44));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int r = 18;

            Color fill = base;
            if (hover) {
                fill = new Color(
                        Math.min(255, base.getRed() + 18),
                        Math.min(255, base.getGreen() + 18),
                        Math.min(255, base.getBlue() + 18)
                );
            }

            // shadow
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillRoundRect(3, 4, w - 6, h - 6, r, r);

            // button fill
            if (!primary) {
                // secondary: darker glass style
                g2.setColor(new Color(255, 255, 255, hover ? 14 : 10));
                g2.fillRoundRect(0, 0, w - 6, h - 6, r, r);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.drawRoundRect(0, 0, w - 7, h - 7, r, r);
            } else {
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w - 6, h - 6, r, r);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.drawRoundRect(0, 0, w - 7, h - 7, r, r);
            }

            // text
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(getText())) / 2 - 3;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent() - 2;
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    private static class CloseIconButton extends JButton {
        private boolean hover = false;

        CloseIconButton() {
            setPreferredSize(new Dimension(36, 36));
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

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(255, 255, 255, hover ? 22 : 16));
            g2.fillOval(4, 4, w - 8, h - 8);

            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 255, hover ? 240 : 210));

            int pad = 12;
            g2.drawLine(pad, pad, w - pad, h - pad);
            g2.drawLine(w - pad, pad, pad, h - pad);

            g2.dispose();
        }
    }
}
