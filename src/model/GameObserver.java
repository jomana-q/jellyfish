package model;
public interface GameObserver {
    void onGameStateChanged(GameSession session);
}
