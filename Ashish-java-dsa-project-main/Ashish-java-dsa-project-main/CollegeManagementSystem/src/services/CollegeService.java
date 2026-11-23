package services;

import models.Student;
import models.AdmissionApplication;
import models.AdminActionTransaction;

import java.util.*;

/**
 * CollegeService.java
 * Candidate: Ashish Kumar | ERP: RU-26-03142
 * School of Computer Science and Engineering, RISU Bhilai
 *
 * Centralized service layer managing the foundational DSA structures:
 * 1. studentMap (HashMap<String, Student>): Constant-time O(1) Student lookup, add, edit.
 * 2. lowAttendanceHeap (PriorityQueue<Student>): Min-Heap surfacing students nearest attendance depletion in O(1) root peek.
 * 3. admissionQueue (Queue<AdmissionApplication>): FIFO Queue ensuring non-blocking, fair sequential admission processing.
 * 4. undoStack (Stack<AdminActionTransaction>): LIFO Stack enabling 1-click Undo of accidental administrative changes.
 */
public class CollegeService {

    // 1. Hash Table for constant-time O(1) lookups
    private final Map<String, Student> studentMap;

    // 2. Min-Heap Priority Queue for O(1) root peek of lowest attendance
    private final PriorityQueue<Student> lowAttendanceHeap;

    // 3. FIFO Queue for sequential admission processing
    private final Queue<AdmissionApplication> admissionQueue;

    // 4. LIFO Stack for 1-click Undo rollback
    private final Stack<AdminActionTransaction> undoStack;

    private int nextStudentNum = 111;
    private int nextAppNum = 1004;
    private int nextTxnNum = 501;

    public CollegeService() {
        this.studentMap = new HashMap<>();
        this.lowAttendanceHeap = new PriorityQueue<>();
        this.admissionQueue = new LinkedList<>();
        this.undoStack = new Stack<>();

        loadPredefinedCatalog();
    }

    /**
     * Initializes the predefined baseline catalog of 10 students across engineering disciplines.
     */
    private void loadPredefinedCatalog() {
        addStudentInternal(new Student("STU101", "Aarav Sharma",    "Computer Science",       4, 8.85, 54.0, "aarav@rungta.ac.in",    "9876543201"));
        addStudentInternal(new Student("STU102", "Diya Patel",      "Information Technology", 4, 9.15, 88.0, "diya@rungta.ac.in",     "9876543202"));
        addStudentInternal(new Student("STU103", "Rohan Verma",     "Computer Science (AIML)",2, 7.60, 58.5, "rohan@rungta.ac.in",    "9876543203"));
        addStudentInternal(new Student("STU104", "Ananya Mishra",   "Electronics & Comm",     6, 8.40, 72.0, "ananya@rungta.ac.in",   "9876543204"));
        addStudentInternal(new Student("STU105", "Kabir Singh",     "Mechanical Engineering", 4, 6.95, 62.0, "kabir@rungta.ac.in",    "9876543205"));
        addStudentInternal(new Student("STU106", "Sneha Roy",       "Computer Science",       6, 9.40, 93.0, "sneha@rungta.ac.in",    "9876543206"));
        addStudentInternal(new Student("STU107", "Aditya Nair",     "Civil Engineering",      2, 7.20, 69.5, "aditya@rungta.ac.in",   "9876543207"));
        addStudentInternal(new Student("STU108", "Ishita Gupta",    "Computer Science (AIML)",4, 8.70, 74.0, "ishita@rungta.ac.in",   "9876543208"));
        addStudentInternal(new Student("STU109", "Varun Chopra",    "Electrical Engineering", 6, 7.85, 82.5, "varun@rungta.ac.in",    "9876543209"));
        addStudentInternal(new Student("STU110", "Meera Joshi",     "Information Technology", 2, 8.90, 91.0, "meera@rungta.ac.in",    "9876543210"));

        // Preload FIFO Admission Queue
        admissionQueue.offer(new AdmissionApplication("ADM-1001", "Vikram Rathore",  "Computer Science",       92.4));
        admissionQueue.offer(new AdmissionApplication("ADM-1002", "Pooja Hegde",      "Computer Science (AIML)",88.0));
        admissionQueue.offer(new AdmissionApplication("ADM-1003", "Sameer Deshmukh",  "Information Technology", 85.5));
    }

    private void addStudentInternal(Student s) {
        studentMap.put(s.getStudentId(), s);
        lowAttendanceHeap.offer(s);
    }

    // ==================== HASHMAP OPERATIONS O(1) ====================

    public synchronized Student getStudent(String studentId) {
        return studentMap.get(studentId);
    }

    public synchronized List<Student> getAllStudents() {
        return new ArrayList<>(studentMap.values());
    }

    public synchronized boolean addStudent(Student student) {
        if (studentMap.containsKey(student.getStudentId())) {
            return false; // Prevent duplicate keys
        }
        studentMap.put(student.getStudentId(), student);
        rebuildHeap();

        // Push to LIFO Stack for rollback
        undoStack.push(new AdminActionTransaction(
            "TXN-" + (nextTxnNum++), "ADD_STUDENT", student.getStudentId(),
            null, student, "Added new student " + student.getName()
        ));
        return true;
    }

    public synchronized boolean updateAttendance(String studentId, double newAttendance) {
        Student s = studentMap.get(studentId);
        if (s == null) return false;

        double oldAttendance = s.getAttendance();
        Student beforeSnapshot = cloneStudent(s);

        s.setAttendance(newAttendance);
        rebuildHeap();

        undoStack.push(new AdminActionTransaction(
            "TXN-" + (nextTxnNum++), "UPDATE_ATTENDANCE", studentId,
            beforeSnapshot, cloneStudent(s),
            String.format("Updated %s attendance: %.1f%% -> %.1f%%", s.getName(), oldAttendance, newAttendance)
        ));
        return true;
    }

    // ==================== MIN-HEAP OPERATIONS O(1) peek / O(log N) ====================

    private synchronized void rebuildHeap() {
        lowAttendanceHeap.clear();
        lowAttendanceHeap.addAll(studentMap.values());
    }

    /**
     * Returns students requiring attendance intervention, ordered strictly ascending by attendance
     * (lowest attendance first) using the Min-Heap.
     */
    public synchronized List<Student> getLowAttendanceAlerts() {
        List<Student> alerts = new ArrayList<>();
        PriorityQueue<Student> copy = new PriorityQueue<>(lowAttendanceHeap);
        while (!copy.isEmpty()) {
            Student s = copy.poll();
            if (s.isLowAttendance()) {
                alerts.add(s);
            }
        }
        return alerts;
    }

    public synchronized Student peekMostCriticalStudent() {
        return lowAttendanceHeap.peek();
    }

    // ==================== FIFO QUEUE OPERATIONS O(1) ====================

    public synchronized List<AdmissionApplication> getAdmissionQueue() {
        return new ArrayList<>(admissionQueue);
    }

    public synchronized void enqueueAdmission(String applicantName, String dept, double score) {
        AdmissionApplication app = new AdmissionApplication(
            "ADM-" + (nextAppNum++), applicantName, dept, score
        );
        admissionQueue.offer(app);
    }

    /**
     * Dispatches the next admission application at the head of the FIFO queue.
     * Enrolls the applicant into the student HashMap and logs transaction to LIFO stack.
     */
    public synchronized Map<String, Object> processNextAdmission() {
        Map<String, Object> result = new HashMap<>();
        if (admissionQueue.isEmpty()) {
            result.put("status", "EMPTY");
            result.put("message", "Admission FIFO Queue is currently empty.");
            return result;
        }

        AdmissionApplication app = admissionQueue.poll();
        app.setStatus("APPROVED");

        String newId = "STU" + (nextStudentNum++);
        Student newStudent = new Student(
            newId, app.getApplicantName(), app.getDepartment(), 1,
            Math.min(10.0, (app.getQualifyingScore() / 10.0)), 100.0,
            app.getApplicantName().toLowerCase().replace(" ", ".") + "@rungta.ac.in",
            "987654" + (1000 + nextStudentNum)
        );

        studentMap.put(newId, newStudent);
        rebuildHeap();

        // Push to LIFO Stack
        AdminActionTransaction txn = new AdminActionTransaction(
            "TXN-" + (nextTxnNum++), "APPROVE_ADMISSION", newId,
            null, newStudent, "Processed Admission for " + app.getApplicantName() + " (" + newId + ")"
        );
        undoStack.push(txn);

        result.put("status", "SUCCESS");
        result.put("message", "Application " + app.getApplicationId() + " processed. Enrolled as " + newId + ".");
        result.put("student", newStudent);
        result.put("application", app);
        return result;
    }

    // ==================== LIFO STACK UNDO OPERATIONS O(1) ====================

    public synchronized List<AdminActionTransaction> getUndoHistory() {
        List<AdminActionTransaction> list = new ArrayList<>(undoStack);
        Collections.reverse(list); // Top of stack first
        return list;
    }

    /**
     * Reverts the most recent administrative operation at the top of the LIFO stack.
     */
    public synchronized Map<String, Object> undoLastAction() {
        Map<String, Object> result = new HashMap<>();
        if (undoStack.isEmpty()) {
            result.put("status", "EMPTY");
            result.put("message", "Undo Stack is empty. No operations to revert.");
            return result;
        }

        AdminActionTransaction txn = undoStack.pop();
        String type = txn.getActionType();
        String studentId = txn.getStudentId();

        if ("ADD_STUDENT".equals(type) || "APPROVE_ADMISSION".equals(type)) {
            studentMap.remove(studentId);
            rebuildHeap();
            result.put("status", "SUCCESS");
            result.put("message", "Undo Successful: Reverted enrollment of " + studentId + ".");
        } else if ("UPDATE_ATTENDANCE".equals(type)) {
            Student before = txn.getSnapshotBefore();
            if (before != null && studentMap.containsKey(studentId)) {
                studentMap.put(studentId, before);
                rebuildHeap();
            }
            result.put("status", "SUCCESS");
            result.put("message", "Undo Successful: Restored " + studentId + " attendance to " + before.getAttendance() + "%.");
        } else {
            result.put("status", "SUCCESS");
            result.put("message", "Reverted action: " + txn.getDescription());
        }

        result.put("revertedTransaction", txn);
        return result;
    }

    // ==================== SYSTEM STATS ====================

    public synchronized Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentMap.size());
        stats.put("lowAttendanceAlerts", getLowAttendanceAlerts().size());
        stats.put("pendingAdmissions", admissionQueue.size());
        stats.put("undoStackDepth", undoStack.size());
        stats.put("criticalStudent", peekMostCriticalStudent() != null ? peekMostCriticalStudent().getName() + " (" + peekMostCriticalStudent().getAttendance() + "%)" : "None");
        return stats;
    }

    private Student cloneStudent(Student s) {
        Student c = new Student(
            s.getStudentId(), s.getName(), s.getDepartment(), s.getSemester(),
            s.getCgpa(), s.getAttendance(), s.getEmail(), s.getPhone()
        );
        c.setFeesPaid(s.isFeesPaid());
        return c;
    }
}
