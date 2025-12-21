package controller;

import javax.swing.SwingUtilities;

import view.MainMenuGUI;

public class Main {
    public static void main(String[] args) {
        // הרצת האפליקציה בתוך ה-Thread של ה-Swing (חובה ב-GUI)
        SwingUtilities.invokeLater(() -> {
            
            // יצירה והצגה של התפריט הראשי
            new MainMenuGUI(); 
            
        });
    }
}