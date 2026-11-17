EMPLOYEE ATTENDANCE & PAYROLL SYSTEM  (Java Swing + SQLite)

HOW TO RUN
1. Install a JDK (version 11 or newer).
2. Windows: double-click run.bat      Mac/Linux: ./run.sh
   (the database file payroll.db is created automatically in the same folder)

HOW TO OPEN IN AN IDE (IntelliJ / NetBeans / Eclipse)
1. Create a new Java project and copy the 9 files from the "src" folder into it.
2. Add lib/sqlite-jdbc.jar to the project libraries.
3. Run Main.java

FILES
Main.java            starts the program
Database.java        all database code (connect, create tables, small helpers)
MainWindow.java      window + sidebar
DashboardPanel.java  Dashboard
EmployeePanel.java   Module 1  Employees
DepartmentPanel.java Module 3  Departments
AttendancePanel.java Module 2  Attendance
SalaryPanel.java     Module 4  Salary  (Net = Basic + Bonus - Deduction)
ReportPanel.java     Module 5  Reports

TIP: add Departments first, then Employees, then Attendance and Salary.
