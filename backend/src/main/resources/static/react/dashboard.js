/**
 * GTPL_UG Management System - React Dashboard Components
 * Enterprise-grade dashboard widgets with Chart.js integration
 * 
 * @author GTPL Development Team
 * @version 1.0.0
 */

const { useState, useEffect, useRef } = React;

// ============================================
// Chart Components
// ============================================

/**
 * Project Progress Chart Component
 * Displays completed vs remaining KM for all projects
 */
function ProjectProgressChart() {
    const canvasRef = useRef(null);
    const chartRef = useRef(null);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/charts/project-progress')
            .then(res => res.json())
            .then(data => {
                setData(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    useEffect(() => {
        if (!canvasRef.current || data.length === 0) return;

        const ctx = canvasRef.current.getContext('2d');
        
        if (chartRef.current) {
            chartRef.current.destroy();
        }

        chartRef.current = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(d => d.name.substring(0, 20)),
                datasets: [
                    {
                        label: 'Completed (KM)',
                        data: data.map(d => d.completed),
                        backgroundColor: 'rgba(37, 99, 235, 0.8)',
                        borderColor: 'rgba(37, 99, 235, 1)',
                        borderWidth: 1,
                        borderRadius: 4
                    },
                    {
                        label: 'Remaining (KM)',
                        data: data.map(d => d.remaining),
                        backgroundColor: 'rgba(148, 163, 184, 0.5)',
                        borderColor: 'rgba(148, 163, 184, 1)',
                        borderWidth: 1,
                        borderRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: { 
                        stacked: true,
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    },
                    y: { 
                        stacked: true, 
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Kilometers'
                        }
                    }
                },
                plugins: {
                    legend: { 
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        callbacks: {
                            afterLabel: function(context) {
                                const project = data[context.dataIndex];
                                return `Progress: ${project.progress}%`;
                            }
                        }
                    }
                }
            }
        });

        return () => {
            if (chartRef.current) {
                chartRef.current.destroy();
            }
        };
    }, [data]);

    if (loading) return <div className="chart-loading">Loading chart...</div>;
    if (error) return <div className="chart-error">Error: {error}</div>;

    return (
        <div className="chart-container">
            <canvas ref={canvasRef} height="250"></canvas>
        </div>
    );
}

/**
 * Vendor Performance Chart Component
 * Displays vendor completion rates as a doughnut chart
 */
function VendorPerformanceChart() {
    const canvasRef = useRef(null);
    const chartRef = useRef(null);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/charts/vendor-performance')
            .then(res => res.json())
            .then(data => {
                setData(data);
                setLoading(false);
            });
    }, []);

    useEffect(() => {
        if (!canvasRef.current || data.length === 0) return;

        const ctx = canvasRef.current.getContext('2d');
        
        if (chartRef.current) {
            chartRef.current.destroy();
        }

        const colors = [
            'rgba(37, 99, 235, 0.8)',
            'rgba(16, 185, 129, 0.8)',
            'rgba(245, 158, 11, 0.8)',
            'rgba(239, 68, 68, 0.8)',
            'rgba(6, 182, 212, 0.8)',
            'rgba(139, 92, 246, 0.8)'
        ];

        chartRef.current = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.map(d => d.vendorName),
                datasets: [{
                    data: data.map(d => d.completionRate),
                    backgroundColor: colors,
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '60%',
                plugins: {
                    legend: { 
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 15
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const vendor = data[context.dataIndex];
                                return `${vendor.vendorName}: ${vendor.completionRate}% (${vendor.completedKm}/${vendor.totalKm} KM)`;
                            }
                        }
                    }
                }
            }
        });

        return () => {
            if (chartRef.current) {
                chartRef.current.destroy();
            }
        };
    }, [data]);

    if (loading) return <div className="chart-loading">Loading chart...</div>;

    return (
        <div className="chart-container">
            <canvas ref={canvasRef} height="250"></canvas>
        </div>
    );
}

/**
 * Daily Timeline Chart Component
 * Shows daily KM completion over time
 */
function DailyTimelineChart() {
    const canvasRef = useRef(null);
    const chartRef = useRef(null);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/charts/daily-timeline')
            .then(res => res.json())
            .then(data => {
                setData(data);
                setLoading(false);
            });
    }, []);

    useEffect(() => {
        if (!canvasRef.current || data.length === 0) return;

        const ctx = canvasRef.current.getContext('2d');
        
        if (chartRef.current) {
            chartRef.current.destroy();
        }

        // Create gradient
        const gradient = ctx.createLinearGradient(0, 0, 0, 250);
        gradient.addColorStop(0, 'rgba(37, 99, 235, 0.3)');
        gradient.addColorStop(1, 'rgba(37, 99, 235, 0.0)');

        chartRef.current = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.map(d => {
                    const date = new Date(d.date);
                    return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short' });
                }),
                datasets: [{
                    label: 'KM Completed',
                    data: data.map(d => d.kmCompleted),
                    borderColor: 'rgba(37, 99, 235, 1)',
                    backgroundColor: gradient,
                    fill: true,
                    tension: 0.4,
                    pointRadius: 3,
                    pointBackgroundColor: 'rgba(37, 99, 235, 1)',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: { 
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Kilometers'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                },
                plugins: {
                    legend: { 
                        display: false
                    }
                }
            }
        });

        return () => {
            if (chartRef.current) {
                chartRef.current.destroy();
            }
        };
    }, [data]);

    if (loading) return <div className="chart-loading">Loading chart...</div>;

    return (
        <div className="chart-container">
            <canvas ref={canvasRef} height="250"></canvas>
        </div>
    );
}

// ============================================
// Widget Components
// ============================================

/**
 * Stats Widget Component
 * Displays key metrics in a card format
 */
function StatsWidget({ title, value, icon, color, trend, trendValue }) {
    const colorClasses = {
        primary: 'widget-primary',
        success: 'widget-success',
        warning: 'widget-warning',
        danger: 'widget-danger',
        info: 'widget-info'
    };

    return (
        <div className={`widget ${colorClasses[color] || 'widget-primary'}`}>
            <div className="widget-content">
                <div className="widget-icon">
                    <i className={`fas ${icon}`}></i>
                </div>
                <div className="widget-info">
                    <div className="widget-value">{value}</div>
                    <div className="widget-title">{title}</div>
                    {trend && (
                        <div className={`widget-trend ${trend}`}>
                            <i className={`fas fa-arrow-${trend}`}></i>
                            {trendValue}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

/**
 * Progress Widget Component
 * Shows progress bar with stats
 */
function ProgressWidget({ title, completed, total, unit = '' }) {
    const percentage = total > 0 ? Math.round((completed / total) * 100) : 0;
    
    return (
        <div className="widget widget-progress">
            <div className="widget-header">
                <h4>{title}</h4>
                <span className="percentage">{percentage}%</span>
            </div>
            <div className="progress-bar-container">
                <div 
                    className="progress-bar" 
                    style={{ width: `${percentage}%` }}
                ></div>
            </div>
            <div className="widget-stats">
                <span>{completed}{unit} completed</span>
                <span>{total}{unit} total</span>
            </div>
        </div>
    );
}

/**
 * Notification Widget Component
 * Shows recent notifications
 */
function NotificationWidget() {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    useEffect(() => {
        fetch('/api/widgets/notifications')
            .then(res => res.json())
            .then(data => {
                setNotifications(data.recentNotifications || []);
                setUnreadCount(data.unreadCount || 0);
            });
    }, []);

    const getIcon = (type) => {
        const icons = {
            'DAILY_UPDATE_MISSED': 'fa-exclamation-triangle',
            'PROJECT_ASSIGNED': 'fa-clipboard-check',
            'PROJECT_DELAYED': 'fa-clock',
            'MATERIAL_APPROVED': 'fa-check-circle',
            'MATERIAL_REJECTED': 'fa-times-circle',
            'PROGRESS_UPDATE': 'fa-chart-line',
            'SYSTEM_ALERT': 'fa-exclamation-circle',
            'DEADLINE_REMINDER': 'fa-calendar-alt'
        };
        return icons[type] || 'fa-bell';
    };

    return (
        <div className="widget widget-notifications">
            <div className="widget-header">
                <h4>Notifications</h4>
                {unreadCount > 0 && (
                    <span className="badge badge-danger">{unreadCount}</span>
                )}
            </div>
            <div className="notification-list">
                {notifications.length === 0 ? (
                    <div className="empty-state">
                        <i className="fas fa-bell-slash"></i>
                        <p>No new notifications</p>
                    </div>
                ) : (
                    notifications.map((notif, index) => (
                        <div key={index} className={`notification-item ${notif.read ? 'read' : 'unread'}`}>
                            <i className={`fas ${getIcon(notif.notificationType)}`}></i>
                            <div className="notification-content">
                                <div className="notification-title">{notif.title}</div>
                                <div className="notification-message">{notif.message}</div>
                                <div className="notification-time">{notif.timeAgo}</div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}

// ============================================
// Dashboard Container Components
// ============================================

/**
 * Admin Dashboard Component
 */
function AdminDashboard() {
    const [stats, setStats] = useState({
        totalProjects: 0,
        inProgressProjects: 0,
        completedProjects: 0,
        activeVendors: 0,
        overdueProjects: 0,
        pendingMaterialRequests: 0
    });

    useEffect(() => {
        fetch('/api/dashboard/admin')
            .then(res => res.json())
            .then(data => setStats(data));
    }, []);

    return (
        <div className="dashboard-container">
            <div className="stats-row">
                <StatsWidget 
                    title="Total Projects" 
                    value={stats.totalProjects} 
                    icon="fa-project-diagram" 
                    color="primary" 
                />
                <StatsWidget 
                    title="In Progress" 
                    value={stats.inProgressProjects} 
                    icon="fa-spinner" 
                    color="warning" 
                />
                <StatsWidget 
                    title="Completed" 
                    value={stats.completedProjects} 
                    icon="fa-check-circle" 
                    color="success" 
                />
                <StatsWidget 
                    title="Active Vendors" 
                    value={stats.activeVendors} 
                    icon="fa-users" 
                    color="info" 
                />
            </div>
            
            <div className="charts-row">
                <div className="chart-card">
                    <h3>Project Progress</h3>
                    <ProjectProgressChart />
                </div>
                <div className="chart-card">
                    <h3>Vendor Performance</h3>
                    <VendorPerformanceChart />
                </div>
            </div>
            
            <div className="charts-row">
                <div className="chart-card">
                    <h3>Daily Progress Timeline</h3>
                    <DailyTimelineChart />
                </div>
                <div className="widget-card">
                    <NotificationWidget />
                </div>
            </div>
        </div>
    );
}

/**
 * Vendor Dashboard Component
 */
function VendorDashboard() {
    const [stats, setStats] = useState({
        totalProjects: 0,
        activeProjects: 0,
        completedProjects: 0,
        totalKmAssigned: 0,
        totalKmCompleted: 0,
        overallProgress: 0
    });

    useEffect(() => {
        fetch('/api/dashboard/vendor')
            .then(res => res.json())
            .then(data => setStats(data));
    }, []);

    return (
        <div className="dashboard-container">
            <div className="stats-row">
                <StatsWidget 
                    title="Total Projects" 
                    value={stats.totalProjects} 
                    icon="fa-project-diagram" 
                    color="primary" 
                />
                <StatsWidget 
                    title="Active Projects" 
                    value={stats.activeProjects} 
                    icon="fa-spinner" 
                    color="warning" 
                />
                <StatsWidget 
                    title="Completed" 
                    value={stats.completedProjects} 
                    icon="fa-check-circle" 
                    color="success" 
                />
                <StatsWidget 
                    title="KM Completed" 
                    value={stats.totalKmCompleted} 
                    icon="fa-road" 
                    color="info" 
                />
            </div>
            
            <div className="progress-section">
                <ProgressWidget 
                    title="Overall Progress"
                    completed={stats.totalKmCompleted}
                    total={stats.totalKmAssigned}
                    unit=" KM"
                />
            </div>
            
            <div className="charts-row">
                <div className="chart-card">
                    <h3>My Project Progress</h3>
                    <ProjectProgressChart />
                </div>
                <div className="widget-card">
                    <NotificationWidget />
                </div>
            </div>
        </div>
    );
}

// ============================================
// Mount Components
// ============================================

// Mount Admin Dashboard
document.addEventListener('DOMContentLoaded', () => {
    const adminDashboardRoot = document.getElementById('admin-dashboard-root');
    if (adminDashboardRoot) {
        ReactDOM.render(<AdminDashboard />, adminDashboardRoot);
    }

    const vendorDashboardRoot = document.getElementById('vendor-dashboard-root');
    if (vendorDashboardRoot) {
        ReactDOM.render(<VendorDashboard />, vendorDashboardRoot);
    }
});
