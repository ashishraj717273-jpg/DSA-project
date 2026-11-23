package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AdmissionApplication Model Class
 * Represents an admission application payload buffered in the FIFO Queue.
 * Processed strictly in First-In, First-Out sequence without starvation.
 */
public class AdmissionApplication {
    private String applicationId;    // e.g., "ADM-1001"
    private String applicantName;
    private String department;
    private double qualifyingScore;  // e.g., 92.4%
    private String status;           // "QUEUED", "APPROVED", "REJECTED"
    private String timestamp;

    public AdmissionApplication(String applicationId, String applicantName, 
                                String department, double qualifyingScore) {
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.department = department;
        this.qualifyingScore = qualifyingScore;
        this.status = "QUEUED";
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String getApplicationId()       { return applicationId; }
    public String getApplicantName()       { return applicantName; }
    public String getDepartment()          { return department; }
    public double getQualifyingScore()     { return qualifyingScore; }
    public String getStatus()              { return status; }
    public String getTimestamp()           { return timestamp; }

    public void setStatus(String status)   { this.status = status; }

    public String toJson() {
        return String.format(
            "{\"applicationId\":\"%s\",\"applicantName\":\"%s\",\"department\":\"%s\"," +
            "\"qualifyingScore\":%.1f,\"status\":\"%s\",\"timestamp\":\"%s\"}",
            applicationId, applicantName.replace("\"", "\\\""), department, qualifyingScore, status, timestamp
        );
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s (Score: %.1f%%) [%s]",
            applicationId, applicantName, department, qualifyingScore, status);
    }
}
