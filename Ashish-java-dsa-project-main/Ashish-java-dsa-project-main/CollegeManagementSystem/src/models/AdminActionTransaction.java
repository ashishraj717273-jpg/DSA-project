package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AdminActionTransaction Model Class
 * Captures historical administrative changes pushed to the LIFO Undo Stack.
 * Enables 1-click Undo rollback of accidental operations.
 */
public class AdminActionTransaction {
    private String transactionId;   // e.g., "TXN-501"
    private String actionType;      // "ADD_STUDENT", "APPROVE_ADMISSION", "UPDATE_ATTENDANCE"
    private String studentId;
    private Student snapshotBefore; // State before modification
    private Student snapshotAfter;  // State after modification
    private String description;
    private String timestamp;

    public AdminActionTransaction(String transactionId, String actionType, String studentId,
                                  Student snapshotBefore, Student snapshotAfter, String description) {
        this.transactionId = transactionId;
        this.actionType = actionType;
        this.studentId = studentId;
        this.snapshotBefore = snapshotBefore;
        this.snapshotAfter = snapshotAfter;
        this.description = description;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getTransactionId()   { return transactionId; }
    public String getActionType()      { return actionType; }
    public String getStudentId()       { return studentId; }
    public Student getSnapshotBefore() { return snapshotBefore; }
    public Student getSnapshotAfter()  { return snapshotAfter; }
    public String getDescription()     { return description; }
    public String getTimestamp()       { return timestamp; }

    public String toJson() {
        return String.format(
            "{\"transactionId\":\"%s\",\"actionType\":\"%s\",\"studentId\":\"%s\"," +
            "\"description\":\"%s\",\"timestamp\":\"%s\"}",
            transactionId, actionType, studentId, description.replace("\"", "'"), timestamp
        );
    }

    @Override
    public String toString() {
        return String.format("[%s @ %s] %s: %s", transactionId, timestamp, actionType, description);
    }
}
