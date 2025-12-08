package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

/**
 * מסך ניהול שאלות (Admin Wizard).
 * מאפשר: צפייה בטבלה, הוספה, עריכה, מחיקה ושמירה לקובץ CSV.
 */
public class QuestionManagementPanel extends JPanel {

    private final MainMenuGUI parent;
    private JTable table;
    private DefaultTableModel tableModel;
    
    // נתיב לקובץ ה-CSV (חייב להיות תואם לקובץ בתיקייה הראשית)
    private static final String CSV_FILE = "questions.csv";

    public QuestionManagementPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
        loadQuestionsFromCSV(); // טעינת הנתונים בעת פתיחת המסך
    }

    /** בניית ממשק המשתמש */
    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- כותרת ---
        JLabel titleLabel = new JLabel("ניהול שאלות (Question Wizard)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // --- טבלה ---
        // הגדרת עמודות הטבלה לפי המבנה של קובץ ה-CSV
        String[] columnNames = {"ID", "Question", "Difficulty", "Ans A", "Ans B", "Ans C", "Ans D", "Correct"};
        
        // יצירת מודל לטבלה שלא מאפשר עריכה ישירה בתאים (כדי למנוע טעויות)
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // עריכה תתבצע רק דרך כפתור "Edit"
            }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // גלילה לטבלה
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // --- פאנל כפתורים למטה ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setOpaque(false);

        JButton addBtn = createButton("הוסף שאלה");
        JButton editBtn = createButton("ערוך נבחר");
        JButton deleteBtn = createButton("מחק נבחר");
        JButton saveBtn = createButton("שמור שינויים ל-CSV");
        JButton backBtn = createButton("חזרה");

        // צבע מיוחד לכפתור שמירה
        saveBtn.setBackground(new Color(60, 140, 60));

        buttonsPanel.add(addBtn);
        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(Box.createHorizontalStrut(20)); // רווח
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);

        // --- הגדרת פעולות לכפתורים (Listeners) ---

        // 1. הוספת שאלה חדשה
        addBtn.addActionListener(e -> showQuestionDialog(null));

        // 2. עריכת שאלה קיימת
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "יש לבחור שורה בטבלה לעריכה.");
                return;
            }
            showQuestionDialog(selectedRow);
        });

        // 3. מחיקת שאלה
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "יש לבחור שורה למחיקה.");
                return;
            }
            // הסרת השורה מהמודל (עדיין לא נשמר לקובץ עד שלוחצים שמור)
            tableModel.removeRow(selectedRow);
        });

        // 4. שמירת כל הטבלה לקובץ ה-CSV
        saveBtn.addActionListener(e -> saveQuestionsToCSV());

        // 5. חזרה לתפריט אדמין
        backBtn.addActionListener(e -> parent.showAdminDashboard());
    }

    /**
     * פונקציית עזר ליצירת כפתור מעוצב.
     */
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    /**
     * טעינת הנתונים מקובץ ה-CSV לתוך הטבלה.
     */
    private void loadQuestionsFromCSV() {
        File file = new File(CSV_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true; // כדי לדלג על הכותרת

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                // פיצול לפי פסיק (או תו אחר אם צריך)
                // שימוש ב-regex שמתמודד עם פסיקים
                String[] data = line.split(",", -1);
                
                // הוספה לטבלה רק אם השורה תקינה (8 עמודות)
                if (data.length >= 8) {
                    tableModel.addRow(data);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "שגיאה בטעינת הקובץ: " + e.getMessage());
        }
    }

    /**
     * שמירת כל הנתונים מהטבלה חזרה לקובץ ה-CSV.
     * דורס את הקובץ הקיים.
     */
    private void saveQuestionsToCSV() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(CSV_FILE), StandardCharsets.UTF_8))) {
            
            // כתיבת כותרות העמודות
            bw.write("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            bw.newLine();

            // מעבר על כל שורות הטבלה
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object val = tableModel.getValueAt(i, j);
                    sb.append(val == null ? "" : val.toString());
                    
                    // הוספת פסיק אם זה לא העמודה האחרונה
                    if (j < tableModel.getColumnCount() - 1) {
                        sb.append(",");
                    }
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            
            JOptionPane.showMessageDialog(this, "הנתונים נשמרו בהצלחה לקובץ CSV!");
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "שגיאה בשמירת הקובץ: " + e.getMessage());
        }
    }

    /**
     * מציג דיאלוג להוספה (rowToEdit=null) או עריכה (rowToEdit=מספר שורה).
     */
    private void showQuestionDialog(Integer rowToEdit) {
        JDialog dialog = new JDialog(parent, "עורך שאלות", true);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);

        // שדות הקלט
        JTextField idField = new JTextField();
        JTextField qField = new JTextField();
        
        // רמת קושי: 1, 2, 3, 4
        String[] levels = {"1", "2", "3", "4"};
        JComboBox<String> diffBox = new JComboBox<>(levels);
        
        JTextField ansA = new JTextField();
        JTextField ansB = new JTextField();
        JTextField ansC = new JTextField();
        JTextField ansD = new JTextField();
        
        // תשובה נכונה: A, B, C, D
        String[] correctOpts = {"A", "B", "C", "D"};
        JComboBox<String> correctBox = new JComboBox<>(correctOpts);

        // אם אנחנו במצב עריכה - נמלא את השדות בנתונים הקיימים
        if (rowToEdit != null) {
            idField.setText((String) tableModel.getValueAt(rowToEdit, 0));
            qField.setText((String) tableModel.getValueAt(rowToEdit, 1));
            diffBox.setSelectedItem(tableModel.getValueAt(rowToEdit, 2));
            ansA.setText((String) tableModel.getValueAt(rowToEdit, 3));
            ansB.setText((String) tableModel.getValueAt(rowToEdit, 4));
            ansC.setText((String) tableModel.getValueAt(rowToEdit, 5));
            ansD.setText((String) tableModel.getValueAt(rowToEdit, 6));
            correctBox.setSelectedItem(tableModel.getValueAt(rowToEdit, 7));
            
            idField.setEditable(false); // בדרך כלל לא משנים ID בעריכה
        } else {
            // יצירת ID אוטומטי במקרה של הוספה (לפי מספר השורות + 1)
            idField.setText(String.valueOf(tableModel.getRowCount() + 1));
        }

        // הוספת הרכיבים לדיאלוג
        dialog.add(new JLabel("ID:")); dialog.add(idField);
        dialog.add(new JLabel("Question:")); dialog.add(qField);
        dialog.add(new JLabel("Difficulty (1-4):")); dialog.add(diffBox);
        dialog.add(new JLabel("Answer A:")); dialog.add(ansA);
        dialog.add(new JLabel("Answer B:")); dialog.add(ansB);
        dialog.add(new JLabel("Answer C:")); dialog.add(ansC);
        dialog.add(new JLabel("Answer D:")); dialog.add(ansD);
        dialog.add(new JLabel("Correct Answer:")); dialog.add(correctBox);

        JButton okBtn = new JButton("אישור");
        okBtn.addActionListener(e -> {
            // איסוף הנתונים
            String[] rowData = {
                idField.getText(),
                qField.getText(),
                (String) diffBox.getSelectedItem(),
                ansA.getText(),
                ansB.getText(),
                ansC.getText(),
                ansD.getText(),
                (String) correctBox.getSelectedItem()
            };

            // בדיקה בסיסית שאין שדות ריקים
            if (qField.getText().isEmpty() || ansA.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "נא למלא את כל השדות.");
                return;
            }

            if (rowToEdit != null) {
                // עדכון שורה קיימת
                for (int i = 0; i < rowData.length; i++) {
                    tableModel.setValueAt(rowData[i], rowToEdit, i);
                }
            } else {
                // הוספת שורה חדשה
                tableModel.addRow(rowData);
            }
            dialog.dispose();
        });

        dialog.add(new JLabel("")); // סתם כדי למלא מקום
        dialog.add(okBtn);

        dialog.setVisible(true);
    }
}