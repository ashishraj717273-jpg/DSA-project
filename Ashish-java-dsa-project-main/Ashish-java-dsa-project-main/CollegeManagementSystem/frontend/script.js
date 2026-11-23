/**
 * Smart College Management & Student Administration System
 * Client-Side JavaScript Engine
 * Coordinates async REST API communication with Java backend.
 */

let allStudents = [];

// ==================== INITIAL BOOT & REFRESH ====================
document.addEventListener('DOMContentLoaded', () => {
    refreshData();
    setInterval(refreshData, 5000); // Polling every 5s for real-time queue & heap sync
});

async function refreshData() {
    try {
        // 1. Fetch System KPIs
        const statsRes = await fetch('/api/stats');
        if (statsRes.ok) {
            const stats = await statsRes.json();
            document.getElementById('kpiTotalStudents').textContent = stats.totalStudents;
            document.getElementById('kpiLowAttendance').textContent = stats.lowAttendanceAlerts;
            document.getElementById('kpiQueueDepth').textContent = stats.pendingAdmissions;
            document.getElementById('kpiStackDepth').textContent = stats.undoStackDepth;
            document.getElementById('kpiCriticalDesc').textContent = 'Root Peek: ' + stats.criticalStudent;
            document.getElementById('queueBadge').textContent = stats.pendingAdmissions + ' Pending';
            document.getElementById('alertBadge').textContent = stats.lowAttendanceAlerts + ' Critical';
        }

        // 2. Fetch Enrolled Students (HashMap O(1))
        const stuRes = await fetch('/api/students');
        if (stuRes.ok) {
            allStudents = await stuRes.json();
            renderStudents(allStudents);
        }

        // 3. Fetch FIFO Admission Queue
        const queueRes = await fetch('/api/admissions');
        if (queueRes.ok) {
            const queue = await queueRes.json();
            renderQueue(queue);
        }

        // 4. Fetch Min-Heap Attendance Alerts
        const alertRes = await fetch('/api/alerts');
        if (alertRes.ok) {
            const alerts = await alertRes.json();
            renderAlerts(alerts);
        }

    } catch (err) {
        console.warn('Network sync notice:', err);
    }
}

// ==================== RENDERERS ====================

function renderStudents(students) {
    const tbody = document.getElementById('studentTableBody');
    if (!students || students.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center" style="padding: 24px; color: var(--text-muted);">No student records match filter criteria.</td></tr>';
        return;
    }

    tbody.innerHTML = students.map(s => {
        const isLow = s.attendance < 75.0;
        const barColor = isLow ? 'var(--danger)' : 'var(--success)';
        const badgeClass = s.attendance < 60.0 ? 'badge-danger' : (isLow ? 'badge-warning' : 'badge-normal');

        return `<tr>
            <td><code class="code-id">${s.studentId}</code></td>
            <td>
                <strong>${s.name}</strong><br>
                <small style="color: var(--text-muted); font-size: 11px;">${s.email}</small>
            </td>
            <td>${s.department}</td>
            <td>${s.semester}</td>
            <td><strong>${s.cgpa.toFixed(2)}</strong></td>
            <td>
                <span style="font-weight: 600; cursor: pointer; text-decoration: underline;" onclick="promptEditAttendance('${s.studentId}', ${s.attendance})" title="Click to edit attendance">
                    ${s.attendance.toFixed(1)}%
                </span>
                <div class="att-progress-bar">
                    <div class="att-progress-fill" style="width: ${Math.min(100, s.attendance)}%; background: ${barColor};"></div>
                </div>
            </td>
            <td><span class="badge ${badgeClass}">${s.urgency}</span></td>
            <td>
                <button class="btn btn-outline-dark btn-sm" onclick="promptEditAttendance('${s.studentId}', ${s.attendance})">
                    Edit Att
                </button>
            </td>
        </tr>`;
    }).join('');
}

function renderQueue(queue) {
    const container = document.getElementById('queueListContainer');
    if (!queue || queue.length === 0) {
        container.innerHTML = '<div class="empty-state">&#x2713; Admission FIFO Queue is currently empty.</div>';
        return;
    }

    container.innerHTML = queue.map((app, idx) => {
        const isHead = idx === 0;
        return `
            <div class="queue-item ${isHead ? 'head-item' : ''}">
                <div class="item-left">
                    <h4>#${idx + 1} ${app.applicantName} <small style="color: var(--text-muted);">(${app.applicationId})</small></h4>
                    <p>${app.department} &bull; Entrance Score: <strong>${app.qualifyingScore.toFixed(1)}%</strong> &bull; Arrived: ${app.timestamp}</p>
                </div>
                <span class="badge ${isHead ? 'badge-success' : 'badge-warning'}">
                    ${isHead ? 'HEAD (NEXT)' : 'QUEUED'}
                </span>
            </div>
        `;
    }).join('');
}

function renderAlerts(alerts) {
    const container = document.getElementById('alertListContainer');
    if (!alerts || alerts.length === 0) {
        container.innerHTML = '<div class="empty-state" style="color: var(--success);">&#x2713; All students maintain satisfactory attendance (&ge; 75%).</div>';
        return;
    }

    container.innerHTML = alerts.map((s, idx) => {
        const isRoot = idx === 0;
        const badgeClass = s.attendance < 60.0 ? 'badge-danger' : 'badge-warning';

        return `
            <div class="alert-item ${isRoot ? 'root-item' : ''}">
                <div class="item-left">
                    <h4>${isRoot ? '&#x25C6; ROOT NODE: ' : ''}${s.name} <small style="color: var(--text-muted);">(${s.studentId})</small></h4>
                    <p>${s.department} &bull; Attendance: <strong style="color: var(--danger); font-size: 13px;">${s.attendance.toFixed(1)}%</strong> (Min: 75%)</p>
                </div>
                <span class="badge ${badgeClass}">${s.urgency}</span>
            </div>
        `;
    }).join('');
}

// ==================== SEARCH & FILTER ====================

function filterTable() {
    const q = document.getElementById('searchInput').value.toLowerCase().trim();
    const dept = document.getElementById('deptFilter').value;

    const filtered = allStudents.filter(s => {
        const matchesQuery = !q || (
            s.studentId.toLowerCase().includes(q) ||
            s.name.toLowerCase().includes(q) ||
            s.department.toLowerCase().includes(q)
        );
        const matchesDept = !dept || (s.department === dept);
        return matchesQuery && matchesDept;
    });

    renderStudents(filtered);
}

// ==================== REST DISPATCH ACTIONS ====================

async function processNextAdmission() {
    try {
        const res = await fetch('/api/admissions/process', { method: 'POST' });
        const data = await res.json();
        document.getElementById('lastActionText').textContent = 'Action: ' + data.message;
        refreshData();
    } catch (err) {
        console.error('Error processing admission:', err);
    }
}

async function triggerUndo() {
    try {
        const res = await fetch('/api/undo', { method: 'POST' });
        const data = await res.json();
        document.getElementById('lastActionText').textContent = data.message;
        refreshData();
    } catch (err) {
        console.error('Error triggering undo:', err);
    }
}

async function promptEditAttendance(id, current) {
    const val = prompt(`Update attendance percentage for ${id}:`, current);
    if (val !== null && !isNaN(val)) {
        const num = parseFloat(val);
        if (num < 0 || num > 100) {
            alert('Attendance must be between 0% and 100%.');
            return;
        }
        await fetch('/api/attendance', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ studentId: id, attendance: num })
        });
        refreshData();
    }
}

// ==================== MODAL CONTROLS ====================

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
}

async function submitEnqueue() {
    const name = document.getElementById('appApplicantName').value.trim();
    const dept = document.getElementById('appDepartment').value;
    const score = parseFloat(document.getElementById('appScore').value);

    if (!name || isNaN(score)) {
        alert('Please provide valid applicant name and qualifying score.');
        return;
    }

    await fetch('/api/admissions/enqueue', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ applicantName: name, department: dept, score: score })
    });

    closeModal('enqueueModal');
    document.getElementById('appApplicantName').value = '';
    document.getElementById('appScore').value = '';
    refreshData();
}

async function submitNewStudent() {
    const id = document.getElementById('newId').value.trim();
    const name = document.getElementById('newName').value.trim();
    const dept = document.getElementById('newDept').value.trim();
    const sem = parseInt(document.getElementById('newSem').value);
    const cgpa = parseFloat(document.getElementById('newCgpa').value);
    const att = parseFloat(document.getElementById('newAtt').value);

    if (!id || !name || !dept) {
        alert('Student ID, Name, and Department are mandatory fields.');
        return;
    }

    const res = await fetch('/api/students', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            studentId: id, name: name, department: dept,
            semester: sem, cgpa: cgpa, attendance: att
        })
    });

    const data = await res.json();
    if (res.status === 201) {
        closeModal('studentModal');
        document.getElementById('newId').value = '';
        document.getElementById('newName').value = '';
        document.getElementById('newDept').value = '';
        refreshData();
    } else {
        alert(data.message || 'Failed to add student.');
    }
}
