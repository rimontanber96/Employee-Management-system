// MainWindow.java - the main window: a sidebar on the left, pages on the right.
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    CardLayout cards = new CardLayout();          // shows one page at a time
    JPanel content = new JPanel(cards);

    DashboardPanel dashboard = new DashboardPanel(this);
    EmployeePanel employees = new EmployeePanel();
    DepartmentPanel departments = new DepartmentPanel();
    AttendancePanel attendance = new AttendancePanel();
    SalaryPanel salary = new SalaryPanel();
    ReportPanel reports = new ReportPanel();

    public MainWindow() {
        setTitle("Employee Attendance & Payroll System");
        setSize(1000, 620);
        setLocationRelativeTo(null);              // center on screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ----- Sidebar -----
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 5));
        sidebar.setBackground(new Color(40, 55, 80));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebar.setPreferredSize(new Dimension(180, 0));

        String[] pages = {"Dashboard", "Employees", "Departments", "Attendance", "Salary", "Reports"};
        for (String page : pages) {
            JButton button = new JButton(page);
            button.addActionListener(e -> showPage(page));
            sidebar.add(button);
        }
        // empty labels so buttons do not become too tall
        for (int i = 0; i < 6; i++) sidebar.add(new JLabel());

        // ----- Pages -----
        content.add(dashboard, "Dashboard");
        content.add(employees, "Employees");
        content.add(departments, "Departments");
        content.add(attendance, "Attendance");
        content.add(salary, "Salary");
        content.add(reports, "Reports");

        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showPage("Dashboard");
    }

    // Show a page and reload its data
    public void showPage(String name) {
        switch (name) {
            case "Dashboard":   dashboard.refresh();   break;
            case "Employees":   employees.refresh();   break;
            case "Departments": departments.refresh(); break;
            case "Attendance":  attendance.refresh();  break;
            case "Salary":      salary.refresh();      break;
            case "Reports":     reports.refresh();     break;
        }
        cards.show(content, name);
    }
}
