import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { userApi, taskApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  Users,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ArrowRight,
  Briefcase
} from 'lucide-react';

export default function EmployeeWorkloadPage() {
  const navigate = useNavigate();

  const [users, setUsers] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [usersRes, tasksRes] = await Promise.all([
        userApi.getUsers(),
        taskApi.getTasks({ size: 100 })
      ]);
      setUsers(usersRes.data || []);
      setTasks(tasksRes.data?.content || tasksRes.data || []);
    } catch (err) {
      console.error(err);
      setError('Could not load employee workload data.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <LoadingSpinner text="Calculating team workloads..." />;

  // Calculate workloads
  const employeeWorkloads = users.map((u) => {
    const userTasks = tasks.filter((t) => t.assignedEmployeeId === u.id);
    const activeTasks = userTasks.filter((t) => t.status !== 'COMPLETED' && t.status !== 'CANCELLED');
    const completedTasks = userTasks.filter((t) => t.status === 'COMPLETED');
    const overdueTasks = activeTasks.filter((t) => t.dueDate && new Date(t.dueDate) < new Date());

    let loadLevel = 'Normal';
    let badgeVariant = 'success';
    if (activeTasks.length >= 6 || overdueTasks.length >= 2) {
      loadLevel = 'Overloaded';
      badgeVariant = 'danger';
    } else if (activeTasks.length >= 4) {
      loadLevel = 'Heavy';
      badgeVariant = 'warning';
    }

    return {
      ...u,
      activeCount: activeTasks.length,
      completedCount: completedTasks.length,
      overdueCount: overdueTasks.length,
      loadLevel,
      badgeVariant
    };
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <div>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          Employee Workload & Capacity Management
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
          Monitor individual task distribution, overdue liabilities, and balanced team capacity.
        </p>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
        {employeeWorkloads.map((emp) => (
          <div
            key={emp.id}
            style={{
              background: 'var(--surface-color)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-md)',
              padding: '18px',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              gap: '14px',
              boxShadow: 'var(--shadow-sm)'
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{
                    width: '36px',
                    height: '36px',
                    borderRadius: '50%',
                    background: '#e0e7ff',
                    color: '#4338ca',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 700,
                    fontSize: '13px'
                  }}>
                    {emp.fullName?.charAt(0) || 'U'}
                  </div>
                  <div>
                    <h4 style={{ margin: 0, fontSize: '14.5px', fontWeight: 700, color: 'var(--text-primary)' }}>
                      {emp.fullName}
                    </h4>
                    <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>
                      {emp.jobTitle || 'Team Member'}
                    </span>
                  </div>
                </div>

                <Badge variant={emp.badgeVariant}>
                  {emp.loadLevel}
                </Badge>
              </div>

              <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr 1fr',
                gap: '8px',
                textAlign: 'center',
                background: 'var(--bg-secondary)',
                borderRadius: '8px',
                padding: '10px',
                marginTop: '10px'
              }}>
                <div>
                  <span style={{ display: 'block', fontSize: '10.5px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Active</span>
                  <strong style={{ fontSize: '16px', color: 'var(--primary-color)' }}>{emp.activeCount}</strong>
                </div>
                <div>
                  <span style={{ display: 'block', fontSize: '10.5px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Done</span>
                  <strong style={{ fontSize: '16px', color: 'var(--success-color)' }}>{emp.completedCount}</strong>
                </div>
                <div>
                  <span style={{ display: 'block', fontSize: '10.5px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Overdue</span>
                  <strong style={{ fontSize: '16px', color: emp.overdueCount > 0 ? 'var(--danger-color)' : 'var(--text-muted)' }}>
                    {emp.overdueCount}
                  </strong>
                </div>
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '10px', display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                size="sm"
                variant="outline"
                icon={ArrowRight}
                onClick={() => navigate('/tasks')}
              >
                Inspect Tasks
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
