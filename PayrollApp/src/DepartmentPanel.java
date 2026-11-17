// DepartmentPanel.java - Add, View, Update, Delete departments.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DepartmentPanel extends JPanel {

    JTextField nameField = new JTextField(15);
    DefaultTableModel model = Database.newModel("ID", "Department Name");
    JTable table = new JTable(model);

    public DepartmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Department Management");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // form at the bottom
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        form.add(new JLabel("Department name:"));
        form.add(nameField);
        form.add(addButton);
        form.add(updateButton);
        form.add(deleteButton);
        add(form, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addDepartment());
        updateButton.addActionListener(e -> updateDepartment());
        deleteButton.addActionListener(e -> deleteDepartment());

        // when a row is clicked, show its name in the text box
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) nameField.setText(model.getValueAt(row, 1).toString());
        });
    }

    public void refresh() {
        try {
            Database.fill(model, "SELECT id, department_name FROM departments");
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void addDepartment() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Database.message("Please type a department name.");
            return;
        }
        try {
            Database.run("INSERT INTO departments (department_name) VALUES (?)", name);
            nameField.setText("");
            refresh();
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void updateDepartment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Database.message("Please select a department from the table.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            Database.run("UPDATE departments SET department_name=? WHERE id=?", nameField.getText().trim(), id);
            refresh();
        } catch (Exception e) {
            Database.error(e);
        }
    }

    void deleteDepartment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Database.message("Please select a department from the table.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        try {
            // only delete when no employee is using this department
            int used = Database.count("SELECT COUNT(*) FROM employees WHERE department_id=?", id);
            if (used > 0) {
                Database.message("This department is in use by " + used + " employee(s). It cannot be deleted.");
                return;
            }
            Database.run("DELETE FROM departments WHERE id=?", id);
            nameField.setText("");
            refresh();
        } catch (Exception e) {
            Database.error(e);
        }
    }
}
