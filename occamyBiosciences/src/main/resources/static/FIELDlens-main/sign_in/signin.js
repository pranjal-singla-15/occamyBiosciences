// Base API URL
const API_BASE_URL = window.location.origin;

// Handle sign-up form submission
document.addEventListener('DOMContentLoaded', function() {
    const signupForm = document.querySelector('form');

    if (signupForm) {
        signupForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const fullName = document.querySelector('input[type="text"]').value;
            const email = document.querySelector('input[type="email"]').value;
            const password = document.getElementById('pass').value;
            const confirmPassword = document.getElementById('cpass').value;
            const termsCheckbox = document.querySelector('input[type="checkbox"]');
            const button = document.querySelector('.ripple-btn');

            // Validation
            if (password !== confirmPassword) {
                alert('Passwords do not match!');
                return;
            }

            if (!termsCheckbox.checked) {
                alert('Please accept the Terms & Privacy Policy');
                return;
            }

            // Disable button during registration
            button.disabled = true;
            button.textContent = 'Creating Account...';

            try {
                // Create admin user via public endpoint
                const response = await fetch(`${API_BASE_URL}/public`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        userName: email,
                        password: password,
                        phoneNumber: '0000000000' // Default phone number, can be updated later
                    })
                });

                if (response.ok || response.status === 201) {
                    alert('Account created successfully! Please login.');
                    window.location.href = '../login/login.html';
                } else {
                    const errorData = await response.text();
                    alert(`Registration failed: ${errorData}`);
                }
            } catch (error) {
                console.error('Registration error:', error);
                alert('An error occurred during registration. Please try again.');
            } finally {
                button.disabled = false;
                button.textContent = 'Create Account';
            }
        });
    }
});

