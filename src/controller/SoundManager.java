package controller;

import javax.sound.sampled.*;
import java.io.File;

/**
 * מנהל הסאונד (Sound Manager) - אחראי על ניגון מוזיקת רקע ושליטה בווליום.
 * משתמש בתבנית Singleton כדי להבטיח שיש רק מנגנון סאונד אחד במשחק.
 */
public class SoundManager {

    private static SoundManager instance;
    private Clip backgroundClip;
    private FloatControl volumeControl;
    private boolean isMuted = false;
    private float currentVolume = 0.5f; // עוצמת ברירת מחדל (50%)

    // קבלת המופע היחיד של המחלקה (Singleton)
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * ניגון מוזיקת רקע מקובץ WAV.
     * @param filePath הנתיב לקובץ המוזיקה.
     */
    public void playBackgroundMusic(String filePath) {
        try {
            File musicFile = new File(filePath);
            if (!musicFile.exists()) {
                System.err.println("Music file not found: " + filePath);
                return;
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioInput);

            // קבלת שליטה על הווליום (Master Gain)
            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                // הגדרת הווליום ההתחלתי
                setVolume((int)(currentVolume * 100));
            }

            // ניגון בלופ אינסופי
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * עצירת המוזיקה.
     */
    public void stopMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
        }
    }

    /**
     * שינוי עוצמת השמע (0 עד 100).
     */
    public void setVolume(int volume) {
        if (volumeControl == null) return;
        
        currentVolume = volume / 100.0f;

        if (isMuted) return; // אם מושתק, לא משנים את הווליום בפועל

        float min = volumeControl.getMinimum();
        float max = volumeControl.getMaximum();
        
        // המרת האחוזים (0-100) לדציבלים
        float range = max - min;
        float gain = (range * currentVolume) + min;
        
        // הגנה מפני חריגות
        if (gain > max) gain = max;
        if (gain < min) gain = min;
        
        volumeControl.setValue(gain);
    }

    /**
     * השתקה או ביטול השתקה.
     */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
        if (volumeControl == null) return;

        if (muted) {
            volumeControl.setValue(volumeControl.getMinimum()); // השתקה מלאה
        } else {
            // שחזור הווליום האחרון שנבחר
            setVolume((int)(currentVolume * 100));
        }
    }
}