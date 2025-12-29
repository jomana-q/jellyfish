package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom cell button
 * - Rounded corners
 * - Subtle depth (highlight/shadow)
 * - Hover + Press effects
 * - No default LAF artifacts
 */
public class CellButton extends JButton {

    private boolean hovered = false;
    private boolean pressed = false;

    // Visual state
    private Color fill = new Color(110, 160, 235);
    private Color fillHover = null;
    private Color textColor = Color.WHITE;

    private int arc = 10;

    public CellButton() {
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setHorizontalAlignment(SwingConstants.CENTER);

        // Mouse interactions (hover/press)
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; pressed = false; repaint(); }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { pressed = true; repaint(); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e){ pressed = false; repaint(); }
        });
    }

    public void setArc(int arc) {
        this.arc = arc;
        repaint();
    }

    public void setFill(Color fill) {
        this.fill = fill;
        this.fillHover = null;
        repaint();
    }

    public void setFill(Color fill, Color hoverFill) {
        this.fill = fill;
        this.fillHover = hoverFill;
        repaint();
    }

    public void setTextColor(Color c) {
        this.textColor = c;
        setForeground(c);
        repaint();
    }

    /**
     * ✅ IMPORTANT:
     * Set icon AND disabledIcon to the same image so Swing won't "wash out" the icon when disabled.
     */
    public void setScaledIcon(ImageIcon src) {
        if (src == null) {
            setIcon(null);
            setDisabledIcon(null);
            return;
        }

        // scale to button size (fallback if size not ready yet)
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 10;
        if (size <= 0) size = 22;

        Image img = src.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        ImageIcon scaled = new ImageIcon(img);

        setText("");                 // no text when using icon
        setIcon(scaled);
        setDisabledIcon(scaled);     // ✅ prevents whitening
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Press effect (slight down shift)
        int dx = pressed ? 1 : 0;
        int dy = pressed ? 1 : 0;

        // Shape
        RoundRectangle2D shape = new RoundRectangle2D.Double(1 + dx, 1 + dy, w - 2, h - 2, arc, arc);

        // Base fill (with hover variation)
        Color base = fill;
        if (isEnabled() && hovered) {
            base = (fillHover != null) ? fillHover : brighten(fill, 0.12);
        }
        if (!isEnabled()) {
            base = darken(fill, 0.45);
        }

        // Subtle gradient for depth
        GradientPaint gp = new GradientPaint(
                0, 0, brighten(base, 0.12),
                0, h, darken(base, 0.08)
        );
        g2.setPaint(gp);
        g2.fill(shape);

        // Inner highlight (top-left)
        g2.setColor(new Color(255, 255, 255, isEnabled() ? 55 : 25));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(3 + dx, 3 + dy, w - 6, h - 6, arc - 2, arc - 2));

        // Outer border
        g2.setColor(new Color(0, 0, 0, 90));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(shape);

        g2.dispose();

        // Draw text AFTER background
        setForeground(textColor);
        super.paintComponent(g);
    }

    // Helpers
    private Color brighten(Color c, double amount) {
        int r = clamp((int) (c.getRed()   + 255 * amount));
        int g = clamp((int) (c.getGreen() + 255 * amount));
        int b = clamp((int) (c.getBlue()  + 255 * amount));
        return new Color(r, g, b, c.getAlpha());
    }

    private Color darken(Color c, double factor) {
        int r = clamp((int) (c.getRed() * (1.0 - factor)));
        int g = clamp((int) (c.getGreen() * (1.0 - factor)));
        int b = clamp((int) (c.getBlue() * (1.0 - factor)));
        return new Color(r, g, b, c.getAlpha());
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
