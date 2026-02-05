// Base API URL
const API_BASE_URL = window.location.origin;

// Handle login form submission
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.querySelector('form');

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const email = document.querySelector('input[type="email"]').value;
            const password = document.querySelector('input[type="password"]').value;
            const button = document.querySelector('.ripple-btn');

            // Disable button during login
            button.disabled = true;
            button.textContent = 'Logging in...';

            try {
                // Using Basic Authentication
                const credentials = btoa(`${email}:${password}`);

                // Try to access a protected endpoint to verify login
                const response = await fetch(`${API_BASE_URL}/admin/field-officers?adminId=1`, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Basic ${credentials}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    // Admin user - store credentials and redirect
                    sessionStorage.setItem('authToken', credentials);
                    sessionStorage.setItem('userEmail', email);
                    sessionStorage.setItem('userRole', 'ADMIN');

                    // Get admin details
                    const adminData = await response.json();

                    alert('Login successful! Welcome Admin');
                    window.location.href = '../admin/admin.html';
                } else {
                    // Try field officer endpoints
                    const foResponse = await fetch(`${API_BASE_URL}/field-officer/meetings?userId=1`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Basic ${credentials}`,
                            'Content-Type': 'application/json'
                        }
                    });

                    if (foResponse.ok) {
                        // Field officer user
                        sessionStorage.setItem('authToken', credentials);
                        sessionStorage.setItem('userEmail', email);
                        sessionStorage.setItem('userRole', 'USER');

                        alert('Login successful! Welcome Field Officer');
                        window.location.href = '../field_officer/field_officer.html';
                    } else {
                        alert('Invalid credentials. Please try again.');
                    }
                }
            } catch (error) {
                console.error('Login error:', error);
                alert('An error occurred during login. Please try again.');
            } finally {
                button.disabled = false;
                button.textContent = 'Login';
            }
        });
    }
});

// Check if user is already logged in
function checkAuth() {
    const authToken = sessionStorage.getItem('authToken');
    const userRole = sessionStorage.getItem('userRole');

    if (authToken && userRole) {
        if (userRole === 'ADMIN') {
            window.location.href = '../admin/admin.html';
        } else {
            window.location.href = '../field_officer/field_officer.html';
        }
    }
}

// Check on page load
checkAuth();

