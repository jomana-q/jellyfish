package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class BackgroundImagePanel extends JPanel {

    private BufferedImage image;

    public BackgroundImagePanel(String resourcePath) {
        setOpaque(true);

        try {
            // resourcePath مثال: "/images/background.jpeg"
            image = ImageIO.read(getClass().getResource(resourcePath));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load background image: " + resourcePath);
            e.printStackTrace();
            image = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();

        int imgW = image.getWidth();
        int imgH = image.getHeight();

        // Cover scaling (مثل CSS background-size: cover)
        double scale = Math.max(panelW / (double) imgW, panelH / (double) imgH);
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);

        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;

        g2.drawImage(image, x, y, drawW, drawH, null);

        // Optional: dark overlay خفيف عشان النص يبين
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRect(0, 0, panelW, panelH);

        g2.dispose();
    }
}
