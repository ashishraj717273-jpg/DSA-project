# Smart College Management & Student Administration System
### Applied Data Structures and Algorithms (DSA) Practical Project
**Academic Session:** 2026-27 | **Course Code:** RU-200-01-00029 (DSA)  
**Institution:** School of Computer Science and Engineering, Rungta International Skills University (RISU), Bhilai  

**Student Name:** Ashish Kumar  
**ERP ID:** RU-26-03142  
**Branch:** B.Tech Computer Science & Engineering (Artificial Intelligence and Machine Learning in association with IBM)  
**Faculty Mentor:** Mr. Surya Mani (IBM SME & Expert / Faculty Mentor)  

---

## 1. Project Overview

The **Smart College Management and Student Administration System** is a full-stack educational administration software designed to model collegiate workflows using foundational Data Structures and Algorithms (DSA).

Rather than relying on heavy relational database abstraction layers, all operational pipelines are engineered directly in memory in Java. This allows direct algorithmic time-complexity evaluation and demonstrates how foundational structures like **Hash Tables, Priority Queues (Min-Heaps), FIFO Queues, and LIFO Stacks** solve real-world institutional logistics challenges with optimal computational efficiency.

---

## 2. Core Data Structures Applied

| Data Structure | Java Implementation | Institutional Workflow | Algorithmic Complexity |
| :--- | :--- | :--- | :--- |
| **Hash Table** | `HashMap<String, Student>` | Constant-time student record retrieval, insertion, and departmental indexing by unique Student ID. | **$O(1)$ Average Lookup / Insertion** |
| **Min-Heap** | `PriorityQueue<Student>` | Automated Attendance Monitoring: Students falling below statutory 75% attendance float to the root for instant alert inspection. | **$O(1)$ Root Peek / $O(\log N)$ Re-heapify** |
| **FIFO Queue** | `Queue<AdmissionApplication>` | Student Admission & Registration Pipeline: Processes applicants in strict arrival sequence without starvation. | **$O(1)$ Enqueue / Dequeue** |
| **LIFO Stack** | `Stack<AdminActionTransaction>` | Administrative Audit Trail: Captures previous state snapshots enabling 1-click **Undo** rollback of accidental actions. | **$O(1)$ Push / Pop** |

---

## 3. How to Run the Project (Step-by-Step)

The project includes an embedded zero-dependency HTTP microservice and requires only a standard Java Development Kit (JDK 8 or above).

### Option A: One-Click Desktop Execution (Recommended)
1. Locate the file **`run_project.bat`** in the root workspace directory.
2. **Double-click** the file.
3. The script will automatically:
   - Detect the installed Java JDK (Microsoft OpenJDK 21 or system PATH).
   - Compile all Java source files in `src/` (with 0 warnings and 0 errors).
   - Start the HTTP server on port `8080` (or the next available port).
   - Automatically launch your default web browser to:  
     👉 **`http://localhost:8080/`**

### Option B: Terminal / Command Prompt Execution
Open a terminal in this directory and execute:
```cmd
run_project.bat
```
Alternatively, from within the `CollegeManagementSystem` folder:
```cmd
cd CollegeManagementSystem
run.bat
```

### Stopping the Application
To stop the web server and console session:
- In the active terminal window, enter `0` to gracefully shut down the service, or
- Close the command prompt window.

---

## 4. Web Dashboard Features

When the dashboard opens at `http://localhost:8080/`, you can interact with all four DSA workflows in real time:

1. **System KPI Status Cards:**
   - **Enrolled Students:** Live counter backed by `HashMap<String, Student>`.
   - **Attendance Alerts:** Live count of students with attendance below 75%, backed by `PriorityQueue<Student>`.
   - **Pending Admissions:** Queue depth counter backed by `Queue<AdmissionApplication>`.
   - **Undo History Depth:** Active transaction stack depth backed by `Stack<AdminActionTransaction>`.

2. **FIFO Admission Queue Panel:**
   - Displays incoming applicant cards labeled with queue position (`#1 HEAD`, `#2`, `#3`).
   - Clicking **"Process Next (FIFO Dequeue)"** polls the head applicant, automatically assigns a new Student ID, registers the record in the Hash Table, and pushes a rollback snapshot to the LIFO Stack.
   - Clicking **"+ Enqueue Admission"** allows adding new applicant payloads to the tail of the queue.

3. **Min-Heap Attendance Monitoring Panel:**
   - Surfaces all students requiring urgent counseling (attendance < 75%), strictly sorted in ascending order of attendance.
   - The student with the lowest attendance is highlighted as **`◆ ROOT NODE`** ($O(1)$ peek).
   - Color-coded badges distinguish between **`CRITICAL (<60%)`** and **`WARNING (<75%)`**.

4. **1-Click LIFO Undo Audit Bar:**
   - Displays the most recent operational snapshot at the top of the stack.
   - Clicking **"Undo Last Action"** pops the top transaction from the LIFO stack and restores the prior state (e.g., reverting an enrollment or restoring prior attendance).

5. **Enrolled Student Directory:**
   - Search box provides instant $O(1)$ filter by Student ID, Name, or Department without page reloads.
   - Filter dropdown allows isolating departments (CSE, AIML, IT, ECE, ME, CE).
   - Clicking on any attendance figure allows instant inline editing to observe dynamic re-heapification.

---

## 5. Directory Structure

```
ashish bhai/
│
├── run_project.bat                         <-- One-click desktop launcher
├── README.md                               <-- Official project documentation
│
└── CollegeManagementSystem/
    ├── run.bat                             <-- Internal compilation & launch script
    │
    ├── frontend/                           <-- Dedicated Web UI
    │   ├── index.html                      <-- Semantic HTML5 dashboard & modals
    │   ├── style.css                       <-- Responsive CSS3 layout & color-coded DSA badges
    │   └── script.js                       <-- Asynchronous REST API client engine
    │
    └── src/                                <-- Pure Java Backend Source
        ├── Main.java                       <-- Entry point, server startup & console CLI
        │
        ├── models/
        │   ├── Student.java                <-- Domain entity (implements Comparable<Student>)
        │   ├── AdmissionApplication.java   <-- FIFO queue payload
        │   └── AdminActionTransaction.java <-- LIFO stack transaction snapshot
        │
        ├── services/
        │   └── CollegeService.java         <-- Core DSA coordinator (HashMap, Heap, Queue, Stack)
        │
        └── server/
            └── CollegeWebServer.java       <-- Embedded Java HttpServer & REST endpoints
```

---

## 6. Viva & External Evaluation Reference Points

When explaining the architecture to external examiners:
1. **Why HashMap over ArrayList?**
   - Searching an `ArrayList` by student roll number requires a linear scan ($O(N)$). A `HashMap` provides constant-time $O(1)$ average access via hash hashing.
2. **Why Min-Heap for Attendance Alerts?**
   - Sorting an entire student list upon every query takes $O(N \log N)$ time. A `PriorityQueue` (Min-Heap) automatically keeps the most critical student at the root in $O(1)$ peek time, with logarithmic $O(\log N)$ updates.
3. **Why FIFO Queue for Admissions?**
   - Guarantees fairness: first-come applicants are cleared first without starvation or priority inversions.
4. **Why LIFO Stack for Undo?**
   - The most recently performed administrative action is always at the top of the stack ($O(1)$ pop), which is the exact semantic requirement for an Undo operation.

# Ashish-java-dsa-project
