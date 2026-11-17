// ReportPanel.java - four simple reports shown in a table.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class ReportPanel extends JPanel {

    String[] reportNames = {"Employee List", "Attendance Report", "Salary Report", "Monthly Summary"};
    JComboBox<String> reportBox = new JComboBox<>(reportNames);
    JTextField monthField = new JTextField(LocalDate.now().toString().substring(0, 7), 8);  // yyyy-MM
    JTable table = new JTable();

    public ReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton showButton = new JButton("Show Report");
        bar.add(new JLabel("Report:"));
        bar.add(reportBox);
        bar.add(new JLabel("Month (yyyy-MM):"));
        bar.add(monthField);
        bar.add(showButton);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(bar, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        showButton.addActionListener(e -> showReport());
    }

    public void refresh() {
        showReport();
    }

    void showReport() {
        String month = monthField.getText().trim();
        String report = (String) reportBox.getSelectedItem();
        DefaultTableModel model;

        try {
            if (report.equals("Employee List")) {
                model = Database.newModel("ID", "Name", "Phone", "Email", "Department", "Basic Salary");
                Database.fill(model,
                        "SELECT e.id, e.name, e.phone, e.email, IFNULL(d.department_name, ''), e.basic_salary "
                      + "FROM employees e LEFT JOIN departments d ON e.department_id = d.id ORDER BY e.id");

            } else if (report.equals("Attendance Report")) {
                model = Database.newModel("Employee", "Date", "Status");
                Database.fill(model,
                        "SELECT e.name, a.attendance_date, a.status FROM attendance a "
                      + "JOIN employees e ON a.employee_id = e.id "
                      + "WHERE a.attendance_date LIKE ? ORDER BY a.attendance_date", month + "%");

            } else if (report.equals("Salary Report")) {
                model = Database.newModel("Employee", "Month", "Basic", "Bonus", "Deduction", "Net Salary");
                Database.fill(model,
                        "SELECT e.name, s.salary_month, s.basic_salary, s.bonus, s.deduction, s.net_salary "
                      + "FROM salary s JOIN employees e ON s.employee_id = e.id "
                      + "WHERE s.salary_month = ?", month);

            } else {   // Monthly Summary
                model = Database.newModel("Employee", "Present Days", "Absent Days", "Net Salary");
                Database.fill(model,
                        "SELECT e.name, "
                      + "(SELECT COUNT(*) FROM attendance a WHERE a.employee_id = e.id AND a.status='Present' AND a.attendance_date LIKE ?), "
                      + "(SELECT COUNT(*) FROM attendance a WHERE a.employee_id = e.id AND a.status='Absent' AND a.attendance_date LIKE ?), "
                      + "(SELECT s.net_salary FROM salary s WHERE s.employee_id = e.id AND s.salary_month = ?) "
                      + "FROM employees e",
                        month + "%", month + "%", month);
            }
            table.setModel(model);
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
