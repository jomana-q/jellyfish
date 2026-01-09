package model;

import java.awt.Color;

public class ThemeManager {

    private static ThemeManager instance;
    private boolean isDarkMode = true;

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    public void setDarkMode(boolean dark) { this.isDarkMode = dark; }
    public boolean isDarkMode() { return isDarkMode; }

    //Background gradient
    public Color getBackgroundColor1() {
        return isDarkMode ? new Color(10, 25, 40) : new Color(240, 248, 255);
    }

    public Color getBackgroundColor2() {
        return isDarkMode ? new Color(25, 50, 60) : new Color(200, 220, 255);
    }

    public Color getTextColor() {
        return isDarkMode ? Color.WHITE : new Color(30, 30, 30);
    }
    /** Special color for Titles (Headers) */
    public Color getTitleColor() {
        // Dark Mode: White
        // Light Mode: Vibrant Blue 
        return isDarkMode ? Color.WHITE : new Color(60, 100, 200);
    }

    // Boards (closed cells colors)

    /** Board A - blue */
    public Color getBoardAColor() {
        return isDarkMode ? new Color(92, 135, 210) : new Color(90, 140, 230);
    }

    /** Board B - PURPLE (right board) */
    public Color getBoardBColor() {
        return isDarkMode ? new Color(130, 95, 200) : new Color(145, 110, 215);
    }

    /** Border around boards */
    public Color getBoardBorderColor() {
        return isDarkMode ? new Color(255, 255, 255, 70) : new Color(20, 40, 70, 80);
    }

    // Revealed "glass" overlays (transparent)

    /** Default revealed glass (empty cells) */
    public Color getRevealedGlass() {
        return isDarkMode ? new Color(255, 255, 255, 55) : new Color(30, 60, 100, 40);
    }

    /** Numbers glass (a bit stronger for readability) */
    public Color getNumberGlass() {
        return isDarkMode ? new Color(255, 255, 255, 75) : new Color(30, 60, 100, 50);
    }

    /** Used-special glass (a bit weaker) */
    public Color getUsedGlass() {
        return isDarkMode ? new Color(255, 255, 255, 40) : new Color(30, 60, 100, 30);
    }

    // Special tiles colors (tinted glass)

    /** Question tile - BLUE tint */
    public Color getQuestionGlass() {
        return isDarkMode ? new Color(80, 170, 255, 70) : new Color(60, 140, 230, 55);
    }

    /** Surprise tile - LILAC tint */
    public Color getSurpriseGlass() {
        return isDarkMode ? new Color(190, 140, 255, 70) : new Color(175, 120, 240, 55);
    }

    /** Mine tile - RED (opaque / strong) */
    public Color getMineFill1() {
        return new Color(200, 35, 45);
    }

    public Color getMineFill2() {
        return new Color(235, 70, 75);
    }

    // helpers
    public Color darker(Color c, double factor) {
        return new Color(
                clamp((int) (c.getRed() * factor)),
                clamp((int) (c.getGreen() * factor)),
                clamp((int) (c.getBlue() * factor)),
                c.getAlpha()
        );
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
