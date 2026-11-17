// EmployeePanel.java - Add, View, Update, Delete and Search employees.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EmployeePanel extends JPanel {

    JTextField nameField = new JTextField();
    JTextField phoneField = new JTextField();
    JTextField emailField = new JTextField();
    JComboBox<String> departmentBox = new JComboBox<>();
    JTextField salaryField = new JTextField();
    JTextField searchField = new JTextField(15);

    DefaultTableModel model = Database.newModel("ID", "Name", "Phone", "Email", "Department", "Basic Salary");
    JTable table = new JTable(model);

    // This query is used to show employees (with their department name)
    String SELECT_EMPLOYEES =
            "SELECT e.id, e.name, e.phone, e.email, IFNULL(d.department_name, ''), e.basic_salary "
          + "FROM employees e LEFT JOIN departments d ON e.department_id = d.id ";

    public EmployeePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Employee Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // ----- Left side: the form -----
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 8));
        form.add(new JLabel("Name:"));         form.add(nameField);
        form.add(new JLabel("Phone:"));        form.add(phoneField);
        form.add(new JLabel("Email:"));        form.add(emailField);
        form.add(new JLabel("Department:"));   form.add(departmentBox);
        form.add(new JLabel("Basic Salary:")); form.add(salaryField);

        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton clearButton = new JButton("Clear");
        JPanel buttons = new JPanel(new GridLayout(2, 2, 5, 5));
        buttons.add(addButton);    buttons.add(updateButton);
        buttons.add(deleteButton); buttons.add(clearButton);

        JPanel left = new JPanel(new BorderLayout(5, 15));
        left.setPreferredSize(new Dimension(300, 0));
        left.add(form, BorderLayout.NORTH);
        JPanel buttonHolder = new JPanel(new BorderLayout());
        buttonHolder.add(buttons, BorderLayout.NORTH);
        left.add(buttonHolder, BorderLayout.CENTER);
        add(left, BorderLayout.WEST);

        // ----- Right side: search + table -----
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        searchBar.add(new JLabel("Search by ID or name:"));
        searchBar.add(searchField);
        searchBar.add(searchButton);
        searchBar.add(showAllButton);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.add(searchBar, BorderLayout.NORTH);
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        // ----- What each button does -----
        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> search());
        showAllButton.addActionListener(e -> { searchField.setText(""); search(); });

        // when a row is clicked, copy it into the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedRow();
        });
    }

    // Reload the department list and the employee table
    public void refresh() {
        Database.fillCombo(departmentBox, "SELECT id, department_name FROM departments");
        search();
    }

    void search() {
        String text = searchField.getText().trim();
        try {
            Database.fill(model, SELECT_EMPLOYEES + "WHERE e.name LIKE ? OR CAST(e.id AS TEXT) = ? ORDER BY e.id",
                    "%" + text + "%", text);
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void showSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        nameField.setText(String.valueOf(model.getValueAt(row, 1)));
        phoneField.setText(String.valueOf(model.getValueAt(row, 2)));
        emailField.setText(String.valueOf(model.getValueAt(row, 3)));
        salaryField.setText(String.valueOf(model.getValueAt(row, 5)));

        // select the matching department in the drop-down
        String department = String.valueOf(model.getValueAt(row, 4));
        for (int i = 0; i < departmentBox.getItemCount(); i++) {
            if (departmentBox.getItemAt(i).endsWith(" - " + department)) {
                departmentBox.setSelectedIndex(i);
            }
        }
    }

    void clearForm() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        salaryField.setText("");
        table.clearSelection();
    }

    void addEmployee() {
        if (nameField.getText().trim().isEmpty()) {
            Database.message("Please type the employee name.");
            return;
        }
        try {
            double salary = salaryField.getText().trim().isEmpty() ? 0 : Double.parseDouble(salaryField.getText().trim());
            Object departmentId = Database.idOf(departmentBox) == -1 ? null : Database.idOf(departmentBox);
            Database.run("INSERT INTO employees (name, phone, email, department_id, basic_salary) VALUES (?, ?, ?, ?, ?)",
                    nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(),
                    departmentId, salary);
            clearForm();
            search();
        } catch (NumberFormatException e) {
            Database.message("Salary must be a number.");
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void updateEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Database.message("Please select an employee from the table.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            double salary = Double.parseDouble(salaryField.getText().trim());
            Object departmentId = Database.idOf(departmentBox) == -1 ? null : Database.idOf(departmentBox);
            Database.run("UPDATE employees SET name=?, phone=?, email=?, department_id=?, basic_salary=? WHERE id=?",
                    nameField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(),
                    departmentId, salary, id);
            search();
        } catch (NumberFormatException e) {
            Database.message("Salary must be a number.");
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void deleteEmployee() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Database.message("Please select an employee from the table.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete this employee and all their attendance and salary records?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        int id = (int) model.getValueAt(row, 0);
        try {
            Database.run("DELETE FROM attendance WHERE employee_id=?", id);
            Database.run("DELETE FROM salary WHERE employee_id=?", id);
            Database.run("DELETE FROM employees WHERE id=?", id);
            clearForm();
            search();
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
