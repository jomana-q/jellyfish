package model;

public class Cell {

    private CellType type;        // סוג התא
    private boolean revealed;     // האם התא נפתח
    private boolean flagged;      // האם מסומן בדגל
    private int adjacentMines;    // מספר מוקשים מסביב (אם NUMBER)
    private boolean powerUsed;    // האם הופעלה כבר (Question/Surprise)

    public Cell(CellType type) {
        this.type = type;
        this.revealed = false;
        this.flagged = false;
        this.adjacentMines = 0;
        this.powerUsed = false;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public boolean isPowerUsed() {
        return powerUsed;
    }

    public void setPowerUsed(boolean powerUsed) {
        this.powerUsed = powerUsed;
    }
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
}
