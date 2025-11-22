package model;

public class Cell {

    private CellType type;        // סוג התא
    private boolean revealed;     // האם התא נפתח
    private boolean flagged;      // האם מסומן בדגל
    private int adjacentMines;    // מספר מוקשים מסביב (אם NUMBER)

    public Cell(CellType type) {
        this.type = type;
        this.revealed = false;
        this.flagged = false;
        this.adjacentMines = 0;
    }

    public CellType getType() {
        return type;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
