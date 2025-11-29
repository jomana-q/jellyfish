package model;

public class FlagResult {
    public final boolean success;
    public final String message;
    public final int points;

    public FlagResult(boolean success, String message, int points) {
        this.success = success;
        this.message = message;
        this.points = points;
    }
}