package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * מסך ניהול שאלות (Admin Wizard).
 * מאפשר: צפייה בטבלה, הוספה, עריכה, מחיקה ושמירה לקובץ CSV.
 */
public class QuestionManagementPanel extends JPanel {

    private final MainMenuGUI parent;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String CSV_FILE = "questions.csv";

    public QuestionManagementPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
        loadQuestionsFromCSV();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Question Wizard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Question", "Difficulty", "Ans A", "Ans B", "Ans C", "Ans D", "Correct"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setOpaque(false);

        JButton addBtn = createButton("Add question");
        JButton editBtn = createButton("Edit");
        JButton deleteBtn = createButton("Delete");
        JButton saveBtn = createButton("Save changes to CSV");
        JButton backBtn = createButton("Back");

        saveBtn.setBackground(new Color(60, 140, 60));

        buttonsPanel.add(addBtn);
        buttonsPanel.add(editBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(Box.createHorizontalStrut(20));
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(backBtn);

        add(buttonsPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> showQuestionDialog(null));

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a row in the table to edit.");
                return;
            }
            showQuestionDialog(selectedRow);
        });

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
                tableModel.removeRow(selectedRow);
            }
        });

        saveBtn.addActionListener(e -> saveQuestionsToCSV());

        backBtn.addActionListener(e -> parent.showAdminDashboard());
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadQuestionsFromCSV() {
        File file = new File(CSV_FILE);

        System.out.println("System: Admin loading questions from: " + file.getAbsolutePath());
        System.out.println("System: Exists=" + file.exists() + " size=" + (file.exists() ? file.length() : -1));

        if (!file.exists()) return;

        tableModel.setRowCount(0);

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null || header.trim().isEmpty()) {
                System.err.println("Admin: CSV file is empty.");
                return;
            }
            header = header.replace("\uFEFF", "");

            char delimiter = detectDelimiter(header);

            String line;
            int rowNum = 1;

            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                String[] data = parseLine(line, delimiter);

                if (data.length < 8) {
                    System.err.println("Admin: skipped bad row " + rowNum + ": " + line);
                    continue;
                }

                // Normalize to exactly 8 columns
                Object[] row = new Object[8];
                for (int i = 0; i < 8; i++) row[i] = data[i];

                tableModel.addRow(row);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }

    private void saveQuestionsToCSV() {
        File file = new File(CSV_FILE);

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // Always save as real CSV with commas
            bw.write("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            bw.newLine();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                List<String> fields = new ArrayList<>(8);
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    Object val = tableModel.getValueAt(i, j);
                    fields.add(escapeCsv(val == null ? "" : val.toString()));
                }
                bw.write(String.join(",", fields));
                bw.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage());
            return;
        }

        System.out.println("System: Saved questions.csv size=" + file.length());
        JOptionPane.showMessageDialog(this, "The data was successfully saved to a CSV file!");

        // Reload questions in runtime
        model.QuestionBank.getInstance().reloadQuestions();
    }

    private void showQuestionDialog(Integer rowToEdit) {
        JDialog dialog = new JDialog(parent, "Make question", true);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));
        dialog.setSize(520, 460);
        dialog.setLocationRelativeTo(this);

        JTextField idField = new JTextField();
        JTextField qField = new JTextField();

        String[] levels = {"1", "2", "3", "4"};
        JComboBox<String> diffBox = new JComboBox<>(levels);

        JTextField ansA = new JTextField();
        JTextField ansB = new JTextField();
        JTextField ansC = new JTextField();
        JTextField ansD = new JTextField();

        String[] correctOpts = {"A", "B", "C", "D"};
        JComboBox<String> correctBox = new JComboBox<>(correctOpts);

        if (rowToEdit != null) {
            idField.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 0)));
            qField.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 1)));
            diffBox.setSelectedItem(String.valueOf(tableModel.getValueAt(rowToEdit, 2)));
            ansA.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 3)));
            ansB.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 4)));
            ansC.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 5)));
            ansD.setText(String.valueOf(tableModel.getValueAt(rowToEdit, 6)));
            correctBox.setSelectedItem(String.valueOf(tableModel.getValueAt(rowToEdit, 7)));
            idField.setEditable(false);
        } else {
            idField.setText(getNextId());
        }

        dialog.add(new JLabel("ID:")); dialog.add(idField);
        dialog.add(new JLabel("Question:")); dialog.add(qField);
        dialog.add(new JLabel("Difficulty (1-4):")); dialog.add(diffBox);
        dialog.add(new JLabel("Answer A:")); dialog.add(ansA);
        dialog.add(new JLabel("Answer B:")); dialog.add(ansB);
        dialog.add(new JLabel("Answer C:")); dialog.add(ansC);
        dialog.add(new JLabel("Answer D:")); dialog.add(ansD);
        dialog.add(new JLabel("Correct Answer:")); dialog.add(correctBox);

        JButton okBtn = new JButton("Confirmation");
        okBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String q  = qField.getText().trim();
            String d  = String.valueOf(diffBox.getSelectedItem()).trim();

            String a = ansA.getText().trim();
            String b = ansB.getText().trim();
            String c = ansC.getText().trim();
            String dd= ansD.getText().trim();

            String corr = String.valueOf(correctBox.getSelectedItem()).trim();

            if (id.isEmpty() || q.isEmpty() || d.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || dd.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields (including Answer D).");
                return;
            }

            String[] rowData = { id, q, d, a, b, c, dd, corr };

            if (rowToEdit != null) {
                for (int i = 0; i < rowData.length; i++) {
                    tableModel.setValueAt(rowData[i], rowToEdit, i);
                }
            } else {
                tableModel.addRow(rowData);
            }
            dialog.dispose();
        });

        dialog.add(new JLabel(""));
        dialog.add(okBtn);

        dialog.setVisible(true);
    }

    private String getNextId() {
        int maxId = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                int id = Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 0)).trim());
                if (id > maxId) maxId = id;
            } catch (Exception ignored) {}
        }
        return String.valueOf(maxId + 1);
    }

    // -------- CSV helpers --------

    private char detectDelimiter(String header) {
        if (header.contains("\t")) return '\t';
        if (header.contains(","))  return ',';
        if (header.contains(";"))  return ';';
        return ',';
    }

    private String[] parseLine(String line, char delimiter) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean mustQuote = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String t = s.replace("\"", "\"\"");
        return mustQuote ? "\"" + t + "\"" : t;
    }
}
