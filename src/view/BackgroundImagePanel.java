package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import model.ThemeManager; // ייבוא מנהל העיצוב כדי לבדוק אם המצב הוא Dark או Light

public class BackgroundImagePanel extends JPanel {

	private BufferedImage imageDark;  // תמונת הרקע למצב כהה
    private BufferedImage imageLight; // תמונת הרקע למצב בהיר

    public BackgroundImagePanel(String darkPath, String lightPath) {
        setOpaque(true);
        try {
            // טעינת שתי התמונות מהמשאבים של הפרויקט
            imageDark = ImageIO.read(getClass().getResource(darkPath));
            imageLight = ImageIO.read(getClass().getResource(lightPath));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Failed to load background images");
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // בחירת התמונה הנכונה לפי מצב התצוגה הנוכחי
        BufferedImage currentImage = ThemeManager.getInstance().isDarkMode() ? imageDark : imageLight;

        if (currentImage == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();

        int imgW = currentImage.getWidth();
        int imgH = currentImage.getHeight();

        // Cover scaling (مثل CSS background-size: cover)
        double scale = Math.max(panelW / (double) imgW, panelH / (double) imgH);
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);

        int x = (panelW - drawW) / 2;
        int y = (panelH - drawH) / 2;

        g2.drawImage(currentImage, x, y, drawW, drawH, null);
        
     // בדיקה האם המשתמש בחר במצב כהה או בהיר
        boolean isDark = ThemeManager.getInstance().isDarkMode(); 

        if (isDark) {
            // שכבת כיסוי כהה עבור מצב לילה כדי שהטקסט הלבן יבלוט
            g2.setColor(new Color(0, 0, 0, 80)); 
        } else {
            // שכבת כיסוי בהירה עבור מצב יום כדי לשמור על מראה נקי ובהיר
            g2.setColor(new Color(255, 255, 255, 40)); 
        }

        // ציור שכבת הכיסוי על כל הפאנל
        g2.fillRect(0, 0, panelW, panelH);

        g2.dispose();
    }
}
