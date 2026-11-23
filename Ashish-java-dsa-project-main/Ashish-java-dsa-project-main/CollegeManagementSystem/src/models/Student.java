package models;

/**
 * Student.java
 * Candidate: Ashish Kumar | ERP: RU-26-03142
 * School of Computer Science and Engineering, RISU Bhilai
 *
 * Domain model entity representing an enrolled student.
 * Implements java.lang.Comparable<Student> to establish Min-Heap ordering in PriorityQueue
 * based on attendance percentage (lowest attendance gets highest urgency at the root).
 */
public class Student implements Comparable<Student> {
    private String studentId;       // Unique Student ID (e.g., STU101)
    private String name;
    private String department;
    private int semester;
    private double cgpa;
    private double attendance;      // Attendance percentage (e.g., 54.0%)
    private double minThreshold;    // University statutory threshold: 75.0%
    private String email;
    private String phone;
    private boolean feesPaid;

    public Student(String studentId, String name, String department, int semester, 
                   double cgpa, double attendance, String email, String phone) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
        this.attendance = attendance;
        this.minThreshold = 75.0;
        this.email = email;
        this.phone = phone;
        this.feesPaid = false;
    }

    /**
     * Min-Heap priority comparison:
     * Lower attendance yields higher urgency, ensuring critically vulnerable students
     * float to the root node of PriorityQueue<Student>.
     */
    @Override
    public int compareTo(Student other) {
        return Double.compare(this.attendance, other.attendance);
    }

    public boolean isLowAttendance() {
        return this.attendance < this.minThreshold;
    }

    public String getUrgencyLevel() {
        if (this.attendance < 60.0) return "CRITICAL (<60%)";
        if (this.attendance < 75.0) return "WARNING (<75%)";
        return "NORMAL";
    }

    // Getters and Setters
    public String getStudentId()          { return studentId; }
    public String getName()               { return name; }
    public String getDepartment()         { return department; }
    public int getSemester()              { return semester; }
    public double getCgpa()               { return cgpa; }
    public double getAttendance()         { return attendance; }
    public double getMinThreshold()       { return minThreshold; }
    public String getEmail()              { return email; }
    public String getPhone()              { return phone; }
    public boolean isFeesPaid()           { return feesPaid; }

    public void setName(String name)                  { this.name = name; }
    public void setDepartment(String department)      { this.department = department; }
    public void setSemester(int semester)             { this.semester = semester; }
    public void setCgpa(double cgpa)                  { this.cgpa = cgpa; }
    public void setAttendance(double attendance)      { this.attendance = attendance; }
    public void setFeesPaid(boolean feesPaid)         { this.feesPaid = feesPaid; }

    // JSON serialization helper for HTTP REST responses
    public String toJson() {
        return String.format(
            "{\"studentId\":\"%s\",\"name\":\"%s\",\"department\":\"%s\",\"semester\":%d," +
            "\"cgpa\":%.2f,\"attendance\":%.1f,\"minThreshold\":%.1f,\"isLowAttendance\":%b," +
            "\"urgency\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"feesPaid\":%b}",
            escapeJson(studentId), escapeJson(name), escapeJson(department), semester,
            cgpa, attendance, minThreshold, isLowAttendance(),
            getUrgencyLevel(), escapeJson(email), escapeJson(phone), feesPaid
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", " ");
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Dept: %s | Sem: %d | CGPA: %.2f | Attendance: %.1f%% (%s)",
            studentId, name, department, semester, cgpa, attendance, getUrgencyLevel());
    }
}
