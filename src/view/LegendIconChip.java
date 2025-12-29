package view;

import javax.swing.*;
import java.awt.*;

public class LegendIconChip extends JComponent {
    private final Color bg;
    private final Image imgScaled;

    public LegendIconChip(ImageIcon icon, Color bg) {
        this.bg = bg;
        setPreferredSize(new Dimension(46, 34));
        setOpaque(false);

        if (icon != null) {
            int size = 24; // fits 46x34 nicely
            this.imgScaled = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } else {
            this.imgScaled = null;
        }
        setToolTipText(" "); // just to enable tooltip area
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

        g2.dispose();
    }
}
