package model;

public class ActivationResult {
    public final boolean success;
    public final String message;
    public final int points;
    public final int hearts;

    public ActivationResult(boolean success, String message, int points, int hearts) {
        this.success = success;
        this.message = message;
        this.points = points;
        this.hearts = hearts;
    }
}