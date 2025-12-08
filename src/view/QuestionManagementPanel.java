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
        JLabel titleLabel = new JLabel("Question Wizard", SwingConstants.CENTER);
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

        JButton addBtn = createButton("Add qustion ");
        JButton editBtn = createButton("Edit");
        JButton deleteBtn = createButton("Delete");
        JButton saveBtn = createButton("Save changes to CSV");
        JButton backBtn = createButton("back");

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
                JOptionPane.showMessageDialog(this, "יSelect a row in the table to edit.");
                return;
            }
            showQuestionDialog(selectedRow);
        });

        // 3. מחיקת שאלה
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a row to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "Are you sure you want to delete the selected question?", 
                    "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                	// הסרת השורה מהמודל רק אם המשתמש אישר
                    tableModel.removeRow(selectedRow);
                }
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
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
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
            
            JOptionPane.showMessageDialog(this, "The data was successfully saved to a CSV file!");
            model.QuestionBank.getInstance().reloadQuestions();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
        }
    }

    /**
     * מציג דיאלוג להוספה (rowToEdit=null) או עריכה (rowToEdit=מספר שורה).
     */
    /**
     * מציג דיאלוג להוספה (rowToEdit=null) או עריכה (rowToEdit=מספר שורה).
     */
    private void showQuestionDialog(Integer rowToEdit) {
        JDialog dialog = new JDialog(parent, "Make question", true);
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
            // --- שינוי: יצירת ID ייחודי (Max ID + 1) במקום לפי מספר שורות ---
            idField.setText(getNextId());
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

        JButton okBtn = new JButton("confirmation");
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
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.");
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

    /**
     * פונקציית עזר למציאת ה-ID הפנוי הבא (המקסימלי + 1).
     * מונע כפילויות גם אם מחקנו שאלות באמצע.
     */
    private String getNextId() {
        int maxId = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                // קריאת ה-ID מעמודה 0 והמרה למספר
                String idStr = (String) tableModel.getValueAt(i, 0);
                int id = Integer.parseInt(idStr);
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                // מתעלמים משורות עם ID לא תקין או ריק
            }
        }
        return String.valueOf(maxId + 1);
    }
    
    
}