/**
 * GTPL_UG Management System - Main JavaScript
 * Enterprise-grade frontend functionality
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */

// DOM Ready
document.addEventListener('DOMContentLoaded', function() {
    initSidebar();
    initMobileMenu();
    initFormValidation();
    initTooltips();
    initCharts();
    initWidgets();
});

/**
 * Sidebar functionality
 */
function initSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const menuToggle = document.getElementById('menuToggle');
    
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('open');
        });
        
        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', function(e) {
            if (window.innerWidth <= 1024) {
                if (!sidebar.contains(e.target) && !menuToggle.contains(e.target)) {
                    sidebar.classList.remove('open');
                }
            }
        });
    }
}

/**
 * Mobile menu functionality
 */
function initMobileMenu() {
    // Add active state to current nav item
    const currentPath = window.location.pathname;
    const navItems = document.querySelectorAll('.nav-item');
    
    navItems.forEach(item => {
        const href = item.getAttribute('href');
        if (href && currentPath.startsWith(href)) {
            item.classList.add('active');
        }
    });
}

/**
 * Form validation
 */
function initFormValidation() {
    const forms = document.querySelectorAll('form[data-validate]');
    
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            let isValid = true;
            const requiredFields = form.querySelectorAll('[required]');
            
            requiredFields.forEach(field => {
                if (!field.value.trim()) {
                    isValid = false;
                    field.classList.add('is-invalid');
                    
                    // Add error message if not exists
                    let errorMsg = field.parentElement.querySelector('.error-message');
                    if (!errorMsg) {
                        errorMsg = document.createElement('span');
                        errorMsg.className = 'error-message';
                        errorMsg.style.color = 'var(--danger-color)';
                        errorMsg.style.fontSize = '0.75rem';
                        errorMsg.style.marginTop = '0.25rem';
                        errorMsg.style.display = 'block';
                        field.parentElement.appendChild(errorMsg);
                    }
                    errorMsg.textContent = 'This field is required';
                } else {
                    field.classList.remove('is-invalid');
                    const errorMsg = field.parentElement.querySelector('.error-message');
                    if (errorMsg) {
                        errorMsg.remove();
                    }
                }
            });
            
            if (!isValid) {
                e.preventDefault();
            }
        });
    });
    
    // Real-time validation
    const inputs = document.querySelectorAll('input[required], textarea[required], select[required]');
    inputs.forEach(input => {
        input.addEventListener('blur', function() {
            if (!this.value.trim()) {
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });
    });
}

/**
 * Tooltips initialization
 */
function initTooltips() {
    const tooltipTriggers = document.querySelectorAll('[data-tooltip]');
    
    tooltipTriggers.forEach(trigger => {
        trigger.addEventListener('mouseenter', function(e) {
            const tooltipText = this.getAttribute('data-tooltip');
            const tooltip = document.createElement('div');
            tooltip.className = 'tooltip';
            tooltip.textContent = tooltipText;
            tooltip.style.cssText = `
                position: absolute;
                background: var(--text-primary);
                color: white;
                padding: 0.375rem 0.75rem;
                border-radius: var(--radius-sm);
                font-size: 0.75rem;
                z-index: 1000;
                white-space: nowrap;
            `;
            
            document.body.appendChild(tooltip);
            
            const rect = this.getBoundingClientRect();
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
            tooltip.style.top = rect.top - tooltip.offsetHeight - 8 + 'px';
            
            this._tooltip = tooltip;
        });
        
        trigger.addEventListener('mouseleave', function() {
            if (this._tooltip) {
                this._tooltip.remove();
                this._tooltip = null;
            }
        });
    });
}

/**
 * Charts initialization
 */
function initCharts() {
    // Project Progress Chart
    const projectProgressChart = document.getElementById('projectProgressChart');
    if (projectProgressChart) {
        fetchProjectProgressData().then(data => {
            renderProjectProgressChart(projectProgressChart, data);
        });
    }
    
    // Vendor Performance Chart
    const vendorPerformanceChart = document.getElementById('vendorPerformanceChart');
    if (vendorPerformanceChart) {
        fetchVendorPerformanceData().then(data => {
            renderVendorPerformanceChart(vendorPerformanceChart, data);
        });
    }
    
    // Material Usage Chart
    const materialUsageChart = document.getElementById('materialUsageChart');
    if (materialUsageChart) {
        fetchMaterialUsageData().then(data => {
            renderMaterialUsageChart(materialUsageChart, data);
        });
    }
    
    // Daily Timeline Chart
    const dailyTimelineChart = document.getElementById('dailyTimelineChart');
    if (dailyTimelineChart) {
        fetchDailyTimelineData().then(data => {
            renderDailyTimelineChart(dailyTimelineChart, data);
        });
    }
}

/**
 * Widgets initialization
 */
function initWidgets() {
    // Auto-refresh widgets every 5 minutes
    setInterval(refreshWidgets, 300000);
    
    // Refresh on visibility change
    document.addEventListener('visibilitychange', function() {
        if (!document.hidden) {
            refreshWidgets();
        }
    });
}

/**
 * Refresh all widgets
 */
function refreshWidgets() {
    const widgets = document.querySelectorAll('[data-widget]');
    widgets.forEach(widget => {
        const widgetType = widget.getAttribute('data-widget');
        refreshWidget(widget, widgetType);
    });
}

/**
 * Refresh a single widget
 */
function refreshWidget(element, type) {
    const url = `/api/widgets/${type}`;
    fetch(url)
        .then(response => response.json())
        .then(data => {
            updateWidget(element, type, data);
        })
        .catch(error => console.error('Error refreshing widget:', error));
}

/**
 * Update widget with new data
 */
function updateWidget(element, type, data) {
    switch(type) {
        case 'vendor-stats':
            updateVendorStatsWidget(element, data);
            break;
        case 'project-summary':
            updateProjectSummaryWidget(element, data);
            break;
        case 'notifications':
            updateNotificationsWidget(element, data);
            break;
        case 'compliance-status':
            updateComplianceWidget(element, data);
            break;
    }
}

// API Functions

async function fetchProjectProgressData() {
    try {
        const response = await fetch('/api/charts/project-progress');
        return await response.json();
    } catch (error) {
        console.error('Error fetching project progress:', error);
        return [];
    }
}

async function fetchVendorPerformanceData() {
    try {
        const response = await fetch('/api/charts/vendor-performance');
        return await response.json();
    } catch (error) {
        console.error('Error fetching vendor performance:', error);
        return [];
    }
}

async function fetchMaterialUsageData() {
    try {
        const response = await fetch('/api/charts/material-usage');
        return await response.json();
    } catch (error) {
        console.error('Error fetching material usage:', error);
        return [];
    }
}

async function fetchDailyTimelineData() {
    try {
        const response = await fetch('/api/charts/daily-timeline');
        return await response.json();
    } catch (error) {
        console.error('Error fetching daily timeline:', error);
        return [];
    }
}

// Chart Rendering Functions

function renderProjectProgressChart(canvas, data) {
    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.name),
            datasets: [
                {
                    label: 'Completed (KM)',
                    data: data.map(d => d.completed),
                    backgroundColor: 'rgba(37, 99, 235, 0.8)',
                    borderColor: 'rgba(37, 99, 235, 1)',
                    borderWidth: 1
                },
                {
                    label: 'Remaining (KM)',
                    data: data.map(d => d.remaining),
                    backgroundColor: 'rgba(148, 163, 184, 0.5)',
                    borderColor: 'rgba(148, 163, 184, 1)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { stacked: true },
                y: { stacked: true, beginAtZero: true }
            },
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function renderVendorPerformanceChart(canvas, data) {
    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => d.vendorName),
            datasets: [{
                data: data.map(d => d.completionRate),
                backgroundColor: [
                    'rgba(37, 99, 235, 0.8)',
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                    'rgba(239, 68, 68, 0.8)',
                    'rgba(6, 182, 212, 0.8)'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function renderMaterialUsageChart(canvas, data) {
    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.materialName),
            datasets: [
                {
                    label: 'Assigned',
                    data: data.map(d => d.assigned),
                    backgroundColor: 'rgba(37, 99, 235, 0.8)'
                },
                {
                    label: 'Used',
                    data: data.map(d => d.used),
                    backgroundColor: 'rgba(16, 185, 129, 0.8)'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function renderDailyTimelineChart(canvas, data) {
    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(d => d.date),
            datasets: [{
                label: 'KM Completed',
                data: data.map(d => d.kmCompleted),
                borderColor: 'rgba(37, 99, 235, 1)',
                backgroundColor: 'rgba(37, 99, 235, 0.1)',
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true }
            },
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

// Widget Update Functions

function updateVendorStatsWidget(element, data) {
    element.querySelector('.stat-total').textContent = data.total;
    element.querySelector('.stat-active').textContent = data.active;
    element.querySelector('.stat-inactive').textContent = data.inactive;
}

function updateProjectSummaryWidget(element, data) {
    element.querySelector('.stat-total').textContent = data.total;
    element.querySelector('.stat-inProgress').textContent = data.inProgress;
    element.querySelector('.stat-completed').textContent = data.completed;
    element.querySelector('.stat-overdue').textContent = data.overdue;
}

function updateNotificationsWidget(element, data) {
    const badge = element.querySelector('.notification-badge');
    if (badge) {
        badge.textContent = data.unreadCount;
        badge.style.display = data.unreadCount > 0 ? 'block' : 'none';
    }
}

function updateComplianceWidget(element, data) {
    element.querySelector('.missing-count').textContent = data.projectsWithMissingUpdates;
}

// Utility Functions

/**
 * Format number with commas
 */
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * Format date
 */
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    });
}

/**
 * Format datetime
 */
function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Show confirmation dialog
 */
function confirmAction(message, callback) {
    if (confirm(message)) {
        callback();
    }
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        bottom: 1rem;
        right: 1rem;
        padding: 1rem 1.5rem;
        background: var(--bg-primary);
        border-left: 4px solid var(--${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'primary'}-color);
        box-shadow: var(--shadow-lg);
        border-radius: var(--radius-md);
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Add animation styles
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);
