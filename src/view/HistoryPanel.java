package view;

import model.GameHistory;
import model.GameHistoryEntry;
import model.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final MainMenuGUI parent;
    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryPanel(MainMenuGUI parent) {
        this.parent = parent;
        initializeUI();
        loadHistoryData();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // 1. כותרת (Title) - דינמית
        JLabel titleLabel = new JLabel("Game History & Top Scores", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getTextColor());
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        // שמות העמודות
        String[] columnNames = {"Player Name", "Score", "Difficulty", "Result", "Duration (sec)", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(35); // שורות קצת יותר גבוהות
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setOpaque(false);
        
        // הגדרת ה-Renderer המותאם אישית שלנו (זה הסוד לצבעים)
        table.setDefaultRenderer(Object.class, new HistoryCellRenderer());

        // עיצוב הכותרת של הטבלה
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(60, 120, 200)); // כחול
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // גלילה שקופה
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0)); // רקע שקוף מאחורי הטבלה
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1));
        
        add(scrollPane, BorderLayout.CENTER);

        // כפתור חזרה
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);

        JButton backBtn = new JButton("Back to Admin Dashboard");
        styleButton(backBtn);
        backBtn.addActionListener(e -> parent.showAdminDashboard());

        bottomPanel.add(backBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadHistoryData() {
        GameHistory historyHandler = new GameHistory();
        List<GameHistoryEntry> entries = historyHandler.getTopScores();

        tableModel.setRowCount(0);

        for (GameHistoryEntry entry : entries) {
            Object[] rowData = {
                    entry.getPlayerName(),
                    entry.getScore(),
                    entry.getDifficulty(),
                    entry.getResult(),       // עמודה 3 (Result) משמשת אותנו לצביעה
                    entry.getDurationSeconds(),
                    entry.getDate()
            };
            tableModel.addRow(rowData);
        }
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(250, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ==========================================================
    // מחלקה פנימית לצביעת הטבלה (Custom Renderer)
    // ==========================================================
    private static class HistoryCellRenderer extends DefaultTableCellRenderer {
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // בדיקת ערך עמודת "Result" (אינדקס 3)
            String result = (String) table.getModel().getValueAt(row, 3);
            boolean isWin = result != null && (result.contains("Won") || result.contains("revealed"));
            
            // בדיקת מצב כהה/בהיר
            boolean isDark = ThemeManager.getInstance().isDarkMode();

            // צבעי רקע
            if (isSelected) {
                c.setBackground(new Color(0, 120, 215)); // צבע בחירה כחול רגיל
                c.setForeground(Color.WHITE);
            } else {
                if (isWin) {
                    // ירוק: כהה יותר במצב Dark, בהיר יותר במצב Light
                    c.setBackground(isDark ? new Color(20, 60, 30, 200) : new Color(210, 255, 210, 230));
                } else {
                    // אדום: כהה יותר במצב Dark, בהיר יותר במצב Light
                    c.setBackground(isDark ? new Color(60, 20, 20, 200) : new Color(255, 210, 210, 230));
                }
                
                // צבע טקסט: לבן במצב כהה, שחור במצב בהיר
                c.setForeground(isDark ? Color.WHITE : Color.BLACK);
            }

            // מרכוז הטקסט
            setHorizontalAlignment(SwingConstants.CENTER);
            
            return c;
        }
    }
}