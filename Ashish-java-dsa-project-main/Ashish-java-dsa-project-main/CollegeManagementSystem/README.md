# Smart College Management & Student Administration System
### Applied Data Structures and Algorithms (DSA) Practical Project
**Academic Session:** 2026-27 | **Course Code:** RU-200-01-00029 (DSA)  
**Institution:** School of Computer Science and Engineering, Rungta International Skills University (RISU), Bhilai  

**Student Name:** Ashish Kumar | **ERP ID:** RU-26-03142  
**Faculty Mentor:** Mr. Surya Mani (IBM SME & Expert / Faculty Mentor)  

---

## Quick Execution

Double-click **`run.bat`** (or **`run_project.bat`** in the root directory).  
The embedded Java HTTP server will start on port `8080` (or next open port) and automatically launch your browser to:  
👉 **`http://localhost:8080/`**

---

## Applied Data Structures

* **Hash Table (`HashMap<String, Student>`):** Constant-time $O(1)$ Student ID keyed lookup.
* **Min-Heap (`PriorityQueue<Student>`):** Root node surfaces the student with the lowest attendance for instant $O(1)$ critical alerts (< 75%).
* **FIFO Queue (`Queue<AdmissionApplication>`):** Sequential admission processing.
* **LIFO Stack (`Stack<AdminActionTransaction>`):** State snapshots for 1-click Undo rollback.

---

## Architecture Layout

* `frontend/`: Standalone web dashboard (`index.html`, `style.css`, `script.js`).
* `src/`: Core Java backend source (`models/`, `services/`, `server/`, `Main.java`).
* `run.bat`: Compilation and server launch batch script.
