package controller;

import javax.sound.sampled.*;
import java.net.URL;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * מנהל הסאונד (SoundManager)
 * - מוזיקת רקע מהמשאבים (תפריט / משחק / שאלות)
 * - אפקטים (תשובה נכונה/שגויה, הפתעות)
 * - מוזיקה מותאמת אישית מקובץ WAV שבוחר המשתמש
 * - שליטה בווליום ומיוט
 */
public class SoundManager {

    private static SoundManager instance;
    
  
    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    // ====== קבצי סאונד מתוך התיקייה src/sounds (ב-classpath) ======
    private static final String SND_MENU      = "/sounds/GameMenuSound.wav";
    private static final String SND_GAME      = "/sounds/GameDurationSound.wav";
    private static final String SND_QUESTION  = "/sounds/QuestionDurationSound.wav";
    private static final String SND_CORRECT   = "/sounds/CorrectAnswerSound.wav";
    private static final String SND_WRONG     = "/sounds/WrongAnswerSound.wav";
    private static final String SND_GOOD_SUR  = "/sounds/GoodSurpriseSound.wav";
    private static final String SND_BAD_SUR   = "/sounds/BadSurpriseSound.wav";

    // ניהול טיימר עבור SFX ארוך (5 שניות)
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    // קליפ למוזיקת רקע + קליפ לאפקטים
    private Clip bgmClip;
    private Clip currentSfxClip;

    // למעקב אחר מה מנגן כרגע
    private String currentBgmPath = null; // יכול להיות path של resource או קובץ

    // ווליום ומיוט
    private boolean muted = false;
    private int volume = 60; // 0..100


    private SoundManager() {}

    // ====== מוזיקת רקע מהמשחק (קבצי משאבים) ======

    public void playMenuLoop()     { playBgmLoopFromResource(SND_MENU); }
    public void playGameLoop()     { playBgmLoopFromResource(SND_GAME); }
    public void playQuestionLoop() { playBgmLoopFromResource(SND_QUESTION); }

    /**
     * עצירת כל מוזיקת הרקע (גם של הקבצים החיצוניים)
     */
    public void stopBgm() {
        stopClip(bgmClip);
        bgmClip = null;
        currentBgmPath = null;
    }

    /** מעטפת נוחה – זה מה שה-SettingsPanel קורא */
    public void stopMusic() {
        stopBgm();
    }

    /**
     * הפעלת מוזיקת רקע מתוך המשאבים (classpath).
     */
    private void playBgmLoopFromResource(String resourcePath) {
        if (muted) return;

        // אם כבר מנגן אותו דבר – לא צריך להפעיל שוב
        if (resourcePath != null && resourcePath.equals(currentBgmPath) &&
                bgmClip != null && bgmClip.isOpen()) {
            return;
        }

        stopClip(bgmClip);

        bgmClip = loadClipFromResource(resourcePath);
        currentBgmPath = resourcePath;

        if (bgmClip == null) return;

        applyVolume(bgmClip);
        bgmClip.setFramePosition(0);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgmClip.start();
    }

    // ====== מוזיקת רקע מקובץ WAV שהמשתמש בחר (Settings) ======

    /**
     * ניגון מוזיקת רקע מקובץ WAV חיצוני (נתיב מלא).
     */
    public void playBackgroundMusic(String filePath) {
        if (muted) return;
        if (filePath == null || filePath.isEmpty()) return;

        // אם כבר מנגן את אותו קובץ – לא לפתוח שוב
        if (filePath.equals(currentBgmPath) &&
                bgmClip != null && bgmClip.isOpen()) {
            return;
        }

        stopClip(bgmClip);

        bgmClip = loadClipFromFile(filePath);
        currentBgmPath = filePath;

        if (bgmClip == null) return;

        applyVolume(bgmClip);
        bgmClip.setFramePosition(0);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgmClip.start();
    }

    // ====== אפקטים של שאלות ותוצאות ======

    public void playCorrectThenResumeGame() {
        stopBgm();
        playSfxAndThen(SND_CORRECT, this::playGameLoop);
    }

    public void playWrongThenResumeGame() {
        stopBgm();
        playSfxAndThen(SND_WRONG, this::playGameLoop);
    }

    // ====== הפתעות ======

    public void playGoodSurpriseThenResumeGame() {
        playSfxAndThen(SND_GOOD_SUR, this::playGameLoop);
    }

    public void playBadSurpriseThenResumeGame() {
        playSfxAndThen(SND_BAD_SUR, this::playGameLoop);
    }

    // ====== ווליום ומיוט ======

    public void setVolume(int v) {
        volume = Math.max(0, Math.min(100, v));
        applyVolume(bgmClip);
        applyVolume(currentSfxClip);
    }

    public int getVolume() { return volume; }

    public void setMuted(boolean m) {
        muted = m;
        if (muted) {
            if (bgmClip != null) bgmClip.stop();
        } else {
            if (bgmClip != null) {
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            }
        }
    }

    public boolean isMuted() { return muted; }

    // ====== פנימי – SFX ======

    private void playSfxAndThen(String resourcePath, Runnable after) {
        if (muted) {
            if (after != null) after.run();
            return;
        }

        Clip sfx = loadClipFromResource(resourcePath);
        if (sfx == null) {
            if (after != null) after.run();
            return;
        }

        applyVolume(sfx);
        sfx.setFramePosition(0);

        sfx.addLineListener(ev -> {
            if (ev.getType() == LineEvent.Type.STOP) {
                sfx.close();
                if (after != null) after.run();
            }
        });

        sfx.start();
    }

    // ====== טעינת קליפים ======

    /** טעינה מקובץ משאב (resource בתוך ה-jar / src) */
    private Clip loadClipFromResource(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Missing sound resource: " + path);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            return buildPcmClip(ais);

        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /** טעינה מקובץ WAV חיצוני */
    private Clip loadClipFromFile(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                System.err.println("Sound file not found: " + filePath);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            return buildPcmClip(ais);

        } catch (Exception e) {
            System.err.println("Failed to load sound file: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    /** המרה ל-PCM ופתיחת Clip */
    private Clip buildPcmClip(AudioInputStream ais) throws Exception {
        AudioFormat base = ais.getFormat();

        AudioFormat decoded = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.getSampleRate(),
                16,
                base.getChannels(),
                base.getChannels() * 2,
                base.getSampleRate(),
                false
        );

        AudioInputStream dais = AudioSystem.getAudioInputStream(decoded, ais);

        Clip clip = AudioSystem.getClip();
        clip.open(dais);
        return clip;
    }

    private void stopClip(Clip c) {
        try {
            if (c == null) return;
            c.stop();
            c.close();
        } catch (Exception ignored) {}
    }

    private void applyVolume(Clip c) {
        if (c == null) return;
        try {
            if (!c.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;

            FloatControl gain = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);

            float v = Math.max(0.0001f, volume / 100f);
            float dB = (float) (20.0 * Math.log10(v));

            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));

            gain.setValue(dB);

        } catch (Exception ignored) {}
    }

    // ====== גרסה שבה תשובה נכונה מתנגנת 5 שניות ואז חוזרים למשחק ======
    public synchronized void playCorrectFor5SecondsThenResumeGame() {
        stopBgm();

        if (muted) {
            playGameLoop();
            return;
        }

        stopSfx();

        Clip sfx = loadClipFromResource(SND_CORRECT);
        if (sfx == null) {
            playGameLoop();
            return;
        }

        currentSfxClip = sfx;

        applyVolume(sfx);
        sfx.setFramePosition(0);
        sfx.start();

        scheduler.schedule(() -> {
            synchronized (SoundManager.this) {
                stopSfx();
                playGameLoop();
            }
        }, 5, TimeUnit.SECONDS);
    }

    private synchronized void stopSfx() {
        try {
            if (currentSfxClip != null) {
                currentSfxClip.stop();
                currentSfxClip.close();
                currentSfxClip = null;
            }
        } catch (Exception ignored) {}
    }
}
