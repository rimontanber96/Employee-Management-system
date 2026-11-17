// DashboardPanel.java - overview numbers, recent employees and quick buttons.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class DashboardPanel extends JPanel {

    JLabel totalEmployees = new JLabel("0", JLabel.CENTER);
    JLabel presentToday = new JLabel("0", JLabel.CENTER);
    JLabel absentToday = new JLabel("0", JLabel.CENTER);
    JLabel totalDepartments = new JLabel("0", JLabel.CENTER);

    DefaultTableModel model = Database.newModel("ID", "Name", "Phone", "Email");

    public DashboardPanel(MainWindow window) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 22));

        // ----- 4 number boxes -----
        JPanel boxes = new JPanel(new GridLayout(1, 4, 10, 10));
        boxes.add(makeBox("Total Employees", totalEmployees));
        boxes.add(makeBox("Present Today", presentToday));
        boxes.add(makeBox("Absent Today", absentToday));
        boxes.add(makeBox("Departments", totalDepartments));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.add(title, BorderLayout.NORTH);
        top.add(boxes, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // ----- Recently added employees -----
        JPanel middle = new JPanel(new BorderLayout(5, 5));
        middle.add(new JLabel("Recently added employees:"), BorderLayout.NORTH);
        middle.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);

        // ----- Quick buttons -----
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton b1 = new JButton("Add Employee");
        JButton b2 = new JButton("Mark Attendance");
        JButton b3 = new JButton("Enter Salary");
        JButton b4 = new JButton("View Reports");
        b1.addActionListener(e -> window.showPage("Employees"));
        b2.addActionListener(e -> window.showPage("Attendance"));
        b3.addActionListener(e -> window.showPage("Salary"));
        b4.addActionListener(e -> window.showPage("Reports"));
        quick.add(b1); quick.add(b2); quick.add(b3); quick.add(b4);
        add(quick, BorderLayout.SOUTH);
    }

    // A small box with a title and a big number
    private JPanel makeBox(String text, JLabel number) {
        number.setFont(new Font("Arial", Font.BOLD, 36));
        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        box.add(new JLabel(text, JLabel.CENTER), BorderLayout.NORTH);
        box.add(number, BorderLayout.CENTER);
        return box;
    }

    // Load fresh numbers from the database
    public void refresh() {
        try {
            String today = LocalDate.now().toString();     // like 2026-11-17
            totalEmployees.setText("" + Database.count("SELECT COUNT(*) FROM employees"));
            totalDepartments.setText("" + Database.count("SELECT COUNT(*) FROM departments"));
            presentToday.setText("" + Database.count(
                    "SELECT COUNT(*) FROM attendance WHERE attendance_date=? AND status='Present'", today));
            absentToday.setText("" + Database.count(
                    "SELECT COUNT(*) FROM attendance WHERE attendance_date=? AND status='Absent'", today));
            Database.fill(model, "SELECT id, name, phone, email FROM employees ORDER BY id DESC LIMIT 5");
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
