// Main.java - the program starts here.
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Database.createTables();              // make the database file and tables
        } catch (Exception e) {
            Database.error(e);
            return;
        }
        // open the window
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
