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
    private final boolean isFromAdmin; // משתנה חדש: האם הגענו מהאדמין?

    // ⭐ עדכון הבנאי: מקבל פרמטר isFromAdmin
    public HistoryPanel(MainMenuGUI parent, boolean isFromAdmin) {
        this.parent = parent;
        this.isFromAdmin = isFromAdmin; // שמירת המצב
        initializeUI();
        loadHistoryData();
    }

    private void initializeUI() {
        setOpaque(false);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // כותרת
        JLabel titleLabel = new JLabel("Game History & Top Scores", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                setForeground(ThemeManager.getInstance().getTextColor());
                super.paintComponent(g);
            }
        };
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        // טבלה
        String[] columnNames = {"Player Name", "Score", "Difficulty", "Result", "Duration (sec)", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setOpaque(false);
        
        // Renderer לצבעים
        table.setDefaultRenderer(Object.class, new HistoryCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBackground(new Color(60, 120, 200));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(0, 0, 0, 0));
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1));
        
        add(scrollPane, BorderLayout.CENTER);

        // כפתור חזרה
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);

        // ⭐ שינוי הטקסט והפעולה של הכפתור לפי המקור
        String btnText = isFromAdmin ? "Back to Admin Dashboard" : "Back to Main Menu";
        JButton backBtn = new JButton(btnText);
        styleButton(backBtn);
        
        backBtn.addActionListener(e -> {
            if (isFromAdmin) {
                parent.showAdminDashboard(); // חזרה לאדמין
            } else {
                parent.showMainMenu();       // חזרה לתפריט הראשי
            }
        });

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
                    entry.getResult(),
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

    // Custom Renderer לצביעת שורות
    private static class HistoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String result = (String) table.getModel().getValueAt(row, 3);
            boolean isWin = result != null && (result.contains("Won") || result.contains("revealed"));
            boolean isDark = ThemeManager.getInstance().isDarkMode();

            if (isSelected) {
                c.setBackground(new Color(0, 120, 215));
                c.setForeground(Color.WHITE);
            } else {
                if (isWin) {
                    c.setBackground(isDark ? new Color(20, 60, 30, 200) : new Color(210, 255, 210, 230));
                } else {
                    c.setBackground(isDark ? new Color(60, 20, 20, 200) : new Color(255, 210, 210, 230));
                }
                c.setForeground(isDark ? Color.WHITE : Color.BLACK);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}