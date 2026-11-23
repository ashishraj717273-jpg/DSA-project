package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import models.AdmissionApplication;
import models.Student;
import services.CollegeService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * CollegeWebServer.java
 * Candidate: Ashish Kumar | ERP: RU-26-03142
 * School of Computer Science and Engineering, RISU Bhilai
 *
 * Lightweight, zero-dependency HTTP microservice powered by Java JDK's built-in HttpServer.
 * Serves static HTML5/CSS3/JavaScript frontend and exposes collegiate DSA REST endpoints:
 * - GET  / or /index.html       -> Web Dashboard (HTML5)
 * - GET  /style.css             -> Stylesheet (CSS3)
 * - GET  /script.js             -> Client Engine (JavaScript)
 * - GET  /api/stats             -> System KPI Counters
 * - GET  /api/students          -> HashMap O(1) Student Catalog
 * - POST /api/students          -> Add Student Record
 * - GET  /api/admissions        -> FIFO Admission Queue List
 * - POST /api/admissions/enqueue-> Append Application to FIFO Queue Tail
 * - POST /api/admissions/process-> Poll Head of FIFO Queue & Push LIFO Undo Stack
 * - GET  /api/alerts            -> Min-Heap Low-Attendance Alerts (O(1) Root Peek)
 * - POST /api/undo              -> Pop Top of LIFO Stack (O(1) State Rollback)
 * - POST /api/attendance        -> Update Student Attendance Percentage
 */
public class CollegeWebServer {

    private final CollegeService service;
    private int port;
    private HttpServer server;

    public CollegeWebServer(CollegeService service, int initialPort) {
        this.service = service;
        this.port = initialPort;
    }

    public int getPort() {
        return port;
    }

    /**
     * Starts the HTTP server. If initial port is busy, automatically scans the next available port.
     * @return the actual port the server bound to.
     */
    public int start() throws IOException {
        int[] portCandidates = new int[] { port, 8081, 8082, 8088, 5000, 3000, 0 };
        IOException lastEx = null;

        for (int p : portCandidates) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                this.port = server.getAddress().getPort();
                registerEndpoints();
                server.setExecutor(null);
                server.start();
                System.out.println("  [HTTP SERVICE] Online at: http://localhost:" + this.port + "/");
                return this.port;
            } catch (IOException ex) {
                lastEx = ex;
            }
        }

        if (lastEx != null) {
            throw lastEx;
        }
        return this.port;
    }

    private void registerEndpoints() {
        // ==================== STATIC FRONTEND ROUTING ====================
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "/index.html".equals(path)) {
                serveStaticFile(exchange, "index.html", "text/html; charset=UTF-8");
            } else if ("/style.css".equals(path)) {
                serveStaticFile(exchange, "style.css", "text/css; charset=UTF-8");
            } else if ("/script.js".equals(path)) {
                serveStaticFile(exchange, "script.js", "application/javascript; charset=UTF-8");
            } else {
                sendResponse(exchange, 404, "text/plain", "Not Found");
            }
        });

        // ==================== REST API ENDPOINTS ====================

        // 1. System KPIs
        server.createContext("/api/stats", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, Object> stats = service.getSystemStats();
                String json = String.format(
                    "{\"totalStudents\":%d,\"lowAttendanceAlerts\":%d,\"pendingAdmissions\":%d," +
                    "\"undoStackDepth\":%d,\"criticalStudent\":\"%s\"}",
                    stats.get("totalStudents"), stats.get("lowAttendanceAlerts"),
                    stats.get("pendingAdmissions"), stats.get("undoStackDepth"),
                    stats.get("criticalStudent")
                );
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        // 2. Students Registry (HashMap)
        server.createContext("/api/students", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Student> students = service.getAllStudents();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < students.size(); i++) {
                    sb.append(students.get(i).toJson());
                    if (i < students.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String id = extractJsonField(body, "studentId");
                String name = extractJsonField(body, "name");
                String dept = extractJsonField(body, "department");
                int sem = extractJsonInt(body, "semester", 1);
                double cgpa = extractJsonDouble(body, "cgpa", 8.0);
                double att = extractJsonDouble(body, "attendance", 85.0);

                Student s = new Student(id, name, dept, sem, cgpa, att, 
                    name.toLowerCase().replace(" ", ".") + "@rungta.ac.in", "9876543200");
                boolean ok = service.addStudent(s);
                if (ok) {
                    sendResponse(exchange, 201, "application/json", "{\"status\":\"SUCCESS\",\"message\":\"Student added.\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"status\":\"ERROR\",\"message\":\"Duplicate Student ID.\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        // 3. FIFO Admission Queue
        server.createContext("/api/admissions", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<AdmissionApplication> queue = service.getAdmissionQueue();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < queue.size(); i++) {
                    sb.append(queue.get(i).toJson());
                    if (i < queue.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        server.createContext("/api/admissions/enqueue", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                String name = extractJsonField(body, "applicantName");
                String dept = extractJsonField(body, "department");
                double score = extractJsonDouble(body, "score", 85.0);
                service.enqueueAdmission(name, dept, score);
                sendResponse(exchange, 200, "application/json", "{\"status\":\"SUCCESS\",\"message\":\"Enqueued to FIFO.\"}");
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        server.createContext("/api/admissions/process", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, Object> result = service.processNextAdmission();
                String json = String.format("{\"status\":\"%s\",\"message\":\"%s\"}",
                    result.get("status"), result.get("message"));
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        // 4. Min-Heap Attendance Alerts
        server.createContext("/api/alerts", exchange -> {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Student> alerts = service.getLowAttendanceAlerts();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < alerts.size(); i++) {
                    sb.append(alerts.get(i).toJson());
                    if (i < alerts.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, "application/json", sb.toString());
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        // 5. LIFO Stack Undo Rollback
        server.createContext("/api/undo", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, Object> result = service.undoLastAction();
                String json = String.format("{\"status\":\"%s\",\"message\":\"%s\"}",
                    result.get("status"), result.get("message"));
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });

        // 6. Update Attendance
        server.createContext("/api/attendance", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                String id = extractJsonField(body, "studentId");
                double att = extractJsonDouble(body, "attendance", 75.0);
                boolean ok = service.updateAttendance(id, att);
                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"status\":\"SUCCESS\",\"message\":\"Attendance updated.\"}");
                } else {
                    sendResponse(exchange, 404, "application/json", "{\"status\":\"ERROR\",\"message\":\"Student not found.\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void serveStaticFile(HttpExchange exchange, String filename, String contentType) throws IOException {
        Path[] searchPaths = new Path[] {
            Paths.get("frontend", filename),
            Paths.get("..", "frontend", filename),
            Paths.get("CollegeManagementSystem", "frontend", filename),
            Paths.get(filename)
        };

        for (Path p : searchPaths) {
            if (Files.exists(p)) {
                byte[] bytes = Files.readAllBytes(p);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }
        }

        sendResponse(exchange, 404, "text/plain", "Static file " + filename + " not found.");
    }

    private void sendResponse(HttpExchange exchange, int code, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String extractJsonField(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }

    private double extractJsonDouble(String json, String key, double def) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return def;
        start += pattern.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return def;
        }
    }

    private int extractJsonInt(String json, String key, int def) {
        return (int) extractJsonDouble(json, key, def);
    }
}
