import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notificationApi, taskApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import EmptyState from '../../components/common/EmptyState';
import {
  Bell,
  CheckCheck,
  AlertTriangle,
  Clock,
  ArrowRightLeft,
  UserX,
  ShieldAlert,
  ArrowRight,
  Filter
} from 'lucide-react';

export default function NotificationsPage() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Manager follow-ups summary
  const [overdueTasks, setOverdueTasks] = useState([]);
  const [blockedTasks, setBlockedTasks] = useState([]);

  useEffect(() => {
    fetchData();
  }, [unreadOnly]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [notifsRes, tasksRes] = await Promise.all([
        notificationApi.getNotifications({ unreadOnly: unreadOnly || undefined }),
        taskApi.getTasks({ size: 50 })
      ]);
      setNotifications(notifsRes.data?.content || notifsRes.data || []);

      const allTasks = tasksRes.data?.content || tasksRes.data || [];
      setOverdueTasks(allTasks.filter((t) => t.status !== 'COMPLETED' && t.dueDate && new Date(t.dueDate) < new Date()));
      setBlockedTasks(allTasks.filter((t) => t.status === 'BLOCKED'));
    } catch (err) {
      console.error(err);
      setError('Could not load notifications.');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      fetchData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      fetchData();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
            Notifications & Intelligent Follow-Ups
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
            Automated alerts for approaching deadlines, overdue items, unassigned tasks, and handover requests.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <Button
            variant="outline"
            icon={CheckCheck}
            onClick={handleMarkAllAsRead}
          >
            Mark All as Read
          </Button>
        </div>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Manager Follow-Up Center */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '16px' }}>
        {/* Overdue Items Alert Box */}
        <div style={{
          background: overdueTasks.length > 0 ? '#fef2f2' : 'var(--surface-color)',
          border: overdueTasks.length > 0 ? '1px solid #fecaca' : '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '18px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
            <AlertTriangle size={20} color={overdueTasks.length > 0 ? '#dc2626' : 'var(--text-muted)'} />
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: overdueTasks.length > 0 ? '#991b1b' : 'var(--text-primary)' }}>
              Overdue Deliverables ({overdueTasks.length})
            </h4>
          </div>
          <p style={{ fontSize: '12.5px', color: overdueTasks.length > 0 ? '#7f1d1d' : 'var(--text-secondary)', margin: '0 0 12px' }}>
            Tasks past their promised deadline requiring manager escalation or carry-forward.
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {overdueTasks.slice(0, 3).map((t) => (
              <div
                key={t.id}
                onClick={() => navigate(`/tasks/${t.id}`)}
                style={{
                  padding: '8px 10px',
                  borderRadius: '6px',
                  background: 'white',
                  border: '1px solid #fca5a5',
                  cursor: 'pointer',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  fontSize: '12px'
                }}
              >
                <span style={{ fontWeight: 600, color: '#991b1b' }}>{t.title}</span>
                <span style={{ color: '#dc2626', fontWeight: 700 }}>Due {t.dueDate}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Blocked Tasks Alert Box */}
        <div style={{
          background: blockedTasks.length > 0 ? '#fffbeb' : 'var(--surface-color)',
          border: blockedTasks.length > 0 ? '1px solid #fde68a' : '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          padding: '18px',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
            <Clock size={20} color={blockedTasks.length > 0 ? '#d97706' : 'var(--text-muted)'} />
            <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700, color: blockedTasks.length > 0 ? '#92400e' : 'var(--text-primary)' }}>
              Blocked Tasks ({blockedTasks.length})
            </h4>
          </div>
          <p style={{ fontSize: '12.5px', color: blockedTasks.length > 0 ? '#78350f' : 'var(--text-secondary)', margin: '0 0 12px' }}>
            Deliverables stalled by cross-team dependencies, missing API specs, or keys.
          </p>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {blockedTasks.slice(0, 3).map((t) => (
              <div
                key={t.id}
                onClick={() => navigate(`/tasks/${t.id}`)}
                style={{
                  padding: '8px 10px',
                  borderRadius: '6px',
                  background: 'white',
                  border: '1px solid #fcd34d',
                  cursor: 'pointer',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  fontSize: '12px'
                }}
              >
                <span style={{ fontWeight: 600, color: '#92400e' }}>{t.title}</span>
                <span style={{ color: '#b45309', fontWeight: 600 }}>{t.assignedEmployeeName}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Filter Card */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={unreadOnly}
            onChange={(e) => setUnreadOnly(e.target.checked)}
          />
          Show Unread Only
        </label>
      </div>

      {/* Notifications List */}
      {loading ? (
        <LoadingSpinner text="Fetching notifications..." />
      ) : notifications.length === 0 ? (
        <EmptyState
          title="No notifications"
          description="You are completely up to date with your tasks, handovers, and follow-ups."
          icon={Bell}
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {notifications.map((n) => (
            <div
              key={n.id}
              style={{
                background: n.isRead ? 'var(--surface-color)' : 'rgba(79, 70, 229, 0.04)',
                border: n.isRead ? '1px solid var(--border-color)' : '1px solid var(--primary-color)',
                borderRadius: 'var(--radius-md)',
                padding: '16px 20px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                boxShadow: 'var(--shadow-sm)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <div style={{
                  width: '38px',
                  height: '38px',
                  borderRadius: '10px',
                  background: n.notificationType?.includes('OVERDUE') ? '#fee2e2' : n.notificationType?.includes('HANDOVER') ? '#f3e8ff' : '#e0e7ff',
                  color: n.notificationType?.includes('OVERDUE') ? '#dc2626' : n.notificationType?.includes('HANDOVER') ? '#7e22ce' : '#4338ca',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0
                }}>
                  {n.notificationType?.includes('OVERDUE') ? <AlertTriangle size={18} /> : n.notificationType?.includes('HANDOVER') ? <ArrowRightLeft size={18} /> : <Bell size={18} />}
                </div>

                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2px' }}>
                    <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-primary)' }}>
                      {n.title}
                    </span>
                    {!n.isRead && (
                      <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--primary-color)' }} />
                    )}
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
                    {n.message}
                  </p>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>
                    {n.createdAt?.substring(0, 16).replace('T', ' ')}
                  </span>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                {n.actionUrl && (
                  <Button
                    size="sm"
                    variant="outline"
                    icon={ArrowRight}
                    onClick={() => navigate(n.actionUrl)}
                  >
                    View
                  </Button>
                )}
                {!n.isRead && (
                  <Button
                    size="sm"
                    variant="light"
                    onClick={() => handleMarkAsRead(n.id)}
                  >
                    Mark Read
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
