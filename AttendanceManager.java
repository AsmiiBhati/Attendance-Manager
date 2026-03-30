import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class AttendanceManager extends JFrame {
    private HashMap<String, Student> students = new HashMap<>();
    private JComboBox<String> rollBox;
    private JTextArea reportArea;
    private static final String DATA_FILE = "students.txt";

    // Student Inner Class.
    private class Student {
        private String rollNo;
        private String name;
        private int total;
        private int attended;

        public Student(String rollNo, String name) {
            this.rollNo = rollNo;
            this.name = name;
            this.total = 0;
            this.attended = 0;
        }

        public void markPresent() { attended++; total++; }
        public void markAbsent() { total++; }
        public double getPercentage() { return total == 0 ? 0 : (attended * 100.0 / total); }

        @Override
        public String toString() {
            return rollNo + "," + name + "," + attended + "," + total;
        }

        public String getRollNo() { return rollNo; }
        public String getName() { return name; }
        public int getAttended() { return attended; }
        public int getTotal() { return total; }
    }

    public Project() {
    setTitle("Smart Attendance Pro™");
    setSize(720, 520);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(12, 12));

    
    createUI();

    loadStudents();

    updateRollBox();

    updateReport();
    setVisible(true);
}

 private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return btn;
    }

    private void markAttendance(boolean present) {
        String roll = (String) rollBox.getSelectedItem();
        if (roll == null) return;
        Student s = students.get(roll);
        if (present) s.markPresent();
        else s.markAbsent();
        updateReport();
        saveStudents();
    }

    private void updateReport() {
        List<Student> list = new ArrayList<>(students.values());
        list.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));

        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                    SMART ATTENDANCE WITH LIVE REPORT\n");
        sb.append("════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("%-10s %-20s %8s %7s\n", "Roll", "Name", "Attended", "%"));
        sb.append("----------------------------------------------------------------------------\n");

        for (Student s : list) {
            String status = s.getPercentage() >= 75 ? "EXCELLENT" :
                           s.getPercentage() >= 60 ? "GOOD" : "NEEDS IMPROVEMENT";
            String name = truncate(s.getName(), 20);
            sb.append(String.format("%-10s %-20s %3d/%-3d  %6.2f%%  [%s]\n",
                s.getRollNo(), name, s.getAttended(), s.getTotal(),
                s.getPercentage(), status));
        }
        sb.append("════════════════════════════════════════════════════════════════════════════\n");
        sb.append("Total Students: ").append(students.size()).append("\n");
        reportArea.setText(sb.toString());
    }

    private String truncate(String str, int len) {
        return str.length() > len ? str.substring(0, len - 3) + "..." : str;
    }

    private void createUI() {
    // The top panel has controls.
    JPanel top = new JPanel();
    top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    top.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

    top.add(new JLabel("Roll No:"));
    rollBox = new JComboBox<>();  // NOW rollBox is created
    rollBox.setPreferredSize(new Dimension(120, 28));
    top.add(rollBox);

    JButton present = createButton("Present", new Color(0, 160, 0), Color.WHITE);
    JButton absent = createButton("Absent", new Color(180, 0, 0), Color.WHITE);
    JButton addStudent = createButton("Add Student", new Color(0, 100, 200), Color.WHITE);
    JButton reset = createButton("Reset All", Color.ORANGE, Color.BLACK);
    JButton export = createButton("Export", new Color(100, 100, 100), Color.WHITE);

    top.add(present);
    top.add(absent);
    top.add(Box.createHorizontalStrut(20));
    top.add(addStudent);
    top.add(reset);
    top.add(export);

    add(top, BorderLayout.NORTH);

    // This creates the report
    reportArea = new JTextArea();
    reportArea.setFont(new Font("Consolas", Font.PLAIN, 14));
    reportArea.setEditable(false);
    reportArea.setMargin(new Insets(12, 12, 12, 12));
    JScrollPane scroll = new JScrollPane(reportArea);
    scroll.setBorder(BorderFactory.createTitledBorder("Live Attendance Report"));
    add(scroll, BorderLayout.CENTER);

    present.addActionListener(e -> markAttendance(true));
    absent.addActionListener(e -> markAttendance(false));
    addStudent.addActionListener(e -> showAddStudentDialog());
    reset.addActionListener(e -> resetAllAttendance());
    export.addActionListener(e -> exportReport());

    // Auto-saves on exit
    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            saveStudents();
        }
    });
}

    private void updateRollBox() {
        rollBox.removeAllItems();
        List<String> rolls = new ArrayList<>(students.keySet());
        Collections.sort(rolls);
        for (String roll : rolls) {
            rollBox.addItem(roll);
        }
        if (rollBox.getItemCount() > 0) {
            rollBox.setSelectedIndex(0);
        }
    }

    private void showAddStudentDialog() {
        JTextField rollField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Roll No:"));
        panel.add(rollField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String roll = rollField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();

            if (roll.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (students.containsKey(roll)) {
                JOptionPane.showMessageDialog(this, "Student with this Roll No already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            students.put(roll, new Student(roll, name));
            updateRollBox();
            updateReport();
            saveStudents();
            JOptionPane.showMessageDialog(this, "Student added successfully!");
        }
    }

    private void resetAllAttendance() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will reset attendance for ALL students to 0.\nAre you sure?",
                "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            for (Student s : students.values()) {
                s.attended = 0;
                s.total = 0;
            }
            updateReport();
            saveStudents();
            JOptionPane.showMessageDialog(this, "All attendance reset!");
        }
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
        chooser.setSelectedFile(new File("Attendance_Report_" + timestamp + ".txt"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getParent(), file.getName() + ".txt");
            }
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.print(reportArea.getText());
                JOptionPane.showMessageDialog(this, "Report exported to:\n" + file.getAbsolutePath(),
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadStudents() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            // First run: Add sample students
            students.put("AIDS032", new Student("AIDS032", "Asmii Bhati"));
            students.put("AIDS033", new Student("AIDS033", "Astha Pathak"));
            students.put("AIDS039", new Student("AIDS039", "Bhavya Paliwal"));
            saveStudents();
        } else {
            try (Scanner sc = new Scanner(file)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine().trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split(",", -1);
                    if (parts.length >= 4) {
                        String roll = parts[0].trim();
                        String name = parts[1].trim();
                        int attended = safeParse(parts[2].trim());
                        int total = safeParse(parts[3].trim());
                        Student s = new Student(roll, name);
                        s.attended = attended;
                        s.total = total;
                        students.put(roll, s);
                    }
                }
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Data file not found! Creating new.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private int safeParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void saveStudents() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Student s : students.values()) {
                pw.println(s.toString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save data!\n" + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Fixed: Correct method
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Ignore - fallback to default
            }
            new Project();
        });
    }
}
