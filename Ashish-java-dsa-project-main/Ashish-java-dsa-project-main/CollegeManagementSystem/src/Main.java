import services.CollegeService;
import server.CollegeWebServer;
import models.Student;
import models.AdmissionApplication;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Scanner;

/**
 * ============================================================================
 *   PROJECT INTERIM SUBMISSION: DATA STRUCTURES & ALGORITHMS (DSA)
 *   Course Code: RU-200-01-00029 | Session: 2026-27
 * ----------------------------------------------------------------------------
 *   Project Title: Smart College Management & Student Administration System
 *   Submitted By : Ashish Kumar
 *   ERP ID       : RU-26-03142
 *   Branch       : B.Tech Computer Science & Engineering (AI & ML)
 *   Institution  : School of Computer Science and Engineering
 *                  Rungta International Skills University (RISU), Bhilai
 *   Under Guidance: Mr. Surya Mani (IBM SME & Expert / Faculty Mentor)
 * ============================================================================
 *
 * Algorithmic Design & Core Data Structures Applied:
 * 1. HashMap<String, Student>             - Constant-time O(1) Student lookup & updates
 * 2. PriorityQueue<Student> (Min-Heap)     - Automated O(1) root peek for critical low attendance (<75%)
 * 3. Queue<AdmissionApplication> (FIFO)    - Sequential, fair admission processing without starvation
 * 4. Stack<AdminActionTransaction> (LIFO)  - Audit trail & 1-click Undo rollback
 *
 * Architecture Overview:
 * - Embedded Java HTTP Microservice (auto-scans open port starting at 8080)
 * - Semantic HTML5, Responsive CSS3 & Vanilla JavaScript Dashboard
 * - Interactive Terminal CLI Fallback for offline viva demonstration
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;
    private static int activePort = DEFAULT_PORT;

    public static void main(String[] args) {
        CollegeService service = new CollegeService();
        CollegeWebServer server = new CollegeWebServer(service, DEFAULT_PORT);

        boolean webStarted = false;
        try {
            activePort = server.start();
            webStarted = true;
            printBanner(activePort);
            launchBrowser("http://localhost:" + activePort + "/");
        } catch (Exception e) {
            System.out.println("  [NOTICE] Web port unavailable: " + e.getMessage());
            System.out.println("  Running in standalone interactive Console Demonstration mode.");
        }

        try {
            runConsoleInterface(service);
        } finally {
            if (webStarted) {
                server.stop();
            }
            System.out.println("\n  College Management System service stopped. Thank you.");
        }
    }

    private static void printBanner(int port) {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║       SMART COLLEGE MANAGEMENT & STUDENT ADMINISTRATION SYSTEM             ║");
        System.out.println("  ║       Data Structures and Algorithms (DSA) Practical Project               ║");
        System.out.println("  ║       Rungta International Skills University (RISU), Bhilai                ║");
        System.out.println("  ╠════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("  ║   Candidate: Ashish Kumar (ERP: RU-26-03142) | Branch: CSE (AI & ML)       ║");
        System.out.println("  ║   Mentor   : Mr. Surya Mani (IBM SME & Expert / Faculty Mentor)            ║");
        System.out.println("  ╠════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf ("  ║   >>> Web Dashboard Live: http://localhost:%-4d/                           ║%n", port);
        System.out.println("  ║   Dashboard is opening in your default web browser...                      ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void launchBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // Modern ProcessBuilder replacing deprecated Runtime.exec
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            }
        } catch (Exception ex) {
            System.out.println("  [TIP] Please open your browser and navigate to: " + url);
        }
    }

    private static void runConsoleInterface(CollegeService service) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n  --- VIVA CONSOLE CONTROLS (Dashboard is live at http://localhost:" + activePort + "/) ---");
            System.out.println("  1. List All Students (HashMap O(1) Keyed Index)");
            System.out.println("  2. Search Student by ID (O(1) HashMap Lookup)");
            System.out.println("  3. View Low-Attendance Alerts (Min-Heap Priority Queue < 75%)");
            System.out.println("  4. View Admission Queue (FIFO Order Buffer)");
            System.out.println("  5. Process Next Admission (FIFO Dequeue -> HashMap)");
            System.out.println("  6. Undo Last Action (LIFO Stack Rollback)");
            System.out.println("  7. Re-open Web Dashboard in Browser");
            System.out.println("  0. Exit System");
            System.out.print("  Enter choice [0-7]: ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (Exception e) {
                choice = -1;
            }

            switch (choice) {
                case 1:
                    System.out.println("\n  === ENROLLED STUDENTS (HashMap Storage) ===");
                    for (Student s : service.getAllStudents()) {
                        System.out.println("  " + s);
                    }
                    break;
                case 2:
                    System.out.print("  Enter Student ID (e.g., STU101): ");
                    String id = scanner.nextLine().trim().toUpperCase();
                    Student found = service.getStudent(id);
                    if (found != null) {
                        System.out.println("  [FOUND O(1)] " + found);
                    } else {
                        System.out.println("  [NOT FOUND] Student with ID " + id + " does not exist.");
                    }
                    break;
                case 3:
                    System.out.println("\n  === MIN-HEAP ATTENDANCE ALERTS (< 75%) ===");
                    List<Student> alerts = service.getLowAttendanceAlerts();
                    if (alerts.isEmpty()) {
                        System.out.println("  [OK] All enrolled students maintain attendance >= 75%.");
                    } else {
                        int rank = 1;
                        for (Student s : alerts) {
                            System.out.printf("  #%d [%s] %s (%s) | Attendance: %.1f%%%n",
                                rank++, (rank == 2 ? "ROOT NODE" : "HEAP LEVEL"), s.getName(), s.getStudentId(), s.getAttendance());
                        }
                    }
                    break;
                case 4:
                    System.out.println("\n  === FIFO ADMISSION QUEUE ===");
                    List<AdmissionApplication> q = service.getAdmissionQueue();
                    if (q.isEmpty()) {
                        System.out.println("  Admission Queue is currently empty.");
                    } else {
                        int pos = 1;
                        for (AdmissionApplication a : q) {
                            System.out.printf("  #%d [%s] %s (%s) | Score: %.1f%%%n",
                                pos++, (pos == 2 ? "HEAD (NEXT)" : "QUEUED"), a.getApplicantName(), a.getDepartment(), a.getQualifyingScore());
                        }
                    }
                    break;
                case 5:
                    var res = service.processNextAdmission();
                    System.out.println("  " + res.get("message"));
                    break;
                case 6:
                    var undoRes = service.undoLastAction();
                    System.out.println("  " + undoRes.get("message"));
                    break;
                case 7:
                    launchBrowser("http://localhost:" + activePort + "/");
                    System.out.println("  [OK] Launched http://localhost:" + activePort + "/ in browser.");
                    break;
                case 0:
                    System.out.println("  Terminating server session...");
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please enter a number between 0 and 7.");
            }

        } while (choice != 0);
    }
}
