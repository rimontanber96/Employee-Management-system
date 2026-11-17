// AttendancePanel.java - mark Present / Absent and see attendance history.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class AttendancePanel extends JPanel {

    JComboBox<String> employeeBox = new JComboBox<>();
    JTextField dateField = new JTextField(LocalDate.now().toString(), 10);   // yyyy-MM-dd
    DefaultTableModel model = Database.newModel("Date", "Status");

    public AttendancePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Attendance Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // top bar: choose employee + date + buttons
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton presentButton = new JButton("Mark Present");
        JButton absentButton = new JButton("Mark Absent");
        bar.add(new JLabel("Employee:"));
        bar.add(employeeBox);
        bar.add(new JLabel("Date (yyyy-MM-dd):"));
        bar.add(dateField);
        bar.add(presentButton);
        bar.add(absentButton);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(bar, BorderLayout.NORTH);
        center.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        presentButton.addActionListener(e -> mark("Present"));
        absentButton.addActionListener(e -> mark("Absent"));

        // show history when another employee is selected
        employeeBox.addActionListener(e -> showHistory());
    }

    public void refresh() {
        Database.fillCombo(employeeBox, "SELECT id, name FROM employees");
        showHistory();
    }

    void mark(String status) {
        int employeeId = Database.idOf(employeeBox);
        if (employeeId == -1) {
            Database.message("Please add an employee first.");
            return;
        }
        String date = dateField.getText().trim();
        try {
            LocalDate.parse(date);        // checks that the date is correct
            // remove an old mark for the same day, then save the new one
            Database.run("DELETE FROM attendance WHERE employee_id=? AND attendance_date=?", employeeId, date);
            Database.run("INSERT INTO attendance (employee_id, attendance_date, status) VALUES (?, ?, ?)",
                    employeeId, date, status);
            showHistory();
        } catch (java.time.format.DateTimeParseException e) {
            Database.message("Please write the date like 2026-11-17");
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void showHistory() {
        int employeeId = Database.idOf(employeeBox);
        try {
            Database.fill(model,
                    "SELECT attendance_date, status FROM attendance WHERE employee_id=? ORDER BY attendance_date DESC",
                    employeeId);
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
