// Database.java - all database work is in this one file.
// Other files just call these small helper methods.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class Database {

    // The database is one file named payroll.db (created automatically)
    private static final String URL = "jdbc:sqlite:payroll.db";

    // Open a connection to the database
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Create the 4 tables (only if they do not exist yet)
    public static void createTables() throws SQLException {
        try (Connection con = connect(); Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS departments ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "department_name TEXT NOT NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS employees ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, phone TEXT, email TEXT, "
                    + "department_id INTEGER, basic_salary REAL)");

            st.execute("CREATE TABLE IF NOT EXISTS attendance ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "employee_id INTEGER, attendance_date TEXT, status TEXT)");

            st.execute("CREATE TABLE IF NOT EXISTS salary ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "employee_id INTEGER, salary_month TEXT, basic_salary REAL, "
                    + "bonus REAL, deduction REAL, net_salary REAL)");
        }
    }

    // Put the values (?, ?, ?) into a prepared statement
    private static void setValues(PreparedStatement ps, Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + 1, values[i]);
        }
    }

    // Run INSERT / UPDATE / DELETE
    public static void run(String sql, Object... values) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, values);
            ps.executeUpdate();
        }
    }

    // Run a "SELECT COUNT(*)" style query and return the number
    public static int count(String sql, Object... values) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, values);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Run a SELECT and put all rows into a table model
    public static void fill(DefaultTableModel model, String sql, Object... values) throws SQLException {
        model.setRowCount(0);                       // clear old rows
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setValues(ps, values);
            ResultSet rs = ps.executeQuery();
            int columns = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columns];
                for (int i = 0; i < columns; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
        }
    }

    // Make an empty table model that the user cannot edit by typing in the table
    public static DefaultTableModel newModel(String... columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    // Fill a drop-down list with items like "3 - Rahim"
    // The query must return 2 columns: id, name
    public static void fillCombo(JComboBox<String> box, String sql) {
        box.removeAllItems();
        try (Connection con = connect(); Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                box.addItem(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException e) {
            error(e);
        }
    }

    // Get the id number from a drop-down item like "3 - Rahim"  (returns -1 if nothing selected)
    public static int idOf(JComboBox<String> box) {
        String text = (String) box.getSelectedItem();
        if (text == null) return -1;
        return Integer.parseInt(text.split(" - ")[0]);
    }

    // Show an error message box
    public static void error(Exception e) {
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Show a normal message box
    public static void message(String text) {
        JOptionPane.showMessageDialog(null, text);
    }
}
