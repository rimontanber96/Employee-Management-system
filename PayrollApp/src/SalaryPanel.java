// SalaryPanel.java - enter basic salary, bonus, deduction and calculate net salary.
// Net Salary = Basic Salary + Bonus - Deduction
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class SalaryPanel extends JPanel {

    JComboBox<String> employeeBox = new JComboBox<>();
    JTextField monthField = new JTextField(LocalDate.now().toString().substring(0, 7));  // yyyy-MM
    JTextField basicField = new JTextField();
    JTextField bonusField = new JTextField("0");
    JTextField deductionField = new JTextField("0");
    JLabel netLabel = new JLabel("0.00");

    DefaultTableModel model = Database.newModel("Month", "Basic Salary", "Bonus", "Deduction", "Net Salary");

    public SalaryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Salary Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // ----- Left: form -----
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 8));
        form.add(new JLabel("Employee:"));            form.add(employeeBox);
        form.add(new JLabel("Month (yyyy-MM):"));     form.add(monthField);
        form.add(new JLabel("Basic Salary:"));        form.add(basicField);
        form.add(new JLabel("Bonus:"));               form.add(bonusField);
        form.add(new JLabel("Deduction:"));           form.add(deductionField);
        form.add(new JLabel("Net Salary:"));          form.add(netLabel);
        netLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton calculateButton = new JButton("Calculate");
        JButton saveButton = new JButton("Calculate & Save");
        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        buttons.add(calculateButton);
        buttons.add(saveButton);

        JPanel left = new JPanel(new BorderLayout(5, 15));
        left.setPreferredSize(new Dimension(320, 0));
        left.add(form, BorderLayout.NORTH);
        JPanel buttonHolder = new JPanel(new BorderLayout());
        buttonHolder.add(buttons, BorderLayout.NORTH);
        left.add(buttonHolder, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        // ----- Right: salary history of the selected employee -----
        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.add(new JLabel("Salary records of the selected employee:"), BorderLayout.NORTH);
        right.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        calculateButton.addActionListener(e -> calculate());
        saveButton.addActionListener(e -> save());
        employeeBox.addActionListener(e -> employeeChanged());
    }

    public void refresh() {
        Database.fillCombo(employeeBox, "SELECT id, name FROM employees");
        employeeChanged();
    }

    // When another employee is selected: fill his basic salary and show his records
    void employeeChanged() {
        int employeeId = Database.idOf(employeeBox);
        try {
            Database.fill(model,
                    "SELECT salary_month, basic_salary, bonus, deduction, net_salary "
                  + "FROM salary WHERE employee_id=? ORDER BY salary_month DESC", employeeId);
            if (employeeId != -1) {
                Database.fill(oneValue, "SELECT basic_salary FROM employees WHERE id=?", employeeId);
                if (oneValue.getRowCount() > 0) basicField.setText(String.valueOf(oneValue.getValueAt(0, 0)));
            }
        } catch (Exception e) {
            Database.error(e);
        }
    }
    DefaultTableModel oneValue = Database.newModel("value");   // small helper table (not shown)

    // The salary formula - very simple!
    double calculate() {
        try {
            double basic = Double.parseDouble(basicField.getText().trim());
            double bonus = Double.parseDouble(bonusField.getText().trim());
            double deduction = Double.parseDouble(deductionField.getText().trim());
            double net = basic + bonus - deduction;
            netLabel.setText(String.format("%.2f", net));
            return net;
        } catch (NumberFormatException e) {
            Database.message("Basic salary, bonus and deduction must be numbers.");
            return Double.NaN;
        }
    }

    void save() {
        int employeeId = Database.idOf(employeeBox);
        if (employeeId == -1) {
            Database.message("Please add an employee first.");
            return;
        }
        double net = calculate();
        if (Double.isNaN(net)) return;

        String month = monthField.getText().trim();
        try {
            // one salary record per employee per month
            Database.run("DELETE FROM salary WHERE employee_id=? AND salary_month=?", employeeId, month);
            Database.run("INSERT INTO salary (employee_id, salary_month, basic_salary, bonus, deduction, net_salary) "
                       + "VALUES (?, ?, ?, ?, ?, ?)",
                    employeeId, month,
                    Double.parseDouble(basicField.getText().trim()),
                    Double.parseDouble(bonusField.getText().trim()),
                    Double.parseDouble(deductionField.getText().trim()),
                    net);
            employeeChanged();
            Database.message("Salary saved.");
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
