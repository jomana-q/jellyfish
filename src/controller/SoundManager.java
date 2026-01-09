package controller;

import javax.sound.sampled.*;
import java.net.URL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



public class SoundManager {

    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    // ====== קבצי סאונד מהפרויקט (src/sounds) ======
    private static final String SND_MENU      = "/sounds/GameMenuSound.wav";
    private static final String SND_GAME      = "/sounds/GameDurationSound.wav";
    private static final String SND_QUESTION  = "/sounds/QuestionDurationSound.wav";
    private static final String SND_CORRECT   = "/sounds/CorrectAnswerSound.wav";
    private static final String SND_WRONG     = "/sounds/WrongAnswerSound.wav";
    private static final String SND_GOOD_SUR  = "/sounds/GoodSurpriseSound.wav";
    private static final String SND_BAD_SUR   = "/sounds/BadSurpriseSound.wav";
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private Clip currentSfxClip = null;


    private boolean muted = false;
    private int volume = 60; // 0..100

    private Clip bgmClip;
    private String currentBgmPath = null;

    private SoundManager() {}

    // ====== מוזיקה (Loop) ======
    public void playMenuLoop()     { playBgmLoop(SND_MENU); }
    public void playGameLoop()     { playBgmLoop(SND_GAME); }
    public void playQuestionLoop() { playBgmLoop(SND_QUESTION); }

    public void stopBgm() {
        stopClip(bgmClip);
        bgmClip = null;
        currentBgmPath = null;
    }

    // ====== תוצאות שאלה ======
    public void playCorrectThenResumeGame() {
        stopBgm();
        playSfxAndThen(SND_CORRECT, this::playGameLoop);
    }

    public void playWrongThenResumeGame() {
        stopBgm();
        playSfxAndThen(SND_WRONG, this::playGameLoop);
    }

    // ====== Surprise ======
    public void playGoodSurpriseThenResumeGame() {
        playSfxAndThen(SND_GOOD_SUR, this::playGameLoop);
    }

    public void playBadSurpriseThenResumeGame() {
        playSfxAndThen(SND_BAD_SUR, this::playGameLoop);
    }

    // ====== Volume + Mute ======
    public void setVolume(int v) {
        volume = Math.max(0, Math.min(100, v));
        applyVolume(bgmClip);
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

    // ====== Internal ======

    private void playBgmLoop(String path) {
        if (muted) return;

        if (path != null && path.equals(currentBgmPath) && bgmClip != null && bgmClip.isOpen()) {
            return; // כבר מנגן את זה
        }

        stopClip(bgmClip);

        bgmClip = loadClip(path);
        currentBgmPath = path;

        if (bgmClip == null) return;

        applyVolume(bgmClip);
        bgmClip.setFramePosition(0);
        bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        bgmClip.start();
    }

    private void playSfxAndThen(String path, Runnable after) {
        if (muted) {
            if (after != null) after.run();
            return;
        }

        Clip sfx = loadClip(path);
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

    private Clip loadClip(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("Missing sound resource: " + path);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
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

        } catch (Exception e) {
            System.err.println("Failed to load sound: " + path);
            e.printStackTrace();
            return null;
        }
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
 
    public synchronized void playCorrectFor5SecondsThenResumeGame() {
        stopBgm();

        if (muted) {
            playGameLoop();
            return;
        }

        // אם יש SFX קודם שעדיין מנגן – לעצור אותו
        stopSfx();

        Clip sfx = loadClip(SND_CORRECT);
        if (sfx == null) {
            playGameLoop();
            return;
        }

        currentSfxClip = sfx;

        applyVolume(sfx);
        sfx.setFramePosition(0);
        sfx.start();

        // ✅ עצירה קשיחה אחרי 5 שניות (לא תלוי ב-Swing/EDT)
        scheduler.schedule(() -> {
            synchronized (SoundManager.this) {
                stopSfx();      // עוצר וסוגר את הקליפ
                playGameLoop(); // חוזר למוזיקת המשחק
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
