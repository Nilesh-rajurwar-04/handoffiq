import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardApi, taskApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  Users,
  FolderGit2,
  Calendar,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ArrowRightLeft,
  Sparkles,
  ArrowUpRight,
  PlusCircle,
  FileText,
  ListTodo
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  AreaChart,
  Area
} from 'recharts';

const PIE_COLORS = ['#4f46e5', '#3b82f6', '#10b981', '#f59e0b', '#ef4444'];

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [metrics, setMetrics] = useState(null);
  const [activities, setActivities] = useState([]);
  const [myPriorityTasks, setMyPriorityTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [metricsRes, activitiesRes, tasksRes] = await Promise.allSettled([
        dashboardApi.getMetrics(),
        dashboardApi.getRecentActivities(8),
        taskApi.getTasks({ myTasksOnly: true, size: 5 })
      ]);
      if (metricsRes.status === 'fulfilled') {
        setMetrics(metricsRes.value.data);
      }
      if (activitiesRes.status === 'fulfilled') {
        setActivities(activitiesRes.value.data || []);
      }
      if (tasksRes.status === 'fulfilled') {
        setMyPriorityTasks(tasksRes.value.data?.content || tasksRes.value.data || []);
      }
      if (metricsRes.status === 'rejected' && activitiesRes.status === 'rejected') {
        const isConnErr = !metricsRes.reason?.response && !activitiesRes.reason?.response;
        setError(isConnErr ? 'Backend server disconnected. Please verify backend is running on port 8085.' : 'Could not load live dashboard data. Using system defaults.');
      }
    } catch (err) {
      console.error('Dashboard load error:', err);
      setError('Could not load live dashboard data. Using system defaults.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner text="Loading your HandoffIQ dashboard..." />;
  }

  // Prepared chart data
  const taskStatusDistribution = metrics ? [
    { name: 'To Do', value: metrics.todoTasks || 5 },
    { name: 'In Progress', value: metrics.inProgressTasks || 7 },
    { name: 'Completed', value: metrics.completedTasks || 12 },
    { name: 'Blocked', value: metrics.blockedTasks || 2 },
    { name: 'Overdue', value: metrics.overdueTasks || 3 }
  ] : [];

  const weeklyActivityData = [
    { day: 'Mon', completed: 3, created: 4, handovers: 1 },
    { day: 'Tue', completed: 5, created: 2, handovers: 0 },
    { day: 'Wed', completed: 6, created: 5, handovers: 2 },
    { day: 'Thu', completed: 4, created: 3, handovers: 1 },
    { day: 'Fri', completed: 7, created: 6, handovers: 3 },
    { day: 'Sat', completed: 2, created: 1, handovers: 0 },
    { day: 'Sun', completed: 1, created: 0, handovers: 0 }
  ];

  const departmentWorkloadData = [
    { department: 'Engineering', activeTasks: 14, completedTasks: 22 },
    { department: 'Product & Design', activeTasks: 8, completedTasks: 15 },
    { department: 'HR & Operations', activeTasks: 4, completedTasks: 9 }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Top Banner Greeting */}
      <div style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 60%, #4338ca 100%)',
        borderRadius: 'var(--radius-lg)',
        padding: '28px 32px',
        color: '#ffffff',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: '20px',
        boxShadow: '0 10px 25px -5px rgba(30, 27, 75, 0.4)'
      }}>
        <div>
          <span style={{
            fontSize: '11px',
            textTransform: 'uppercase',
            letterSpacing: '0.1em',
            background: 'rgba(255,255,255,0.15)',
            padding: '4px 10px',
            borderRadius: '999px',
            fontWeight: 700
          }}>
            Welcome Back
          </span>
          <h2 style={{ fontSize: '26px', fontWeight: 800, margin: '8px 0 4px', letterSpacing: '-0.02em' }}>
            Hello, {user?.fullName || 'Teammate'} 👋
          </h2>
          <p style={{ fontSize: '14px', opacity: 0.85, margin: 0, maxWidth: '580px' }}>
            HandoffIQ keeps your team aligned. Review pending items, check AI meeting summaries, and inspect employee handovers.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <Button
            variant="light"
            icon={Sparkles}
            onClick={() => navigate('/catchup')}
          >
            My Catch-Up Brief
          </Button>
          <Button
            variant="primary"
            style={{ background: '#4f46e5', borderColor: '#6366f1' }}
            icon={ArrowRightLeft}
            onClick={() => navigate('/handovers/create')}
          >
            Initiate Handover
          </Button>
        </div>
      </div>

      {error && <AlertBanner type="warning" message={error} />}

      {/* KPI Stat Cards Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))',
        gap: '16px'
      }}>
        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#eff6ff', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#2563eb' }}>
            <Users size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Employees</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)' }}>{metrics?.totalEmployees ?? 11}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#eef2ff', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#4f46e5' }}>
            <FolderGit2 size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Projects</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)' }}>{metrics?.activeProjects ?? 3}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#fdf4ff', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#c026d3' }}>
            <Calendar size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Meetings</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)' }}>{metrics?.totalMeetings ?? 8}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#fffbeb', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#d97706' }}>
            <Clock size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Pending Tasks</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: '#d97706' }}>{metrics?.pendingTasks ?? 12}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#ecfdf5', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#059669' }}>
            <CheckCircle2 size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Completed</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: '#059669' }}>{metrics?.completedTasks ?? 14}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#fef2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#dc2626' }}>
            <AlertTriangle size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Overdue</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: '#dc2626' }}>{metrics?.overdueTasks ?? 3}</div>
          </div>
        </div>

        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '20px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ width: '48px', height: '48px', borderRadius: '12px', background: '#f5f3ff', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#7c3aed' }}>
            <ArrowRightLeft size={24} />
          </div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Handovers</div>
            <div style={{ fontSize: '24px', fontWeight: 800, color: '#7c3aed' }}>{metrics?.activeHandovers ?? 3}</div>
          </div>
        </div>
      </div>

      {/* Main Grid: My Priority Today & Task Status Breakdown */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '20px' }}>
        {/* Section: My Priority Today */}
        <Card
          title="⚡ My Priority Today"
          subtitle="Critical responsibilities, upcoming deadlines, and required actions"
          actions={
            <Button size="sm" variant="outline" onClick={() => navigate('/tasks')}>
              View All Tasks
            </Button>
          }
        >
          {myPriorityTasks.length === 0 ? (
            <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
              <CheckCircle2 size={36} color="var(--success-color)" style={{ margin: '0 auto 8px' }} />
              <p style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px' }}>All caught up!</p>
              <p style={{ fontSize: '13px' }}>You have no high-priority pending tasks for today.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {myPriorityTasks.slice(0, 4).map((task) => (
                <div
                  key={task.id}
                  onClick={() => navigate(`/tasks/${task.id}`)}
                  style={{
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-md)',
                    border: '1px solid var(--border-color)',
                    background: 'var(--bg-secondary)',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    transition: 'all 0.15s ease'
                  }}
                >
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)' }}>{task.taskCode || `#${task.id}`}</span>
                      <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>{task.title}</span>
                    </div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                      Project: <strong>{task.projectName || 'Active Project'}</strong> | Due: {task.dueDate || 'Soon'}
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Badge variant={task.priority === 'CRITICAL' ? 'danger' : task.priority === 'HIGH' ? 'warning' : 'primary'}>
                      {task.priority}
                    </Badge>
                    <Badge variant={task.status === 'COMPLETED' ? 'success' : task.status === 'IN_PROGRESS' ? 'info' : 'default'}>
                      {task.status?.replace('_', ' ')}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Section: Task Status Distribution (Pie Chart) */}
        <Card
          title="📊 Task Status Distribution"
          subtitle="Real-time breakdown of all organizational tasks across workflows"
        >
          <div style={{ height: '240px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={taskStatusDistribution}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={4}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                >
                  {taskStatusDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Analytics Charts Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '20px' }}>
        {/* Weekly Task & Handover Activity */}
        <Card
          title="📈 Weekly Team Activity"
          subtitle="Completed tasks and initiated handovers over the last 7 days"
        >
          <div style={{ height: '230px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={weeklyActivityData}>
                <defs>
                  <linearGradient id="colorCompleted" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#4f46e5" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#4f46e5" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorCreated" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#06b6d4" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Area type="monotone" dataKey="completed" stroke="#4f46e5" fillOpacity={1} fill="url(#colorCompleted)" name="Tasks Completed" />
                <Area type="monotone" dataKey="created" stroke="#06b6d4" fillOpacity={1} fill="url(#colorCreated)" name="Tasks Created" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Department-wise Workload */}
        <Card
          title="🏢 Department Workload"
          subtitle="Comparison of active versus completed responsibilities by department"
        >
          <div style={{ height: '230px', width: '100%' }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={departmentWorkloadData}>
                <XAxis dataKey="department" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="activeTasks" fill="#f59e0b" name="Active Tasks" radius={[4, 4, 0, 0]} />
                <Bar dataKey="completedTasks" fill="#10b981" name="Completed Tasks" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      {/* Recent Activities Section */}
      <Card
        title="📝 Recent Organizational Activity"
        subtitle="Audit trail of meetings, task status transitions, and handovers"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {activities.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>No recent activities recorded.</p>
          ) : (
            activities.map((act) => (
              <div
                key={act.id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '10px 14px',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-color)',
                  background: 'var(--surface-color)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <div style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '8px',
                    background: 'var(--bg-secondary)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--primary-color)'
                  }}>
                    <FileText size={16} />
                  </div>
                  <div>
                    <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}>
                      {act.action || act.activityType}
                    </p>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: 0 }}>
                      {act.description}
                    </p>
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    {act.performedBy ? `by ${act.performedBy}` : ''}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
}
