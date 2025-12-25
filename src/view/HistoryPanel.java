package view;

import model.GameHistory;
import model.GameHistoryEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

        JLabel titleLabel = new JLabel("Game History & Top Scores", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Player Name", "Score", "Difficulty", "Result", "Duration (sec)", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.setBackground(new Color(255, 255, 255, 240));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(255, 255, 255, 200));
        add(scrollPane, BorderLayout.CENTER);

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
    }
}
