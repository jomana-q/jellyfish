package view;

import javax.swing.JFrame;
import model.Board;
import model.GameSession;
import controller.MinesweeperController;

public class MinesweeperGUI extends JFrame {

    // מחלקה זמנית (Placeholder) כדי למנוע שגיאות קומפילציה ב-Controller
    
    public MinesweeperGUI(Board board, GameSession session) {
        setTitle("Game Board - Under Construction");
        setSize(400, 300);
        // כרגע אין כאן קוד, זה יפותח בהמשך
    }

    // פונקציות ריקות שהבקר צריך כדי לא לקרוס
    
    public void setController(MinesweeperController controller) {
        // TODO: ימומש בהמשך ע"י המפתח האחראי על ה-GUI
    }

    public void refreshView() {
        // TODO: ימומש בהמשך - עדכון הלוח הגרפי
    }

    public void showGameOver() {
        // TODO: ימומש בהמשך - הצגת הודעת הפסד
    }
}