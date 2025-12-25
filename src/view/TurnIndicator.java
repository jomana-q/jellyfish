package view;

import javax.swing.*;
import java.awt.*;

public class TurnIndicator extends JComponent {

    private boolean active = false;

    public TurnIndicator() {
        setPreferredSize(new Dimension(14, 14));
        setOpaque(false);
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = active
                ? new Color(80, 220, 120)   // ירוק – תור פעיל
                : new Color(120, 120, 120); // אפור – לא פעיל

        g2.setColor(fill);
        g2.fillOval(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);

        g2.dispose();
    }
}
