// Base API URL
const API_BASE_URL = window.location.origin;

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
    checkAuthentication();
    loadDashboardData();
    setupEventListeners();
});

// Check if user is authenticated and is a field officer
function checkAuthentication() {
    const authToken = sessionStorage.getItem('authToken');
    const userRole = sessionStorage.getItem('userRole');

    if (!authToken || userRole !== 'USER') {
        alert('Please login as a field officer to access this page.');
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

// Load dashboard data
async function loadDashboardData() {
    try {
        // For now, we'll use dummy user ID = 1
        // In production, you should get this from the login response
        const userId = sessionStorage.getItem('userId') || 1;

        // Load meetings
        await loadMeetings(userId);

        // Load tasks
        await loadTasks(userId);

    } catch (error) {
        console.error('Error loading dashboard data:', error);
    }
}

// Load meetings
async function loadMeetings(userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/field-officer/meetings?userId=${userId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const meetings = await response.json();
            displayMeetings(meetings);

            // Update summary card
            updateSummaryCard('Meetings Done', meetings.length);
        } else {
            console.error('Failed to load meetings');
        }
    } catch (error) {
        console.error('Error loading meetings:', error);
    }
}

// Display meetings in activity section
function displayMeetings(meetings) {
    const activityCard = document.querySelector('.card-body:last-child');

    if (!activityCard || meetings.length === 0) {
        return;
    }

    activityCard.innerHTML = '';

    meetings.slice(0, 5).forEach(meeting => {
        const p = document.createElement('p');
        const date = new Date(meeting.createdAt);
        const timeStr = date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
        p.innerHTML = `<strong>${timeStr}</strong> ${meeting.meetingType}: ${meeting.location || 'N/A'}`;
        activityCard.appendChild(p);
    });
}

// Update summary card
function updateSummaryCard(title, value) {
    const cards = document.querySelectorAll('.summary-card');

    cards.forEach(card => {
        const cardTitle = card.querySelector('small').textContent;
        if (cardTitle === title) {
            card.querySelector('h4').textContent = value;
        }
    });
}

// Setup event listeners
function setupEventListeners() {
    // Start Day button
    const startDayCard = document.querySelector('.start-day');
    if (startDayCard) {
        startDayCard.addEventListener('click', startDay);
    }

    // Action cards
    const actionCards = document.querySelectorAll('.action-card');
    actionCards.forEach(card => {
        const action = card.textContent.trim();
        card.addEventListener('click', () => handleAction(action));
    });

    // Sidebar links
    const sidebarLinks = document.querySelectorAll('.sidebar a');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const section = link.getAttribute('data-section');

            // Update active state
            sidebarLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            // Handle section navigation
            handleSectionNavigation(section);
        });
    });

    // Task filter buttons
    const filterAllBtn = document.getElementById('filterAllTasks');
    const filterPendingBtn = document.getElementById('filterPendingTasks');
    const filterCompletedBtn = document.getElementById('filterCompletedTasks');

    if (filterAllBtn) {
        filterAllBtn.addEventListener('click', () => {
            const userId = sessionStorage.getItem('userId') || 1;
            loadTasks(userId);
        });
    }

    if (filterPendingBtn) {
        filterPendingBtn.addEventListener('click', () => {
            const userId = sessionStorage.getItem('userId') || 1;
            loadTasksByStatus(userId, 'PENDING');
        });
    }

    if (filterCompletedBtn) {
        filterCompletedBtn.addEventListener('click', () => {
            const userId = sessionStorage.getItem('userId') || 1;
            loadTasksByStatus(userId, 'COMPLETED');
        });
    }
}

// Start day (start attendance)
async function startDay() {
    const userId = sessionStorage.getItem('userId') || 1;

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            try {
                const response = await fetch(
                    `${API_BASE_URL}/field-officer/attendance/start?userId=${userId}&lat=${lat}&lng=${lng}`,
                    {
                        method: 'POST',
                        headers: getAuthHeaders()
                    }
                );

                if (response.ok) {
                    alert('Day started successfully! Your location has been recorded.');
                    sessionStorage.setItem('dayStarted', 'true');
                    updateStartDayButton();
                } else {
                    const error = await response.json();
                    alert(`Error: ${error.error || 'Failed to start day'}`);
                }
            } catch (error) {
                console.error('Error starting day:', error);
                alert('An error occurred while starting the day.');
            }
        }, (error) => {
            alert('Please enable location services to start your day.');
        });
    } else {
        alert('Geolocation is not supported by this browser.');
    }
}

// Update start day button
function updateStartDayButton() {
    const startDayCard = document.querySelector('.start-day .card-body');
    const dayStarted = sessionStorage.getItem('dayStarted');

    if (dayStarted === 'true') {
        startDayCard.textContent = 'End Day';
        startDayCard.parentElement.onclick = endDay;
    }
}

// End day (end attendance)
async function endDay() {
    const userId = sessionStorage.getItem('userId') || 1;

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            try {
                const response = await fetch(
                    `${API_BASE_URL}/field-officer/attendance/end?userId=${userId}&lat=${lat}&lng=${lng}`,
                    {
                        method: 'POST',
                        headers: getAuthHeaders()
                    }
                );

                if (response.ok) {
                    alert('Day ended successfully! Your location has been recorded.');
                    sessionStorage.removeItem('dayStarted');
                    updateStartDayButton();
                } else {
                    const error = await response.json();
                    alert(`Error: ${error.error || 'Failed to end day'}`);
                }
            } catch (error) {
                console.error('Error ending day:', error);
                alert('An error occurred while ending the day.');
            }
        }, (error) => {
            alert('Please enable location services to end your day.');
        });
    } else {
        alert('Geolocation is not supported by this browser.');
    }
}

// Handle action card clicks
function handleAction(action) {
    const userId = sessionStorage.getItem('userId') || 1;

    switch (action) {
        case 'Log Meeting':
            logMeeting();
            break;
        case 'Add Group Meeting':
            addGroupMeeting();
            break;
        case 'Distribute Sample':
            distributeSample();
            break;
        case 'Add Sale / Order':
            addSale();
            break;
    }
}

// Log meeting
async function logMeeting() {
    const userId = sessionStorage.getItem('userId') || 1;
    const location = prompt('Enter meeting location:');
    const notes = prompt('Enter meeting notes:');

    if (!location) return;

    try {
        const response = await fetch(`${API_BASE_URL}/field-officer/meetings`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                officer: { id: userId },
                meetingType: 'DOCTOR_VISIT',
                location: location,
                notes: notes || ''
            })
        });

        if (response.ok) {
            alert('Meeting logged successfully!');
            loadDashboardData();
        } else {
            const error = await response.json();
            alert(`Error: ${error.error || 'Failed to log meeting'}`);
        }
    } catch (error) {
        console.error('Error logging meeting:', error);
        alert('An error occurred while logging the meeting.');
    }
}

// Add group meeting
async function addGroupMeeting() {
    const userId = sessionStorage.getItem('userId') || 1;
    const location = prompt('Enter group meeting location:');
    const notes = prompt('Enter meeting notes:');

    if (!location) return;

    try {
        const response = await fetch(`${API_BASE_URL}/field-officer/meetings`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                officer: { id: userId },
                meetingType: 'GROUP_MEETING',
                location: location,
                notes: notes || ''
            })
        });

        if (response.ok) {
            alert('Group meeting added successfully!');
            loadDashboardData();
        } else {
            const error = await response.json();
            alert(`Error: ${error.error || 'Failed to add group meeting'}`);
        }
    } catch (error) {
        console.error('Error adding group meeting:', error);
        alert('An error occurred while adding the group meeting.');
    }
}

// Distribute sample
function distributeSample() {
    alert('Sample distribution feature - To be implemented with product selection UI');
}

// Add sale
function addSale() {
    alert('Add sale feature - To be implemented with product and amount selection UI');
}

// Handle section navigation
function handleSectionNavigation(section) {
    const userId = sessionStorage.getItem('userId') || 1;

    // Hide/show sections
    const dashboardContent = document.querySelector('.content > *:not(#tasksSection)');
    const tasksSection = document.getElementById('tasksSection');

    if (section === 'tasks') {
        // Show tasks section, hide dashboard
        if (tasksSection) tasksSection.style.display = 'block';
        document.querySelectorAll('.content > *:not(#tasksSection)').forEach(el => {
            el.style.display = 'none';
        });
        loadTasks(userId);
    } else if (section === 'dashboard') {
        // Show dashboard, hide tasks
        if (tasksSection) tasksSection.style.display = 'none';
        document.querySelectorAll('.content > *:not(#tasksSection)').forEach(el => {
            el.style.display = '';
        });
    } else if (section === 'start-day') {
        startDay();
    } else if (section === 'log-meeting') {
        logMeeting();
    } else if (section === 'add-group-meeting') {
        addGroupMeeting();
    } else if (section === 'distribute-sample') {
        distributeSample();
    } else if (section === 'add-sale') {
        addSale();
    }
}

// Load all tasks for an officer
async function loadTasks(officerId) {
    try {
        const response = await fetch(`${API_BASE_URL}/field-officer/tasks?officerId=${officerId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const tasks = await response.json();
            displayTasks(tasks);
        } else {
            const error = await response.json();
            console.error('Failed to load tasks:', error);
            displayTasksError('Failed to load tasks');
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
        displayTasksError('An error occurred while loading tasks');
    }
}

// Load tasks by status
async function loadTasksByStatus(officerId, status) {
    try {
        const response = await fetch(`${API_BASE_URL}/field-officer/tasks/status/${status}?officerId=${officerId}`, {
            method: 'GET',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            const tasks = await response.json();
            displayTasks(tasks);
        } else {
            const error = await response.json();
            console.error('Failed to load tasks:', error);
            displayTasksError('Failed to load tasks');
        }
    } catch (error) {
        console.error('Error loading tasks:', error);
        displayTasksError('An error occurred while loading tasks');
    }
}

// Display tasks in the tasks container
function displayTasks(tasks) {
    const tasksContainer = document.getElementById('tasksContainer');

    if (!tasksContainer) return;

    if (tasks.length === 0) {
        tasksContainer.innerHTML = '<p class="text-center text-muted">No tasks found</p>';
        return;
    }

    tasksContainer.innerHTML = '';

    tasks.forEach(task => {
        const taskCard = createTaskCard(task);
        tasksContainer.appendChild(taskCard);
    });
}

// Create a task card element
function createTaskCard(task) {
    const card = document.createElement('div');
    card.className = 'task-card';

    const statusClass = `status-${task.status.toLowerCase().replace('_', '-')}`;

    const dueDateStr = task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date';
    const assignedDateStr = new Date(task.assignedDate).toLocaleDateString();

    card.innerHTML = `
        <div class="task-title">${task.title}</div>
        <div class="task-description">${task.description || 'No description'}</div>
        <div class="task-meta">
            <span>
                <span class="status-badge ${statusClass}">${task.status.replace('_', ' ')}</span>
            </span>
            <span>📅 Due: ${dueDateStr}</span>
            <span>👤 Assigned by: ${task.assignedByAdminName}</span>
            <span>📆 Assigned: ${assignedDateStr}</span>
        </div>
        ${task.notes ? `<div class="task-description mt-2"><strong>Notes:</strong> ${task.notes}</div>` : ''}
        <div class="task-actions" id="actions-${task.id}">
            ${createTaskActionButtons(task)}
        </div>
    `;

    return card;
}

// Create action buttons based on task status
function createTaskActionButtons(task) {
    const userId = sessionStorage.getItem('userId') || 1;

    if (task.status === 'COMPLETED') {
        return `<span style="color: #4caf50; font-size: 13px;">✓ Completed on ${new Date(task.completedDate).toLocaleDateString()}</span>`;
    }

    let buttons = '';

    if (task.status === 'PENDING') {
        buttons += `<button class="btn-in-progress" onclick="updateTaskStatus(${task.id}, ${userId}, 'IN_PROGRESS')">
            Start Task
        </button>`;
    }

    if (task.status === 'PENDING' || task.status === 'IN_PROGRESS') {
        buttons += `<button class="btn-complete-task" onclick="updateTaskStatus(${task.id}, ${userId}, 'COMPLETED')">
            Mark as Completed
        </button>`;
    }

    return buttons;
}

// Update task status
async function updateTaskStatus(taskId, officerId, status) {
    try {
        const response = await fetch(
            `${API_BASE_URL}/field-officer/tasks/${taskId}/status?officerId=${officerId}&status=${status}`,
            {
                method: 'PUT',
                headers: getAuthHeaders()
            }
        );

        if (response.ok) {
            const updatedTask = await response.json();
            alert(`Task status updated to ${status.replace('_', ' ')}!`);

            // Reload tasks to reflect the change
            loadTasks(officerId);
        } else {
            const error = await response.json();
            alert(`Error: ${error.error || 'Failed to update task status'}`);
        }
    } catch (error) {
        console.error('Error updating task status:', error);
        alert('An error occurred while updating the task status');
    }
}

// Display error message in tasks container
function displayTasksError(message) {
    const tasksContainer = document.getElementById('tasksContainer');
    if (tasksContainer) {
        tasksContainer.innerHTML = `<p class="text-center text-danger">${message}</p>`;
    }
}

// Logout function
function logout() {
    sessionStorage.clear();
    window.location.href = '../login/login.html';
}

// Add logout button to topbar
const topbar = document.querySelector('.topbar');
if (topbar) {
    const logoutBtn = document.createElement('button');
    logoutBtn.textContent = 'Logout';
    logoutBtn.style.cssText = `
        background: transparent;
        border: 1px solid #2fffd5;
        color: #2fffd5;
        padding: 6px 16px;
        border-radius: 6px;
        cursor: pointer;
        font-size: 14px;
        margin-left: auto;
    `;
    logoutBtn.onclick = logout;
    topbar.appendChild(logoutBtn);
}

// Check if day was already started
updateStartDayButton();

