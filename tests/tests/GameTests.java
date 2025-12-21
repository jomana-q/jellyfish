package tests;

// ייבוא מחלקות הבדיקה של JUnit
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

// ייבוא המחלקות של המשחק (כדי שנוכל לבדוק אותן)
import model.*; 

public class GameTests {

    // משתנה שיחזיק את סשן המשחק לצורך הבדיקה
    private GameSession session;

    /**
     * פונקציה שרצה לפני כל בדיקה (@Before).
     * המטרה: לאתחל את סביבת הבדיקה (Fixture) כדי שכל טסט יתחיל נקי.
     */
    @Before
    public void setUp() {
        // יצירת סשן משחק חדש ברמת קושי קלה (EASY) לפני כל טסט
        session = new GameSession(Difficulty.EASY); 
    }

    /**
     * טסט 1 (Hadeel): בדיקת כמות חיים התחלתית.
     * המטרה: לוודא שברמת קושי EASY השחקן מתחיל עם 10 חיים בדיוק.
     */
    @Test
    public void testInitialLivesEasy() {
        // 1. הערך הצפוי (Expected) לפי מסמך הדרישות
        int expected = 10;
        
        // 2. הערך בפועל (Actual) שקיבלנו מהמערכת
        int actual = session.getLives();
        
        // 3. ביצוע ההשוואה (Assertion)
        // אם הערכים שווים - הטסט עובר (ירוק). אם לא - נכשל (אדום).
        assertEquals("Initial lives for EASY should be 10", expected, actual);
    }
    
    /**
     *Jumana:
     * בדיקה של ניכוי ניקוד בעת הפעלת משבצת שאלה או הפתעה
     * כאשר לשחקן יש בדיוק מספר נקודות מספיק.
     */
    @Test
    public void testPayForPower_WhenScoreEqualsCost() {
        session = new GameSession(Difficulty.MEDIUM);

        int cost = session.getDifficulty().getPowerCost();
        session.updateScore(cost);

        assertTrue("canPayForPower() should be true when score == cost",
                session.canPayForPower());

        session.payForPower(); 

        assertEquals("Score should be 0 after paying cost",
                0, session.getScore());
    }
    /**
     * Sara:
     * Test that remaining lives are converted to score at the end of the game
     * and lives are reset to zero.
     */
    @Test
    public void testConvertRemainingLivesToScoreAtEnd() {
        session = new GameSession(Difficulty.EASY);

        int initialLives = session.getLives(); // EASY -> 10
        int powerCost = session.getDifficulty().getPowerCost(); // EASY -> 5

        // Expected score after conversion
        int expectedScore = initialLives * powerCost;

        // פעולה
        session.convertRemainingLivesToScoreAtEnd();

        // בדיקות
        assertEquals("All remaining lives should be converted to score",
                expectedScore, session.getScore());

        assertEquals("Lives should be zero after conversion",
                0, session.getLives());
    }


}