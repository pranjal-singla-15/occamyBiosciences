// Base API URL
const API_BASE_URL = window.location.origin;

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    setupSidebarNavigation();
    setupCreateOfficerForm();
    setupAssignTaskForm();
    setupAssignMeetingForm();
    loadDashboardData();
});

// Check if user is authenticated and is an admin
function checkAuthentication() {
    const authToken = sessionStorage.getItem('authToken');
    const userRole = sessionStorage.getItem('userRole');

    if (!authToken || userRole !== 'ADMIN') {
        alert('Please login as an admin to access this page.');
        window.location.href = '../login/login.html';
        return;
    }
}

// Get auth headers
function getAuthHeaders() {
    const authToken = sessionStorage.getItem('authToken');
    return {
        'Authorization': `Basic ${authToken}`,
        'Content-Type': 'application/json'
    };
}

// Setup sidebar navigation
function setupSidebarNavigation() {
    const navLinks = document.querySelectorAll('.sidebar .nav-link');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();

            // Remove active class from all links
            navLinks.forEach(l => l.classList.remove('active'));

            // Add active class to clicked link
            this.classList.add('active');

            // Hide all content sections
            document.querySelectorAll('.content-section').forEach(section => {
                section.classList.remove('active');
            });

            // Show selected section
            const sectionId = this.getAttribute('data-section');
            document.getElementById(sectionId).classList.add('active');

            // Load data for specific sections
            if (sectionId === 'field-officers') {
                loadFieldOfficersTable();
            } else if (sectionId === 'reports') {
                loadReportsTable();
            } else if (sectionId === 'tasks') {
                loadFieldOfficersIntoDropdowns();
                loadTasksTable();
            }
        });
    });
}

// Setup create officer form
function setupCreateOfficerForm() {
    const form = document.getElementById('createOfficerForm');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await createFieldOfficer();
        });
    }
}

// Setup assign task form
function setupAssignTaskForm() {
    const form = document.getElementById('assignTaskForm');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await assignTask();
        });
    }
}

// Setup assign meeting form
function setupAssignMeetingForm() {
    const form = document.getElementById('assignMeetingForm');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            await assignMeeting();
        });
    }
}

// Assign task to field officer
async function assignTask() {
    const adminId = sessionStorage.getItem('adminId') || 1;
    const officerId = document.getElementById('taskOfficerSelect').value;
    const title = document.getElementById('taskTitle').value;
    const description = document.getElementById('taskDescription').value;
    const dueDate = document.getElementById('taskDueDate').value;
    const formMessage = document.getElementById('taskFormMessage');

    if (!officerId) {
        formMessage.innerHTML = '<div class="alert alert-danger">Please select a field officer</div>';
        return;
    }

    formMessage.innerHTML = '<div class="alert alert-info">Assigning task...</div>';

    try {
        let url = `${API_BASE_URL}/admin/tasks/assign?adminId=${adminId}&fieldOfficerId=${officerId}&title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}`;

        if (dueDate) {
            url += `&dueDate=${encodeURIComponent(dueDate)}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const task = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-success">
                    ✅ Task assigned successfully!<br>
                    <strong>Task:</strong> ${task.title}
                </div>
            `;
            document.getElementById('assignTaskForm').reset();

            // Reload tasks table
            loadTasksTable();

            setTimeout(() => {
                formMessage.innerHTML = '';
            }, 5000);
        } else {
            const error = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-danger">
                    ❌ Error: ${error.error || 'Failed to assign task'}
                </div>
            `;
        }
    } catch (error) {
        console.error('Error assigning task:', error);
        formMessage.innerHTML = `
            <div class="alert alert-danger">
                ❌ An error occurred. Please try again.
            </div>
        `;
    }
}

// Assign meeting to field officer
async function assignMeeting() {
    const adminId = sessionStorage.getItem('adminId') || 1;
    const officerId = document.getElementById('meetingOfficerSelect').value;
    const meetingType = document.getElementById('meetingType').value;
    const location = document.getElementById('meetingLocation').value;
    const latitude = document.getElementById('meetingLatitude').value;
    const longitude = document.getElementById('meetingLongitude').value;
    const notes = document.getElementById('meetingNotes').value;
    const formMessage = document.getElementById('meetingFormMessage');

    if (!officerId) {
        formMessage.innerHTML = '<div class="alert alert-danger">Please select a field officer</div>';
        return;
    }

    formMessage.innerHTML = '<div class="alert alert-info">Assigning meeting...</div>';

    try {
        let url = `${API_BASE_URL}/admin/meetings/assign?adminId=${adminId}&fieldOfficerId=${officerId}&meetingType=${meetingType}&location=${encodeURIComponent(location)}`;

        if (latitude) {
            url += `&latitude=${encodeURIComponent(latitude)}`;
        }
        if (longitude) {
            url += `&longitude=${encodeURIComponent(longitude)}`;
        }
        if (notes) {
            url += `&notes=${encodeURIComponent(notes)}`;
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const meeting = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-success">
                    ✅ Meeting assigned successfully!<br>
                    <strong>Type:</strong> ${meetingType}<br>
                    <strong>Location:</strong> ${location}
                </div>
            `;
            document.getElementById('assignMeetingForm').reset();

            setTimeout(() => {
                formMessage.innerHTML = '';
            }, 5000);
        } else {
            const error = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-danger">
                    ❌ Error: ${error.error || 'Failed to assign meeting'}
                </div>
            `;
        }
    } catch (error) {
        console.error('Error assigning meeting:', error);
        formMessage.innerHTML = `
            <div class="alert alert-danger">
                ❌ An error occurred. Please try again.
            </div>
        `;
    }
}

// Create field officer
async function createFieldOfficer() {
    const adminId = sessionStorage.getItem('adminId') || 1;
    const username = document.getElementById('officerUsername').value;
    const password = document.getElementById('officerPassword').value;
    const phone = document.getElementById('officerPhone').value;
    const formMessage = document.getElementById('formMessage');

    // Show loading message
    formMessage.innerHTML = '<div class="alert alert-info">Creating field officer...</div>';

    try {
        const response = await fetch(
            `${API_BASE_URL}/admin/create-field-officers?adminId=${adminId}&userName=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}&phoneNumber=${encodeURIComponent(phone)}`,
            {
                method: 'POST',
                headers: getAuthHeaders()
            }
        );

        if (response.ok) {
            const officer = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-success">
                    ✅ Field officer created successfully!<br>
                    <strong>Username:</strong> ${officer.userName}<br>
                    <strong>Phone:</strong> ${officer.phoneNumber}
                </div>
            `;

            // Reset form
            document.getElementById('createOfficerForm').reset();

            // Reload field officers list if on that tab
            setTimeout(() => {
                formMessage.innerHTML = '';
            }, 5000);

        } else {
            const error = await response.json();
            formMessage.innerHTML = `
                <div class="alert alert-danger">
                    ❌ Error: ${error.error || 'Failed to create field officer'}
                </div>
            `;
        }
    } catch (error) {
        console.error('Error creating field officer:', error);
        formMessage.innerHTML = `
            <div class="alert alert-danger">
                ❌ An error occurred. Please try again.
            </div>
        `;
    }
}

// Load dashboard data
async function loadDashboardData() {
    try {
        const adminId = sessionStorage.getItem('adminId') || 1;

        // Load field officers count
        await loadFieldOfficers(adminId);

        // Load sales reports
        await loadSalesReports();

        // Load meetings vs sales chart
        await loadMeetingsVsSalesChart(adminId);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Load field officers
async function loadFieldOfficers(adminId) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/field-officers?adminId=${adminId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const officers = await response.json();

            // Update summary card
            const activeOfficers = officers.length;
            updateSummaryCard('Active Field Workers', `${activeOfficers} / ${activeOfficers}`);
        } else {
            console.error('Failed to load field officers');
        }
    } catch (error) {
        console.error('Error loading field officers:', error);
    }
}

// Load field officers table
async function loadFieldOfficersTable() {
    const adminId = sessionStorage.getItem('adminId') || 1;
    const tbody = document.getElementById('officersTableBody');

    tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><span class="text-muted">Loading...</span></td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/field-officers?adminId=${adminId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const officers = await response.json();

            if (officers.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><span class="text-muted">No field officers found</span></td></tr>';
                return;
            }

            tbody.innerHTML = '';
            officers.forEach(officer => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${officer.id}</td>
                    <td>${officer.userName}</td>
                    <td>${officer.phoneNumber || 'N/A'}</td>
                    <td><span class="badge bg-success">Active</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-info" onclick="viewOfficer(${officer.id})">View</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><span class="text-danger">Failed to load officers</span></td></tr>';
        }
    } catch (error) {
        console.error('Error loading field officers table:', error);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4"><span class="text-danger">Error loading data</span></td></tr>';
    }
}

// View officer details (placeholder)
function viewOfficer(officerId) {
    alert(`View details for officer ID: ${officerId}\n\nThis feature will be implemented in the next update.`);
}

// Load reports table
async function loadReportsTable() {
    const tbody = document.getElementById('reportsTableBody');

    tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4"><span class="text-muted">Loading reports...</span></td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/reports/sales-vs-samples/all`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const reports = await response.json();

            if (reports.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4"><span class="text-muted">No reports available</span></td></tr>';
                return;
            }

            tbody.innerHTML = '';
            reports.forEach(report => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${report.officerName || 'N/A'}</td>
                    <td>-</td>
                    <td>-</td>
                    <td>₹ ${report.totalSales || 0}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4"><span class="text-danger">Failed to load reports</span></td></tr>';
        }
    } catch (error) {
        console.error('Error loading reports:', error);
        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4"><span class="text-danger">Error loading data</span></td></tr>';
    }
}

// Load sales reports
async function loadSalesReports() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/reports/sales-vs-samples/all`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const reports = await response.json();
            displaySalesReports(reports);
        } else {
            console.error('Failed to load sales reports');
        }
    } catch (error) {
        console.error('Error loading sales reports:', error);
    }
}

// Display sales reports
function displaySalesReports(reports) {
    if (reports.length === 0) {
        return;
    }

    let totalSales = 0;
    let totalSamples = 0;

    reports.forEach(report => {
        totalSales += report.totalSales || 0;
        totalSamples += report.totalSamples || 0;
    });

    // Update summary cards
    updateSummaryCard('Total Sales Today', `₹ ${totalSales.toLocaleString()}`);
}

// Update summary card
function updateSummaryCard(title, value) {
    const cards = document.querySelectorAll('.summary-card');

    cards.forEach(card => {
        const cardTitle = card.querySelector('h6').textContent;
        if (cardTitle === title) {
            card.querySelector('h4').textContent = value;
        }
    });
}

// Load and display meetings vs sales chart
let meetingsVsSalesChart = null;

async function loadMeetingsVsSalesChart(adminId) {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/chart/meetings-vs-sales?adminId=${adminId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            createMeetingsVsSalesChart(data);
        } else {
            console.error('Failed to load chart data');
        }
    } catch (error) {
        console.error('Error loading chart data:', error);
    }
}

function createMeetingsVsSalesChart(data) {
    const ctx = document.getElementById('meetingsVsSalesChart');

    if (!ctx) {
        console.error('Chart canvas not found');
        return;
    }

    // Destroy existing chart if it exists
    if (meetingsVsSalesChart) {
        meetingsVsSalesChart.destroy();
    }

    // Prepare data
    const labels = data.map(item => item.officerName || `Officer ${item.officerId}`);
    const meetingsData = data.map(item => item.totalMeetings || 0);
    const salesData = data.map(item => (item.totalSales || 0) / 100); // Scale down for better visualization

    // Create chart
    meetingsVsSalesChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Meetings',
                    data: meetingsData,
                    backgroundColor: 'rgba(47, 255, 213, 0.7)',
                    borderColor: 'rgba(47, 255, 213, 1)',
                    borderWidth: 2,
                    borderRadius: 5
                },
                {
                    label: 'Sales (x100)',
                    data: salesData,
                    backgroundColor: 'rgba(100, 181, 246, 0.7)',
                    borderColor: 'rgba(100, 181, 246, 1)',
                    borderWidth: 2,
                    borderRadius: 5
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top',
                    labels: {
                        color: '#e6f1ff',
                        font: {
                            size: 11
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(8, 20, 36, 0.95)',
                    titleColor: '#2fffd5',
                    bodyColor: '#e6f1ff',
                    borderColor: 'rgba(47, 255, 213, 0.3)',
                    borderWidth: 1,
                    padding: 10,
                    displayColors: true,
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            if (context.parsed.y !== null) {
                                if (context.dataset.label === 'Sales (x100)') {
                                    label += '₹' + (context.parsed.y * 100).toFixed(0);
                                } else {
                                    label += context.parsed.y;
                                }
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        color: '#9fb3c8',
                        font: {
                            size: 10
                        },
                        maxRotation: 45,
                        minRotation: 45
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)',
                        drawBorder: false
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        color: '#9fb3c8',
                        font: {
                            size: 10
                        }
                    },
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)',
                        drawBorder: false
                    }
                }
            }
        }
    });
}

// Load field officers into dropdowns for task and meeting assignment
async function loadFieldOfficersIntoDropdowns() {
    const adminId = sessionStorage.getItem('adminId') || 1;

    try {
        const response = await fetch(`${API_BASE_URL}/admin/field-officers?adminId=${adminId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const officers = await response.json();

            // Populate task officer select
            const taskSelect = document.getElementById('taskOfficerSelect');
            const meetingSelect = document.getElementById('meetingOfficerSelect');

            if (taskSelect) {
                taskSelect.innerHTML = '<option value="">-- Select Officer --</option>';
                officers.forEach(officer => {
                    const option = document.createElement('option');
                    option.value = officer.id;
                    option.textContent = `${officer.userName} (ID: ${officer.id})`;
                    taskSelect.appendChild(option);
                });
            }

            if (meetingSelect) {
                meetingSelect.innerHTML = '<option value="">-- Select Officer --</option>';
                officers.forEach(officer => {
                    const option = document.createElement('option');
                    option.value = officer.id;
                    option.textContent = `${officer.userName} (ID: ${officer.id})`;
                    meetingSelect.appendChild(option);
                });
            }
        } else {
            console.error('Failed to load field officers for dropdowns');
        }
    } catch (error) {
        console.error('Error loading field officers:', error);
    }
}

// Load tasks table
async function loadTasksTable() {
    const adminId = sessionStorage.getItem('adminId') || 1;
    const tbody = document.getElementById('tasksTableBody');

    if (!tbody) return;

    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="text-muted">Loading tasks...</span></td></tr>';

    try {
        const response = await fetch(`${API_BASE_URL}/admin/tasks?adminId=${adminId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const tasks = await response.json();

            if (tasks.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="text-muted">No tasks assigned yet</span></td></tr>';
                return;
            }

            tbody.innerHTML = '';
            tasks.forEach(task => {
                const row = document.createElement('tr');
                const dueDate = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'N/A';
                const assignedDate = task.assignedDate ? new Date(task.assignedDate).toLocaleDateString() : 'N/A';

                row.innerHTML = `
                    <td>${task.id}</td>
                    <td>${task.assignedToOfficer ? task.assignedToOfficer.userName : 'N/A'}</td>
                    <td>${task.title}</td>
                    <td><span class="badge bg-${getStatusColor(task.status)}">${task.status}</span></td>
                    <td>${dueDate}</td>
                    <td>${assignedDate}</td>
                `;
                tbody.appendChild(row);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="text-danger">Failed to load tasks</span></td></tr>';
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4"><span class="text-danger">Error loading data</span></td></tr>';
    }
}

// Helper function to get status badge color
function getStatusColor(status) {
    switch(status) {
        case 'PENDING': return 'warning';
        case 'IN_PROGRESS': return 'info';
        case 'COMPLETED': return 'success';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
}

// Logout function
function logout() {
    sessionStorage.clear();
    window.location.href = '../login/login.html';
}

// Add logout button to navbar
const navbar = document.querySelector('.navbar');
if (navbar) {
    const logoutBtn = document.createElement('button');
    logoutBtn.textContent = 'Logout';
    logoutBtn.className = 'btn btn-outline-info btn-sm ms-auto';
    logoutBtn.onclick = logout;
    navbar.appendChild(logoutBtn);
}

