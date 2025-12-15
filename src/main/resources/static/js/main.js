// ===== User Dropdown =====
function toggleUserDropdown() {
    const dropdown = document.getElementById('userDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', function(event) {
    const userMenu = document.querySelector('.user-menu');
    const dropdown = document.getElementById('userDropdown');
    if (userMenu && dropdown && !userMenu.contains(event.target)) {
        dropdown.classList.remove('show');
    }
});

// ===== FAQ Toggle =====
function toggleFaq(button) {
    const faqItem = button.parentElement;
    const answer = faqItem.querySelector('.faq-answer');
    const icon = button.querySelector('.faq-icon');

    if (answer.classList.contains('show')) {
        answer.classList.remove('show');
        icon.textContent = '+';
    } else {
        // Close all other FAQs
        document.querySelectorAll('.faq-answer.show').forEach(a => {
            a.classList.remove('show');
            a.parentElement.querySelector('.faq-icon').textContent = '+';
        });
        answer.classList.add('show');
        icon.textContent = '-';
    }
}

// ===== Sidebar Toggle =====
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('open');
    }
}

// ===== Favorite Toggle =====
function toggleFavorite(button) {
    const roomId = button.getAttribute('data-room-id');

    fetch('/favorites/toggle/' + roomId, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.isFavorite) {
            button.classList.add('active');
            button.innerHTML = '<span>&#9733;</span>';
        } else {
            button.classList.remove('active');
            button.innerHTML = '<span>&#9734;</span>';
        }

        // Show notification
        showNotification(data.message);
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('Error updating favorites', 'error');
    });
}

// ===== Update Floors by Building =====
function updateFloors() {
    const buildingSelect = document.getElementById('buildingSelect');
    const floorSelect = document.getElementById('floorSelect');

    if (!buildingSelect || !floorSelect) return;

    const building = buildingSelect.value;

    if (!building) {
        // Reset to show all floors
        return;
    }

    fetch('/rooms/api/floors?building=' + building)
        .then(response => response.json())
        .then(floors => {
            const currentValue = floorSelect.value;
            floorSelect.innerHTML = '<option value="">All Floors</option>';
            floors.forEach(floor => {
                const option = document.createElement('option');
                option.value = floor;
                option.textContent = 'Floor ' + floor;
                if (floor == currentValue) {
                    option.selected = true;
                }
                floorSelect.appendChild(option);
            });
        });
}

// ===== Notification =====
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = 'notification ' + type;
    notification.textContent = message;

    document.body.appendChild(notification);

    // Show notification
    setTimeout(() => {
        notification.classList.add('show');
    }, 10);

    // Hide and remove after 5 seconds
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 5000);
}

// ===== Form Validation =====
document.addEventListener('DOMContentLoaded', function() {
    // Add purple highlight to form inputs on focus
    const formControls = document.querySelectorAll('.form-control');
    formControls.forEach(input => {
        input.addEventListener('focus', function() {
            this.style.borderColor = '#6f42c1';
        });
        input.addEventListener('blur', function() {
            if (!this.value) {
                this.style.borderColor = '#dee2e6';
            }
        });
    });

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;

            const target = document.querySelector(targetId);
            if (target) {
                e.preventDefault();
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
});

// ===== Add notification styles dynamically =====
const notificationStyles = document.createElement('style');
notificationStyles.textContent = `
    .notification {
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 25px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 9999;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.3s ease;
    }

    .notification.show {
        opacity: 1;
        transform: translateY(0);
    }

    .notification.success {
        background: #28a745;
    }

    .notification.error {
        background: #dc3545;
    }
`;
document.head.appendChild(notificationStyles);
