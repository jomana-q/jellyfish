package view;

import javax.swing.*;
import java.awt.*;

public class LegendIconChip extends JComponent {
    private final Color bg;
    private final Image imgScaled;
    private final String tag; 

    public LegendIconChip(ImageIcon icon, Color bg) {
        this(icon, bg, null);
    }

    public LegendIconChip(ImageIcon icon, Color bg, String tag) {
        this.bg = bg;
        this.tag = tag;

        setPreferredSize(new Dimension(46, 34));
        setOpaque(false);

        if (icon != null) {
            int size = 24; 
            this.imgScaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } else {
            this.imgScaled = null;
        }
        setToolTipText(" ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // background chip
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, 12, 12);

        g2.setColor(new Color(255, 255, 255, 90));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // draw cached icon
        if (imgScaled != null) {
            int size = 24;
            int x = (w - size) / 2;
            int y = (h - size) / 2;
            g2.drawImage(imgScaled, x, y, this);
        }

        // tag on icon (small label)
        if (tag != null && !tag.isBlank()) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();

            int pad = 6;
            int tw = fm.stringWidth(tag);
            int th = fm.getAscent();

            int tx = (w - tw) / 2;
            int ty = h - 6; // bottom

            // shadow
            g2.setColor(new Color(0, 0, 0, 140));
            g2.drawString(tag, tx + 1, ty + 1);

            // text
            g2.setColor(Color.WHITE);
            g2.drawString(tag, tx, ty);
        }

        g2.dispose();
    }
}
