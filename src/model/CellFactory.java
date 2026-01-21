package model;

public class CellFactory {

    public static Cell createCell(CellType type) {
        return new Cell(type);
    }
}
