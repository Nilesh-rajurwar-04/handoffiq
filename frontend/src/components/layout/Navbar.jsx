import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Bell,
  Search,
  Plus,
  ArrowRightLeft,
  Calendar,
  CheckSquare,
  UserCheck,
  ChevronDown
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import Button from '../common/Button';

const demoUsers = [
  { name: 'Sarah Jenkins', email: 'sarah.jenkins@handoffiq.com', role: 'Engineering Manager', pass: 'Password@123' },
  { name: 'Marcus Vance', email: 'marcus.vance@handoffiq.com', role: 'Product Manager', pass: 'Password@123' },
  { name: 'Rahul Sharma', email: 'rahul.sharma@handoffiq.com', role: 'Principal Backend (Outgoing)', pass: 'Password@123' },
  { name: 'Priya Patel', email: 'priya.patel@handoffiq.com', role: 'Senior Full Stack (Incoming)', pass: 'Password@123' },
  { name: 'David Kim', email: 'david.kim@handoffiq.com', role: 'QA Automation Lead', pass: 'Password@123' },
  { name: 'Admin User', email: 'admin@handoffiq.com', role: 'System Administrator', pass: 'Password@123' },
];

const Navbar = () => {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [showDemoMenu, setShowDemoMenu] = useState(false);
  const [switching, setSwitching] = useState(false);

  const getPageTitle = () => {
    const path = location.pathname;
    if (path === '/') return 'Executive Dashboard';
    if (path.startsWith('/meetings/ai-intelligence')) return 'AI Meeting Intelligence';
    if (path.startsWith('/meetings')) return 'Meeting Management';
    if (path.startsWith('/my-tasks')) return 'My Priority Tasks';
    if (path.startsWith('/tasks')) return 'Task Management';
    if (path.startsWith('/handovers/new')) return 'Initiate Employee Handover';
    if (path.startsWith('/handovers')) return 'Employee Handover Hub';
    if (path.startsWith('/catch-up-brief')) return 'AI Catch-up Brief';
    if (path.startsWith('/knowledge-base')) return 'Knowledge Base & Runbooks';
    if (path.startsWith('/assistant')) return 'Context-Aware Handoff Assistant';
    if (path.startsWith('/notifications')) return 'Notification & Follow-up Center';
    if (path.startsWith('/analytics')) return 'Telemetry & Analytics';
    if (path.startsWith('/employees')) return 'Employee Directory & Workload';
    if (path.startsWith('/projects')) return 'Project Continuity & Timeline';
    if (path.startsWith('/settings')) return 'Profile & Settings';
    return 'HandoffIQ';
  };

  const handleSwitchUser = async (demoUser) => {
    setSwitching(true);
    setShowDemoMenu(false);
    try {
      await login(demoUser.email, demoUser.pass);
      window.location.reload();
    } catch (err) {
      console.error('Failed to switch user', err);
    } finally {
      setSwitching(false);
    }
  };

  return (
    <header
      style={{
        height: '68px',
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--border-color)',
        padding: '0 32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        position: 'sticky',
        top: 0,
        zIndex: 30,
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.02)',
      }}
    >
      {/* Title & Context */}
      <div>
        <h1 style={{ fontSize: '1.25rem', fontWeight: 800, color: '#0F172A', lineHeight: 1.2 }}>
          {getPageTitle()}
        </h1>
        <div style={{ fontSize: '0.75rem', color: '#64748B', display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span>Enterprise Portal</span>
          <span>•</span>
          <span style={{ fontWeight: 600, color: '#4F46E5' }}>{user?.departmentName || 'Engineering'}</span>
        </div>
      </div>

      {/* Right Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
        {/* Quick Action: New Handover */}
        <Button
          variant="primary"
          size="sm"
          icon={ArrowRightLeft}
          onClick={() => navigate('/handovers/new')}
        >
          New Handover
        </Button>

        {/* Demo Account Switcher Dropdown */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setShowDemoMenu(!showDemoMenu)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '6px 12px',
              borderRadius: '8px',
              border: '1px solid #CBD5E1',
              backgroundColor: '#F8FAFC',
              fontSize: '0.8rem',
              fontWeight: 600,
              color: '#334155',
              cursor: 'pointer',
            }}
          >
            <UserCheck size={15} style={{ color: '#4F46E5' }} />
            <span>Switch Role: <strong>{user?.fullName?.split(' ')[0] || 'User'}</strong></span>
            <ChevronDown size={14} />
          </button>

          {showDemoMenu && (
            <div
              style={{
                position: 'absolute',
                top: '100%',
                right: 0,
                marginTop: '6px',
                width: '280px',
                backgroundColor: '#FFFFFF',
                borderRadius: '10px',
                boxShadow: 'var(--shadow-xl)',
                border: '1px solid var(--border-color)',
                padding: '8px',
                zIndex: 50,
              }}
            >
              <div style={{ padding: '6px 8px', fontSize: '0.7rem', fontWeight: 700, color: '#94A3B8', textTransform: 'uppercase' }}>
                Select Demo Role
              </div>
              {demoUsers.map((u, idx) => (
                <div
                  key={idx}
                  onClick={() => handleSwitchUser(u)}
                  style={{
                    padding: '8px 10px',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    fontSize: '0.825rem',
                    backgroundColor: user?.email === u.email ? '#EEF2FF' : 'transparent',
                    display: 'flex',
                    flexDirection: 'column',
                    transition: 'background-color 0.15s ease',
                  }}
                  onMouseEnter={(e) => { if (user?.email !== u.email) e.currentTarget.style.backgroundColor = '#F8FAFC'; }}
                  onMouseLeave={(e) => { if (user?.email !== u.email) e.currentTarget.style.backgroundColor = 'transparent'; }}
                >
                  <div style={{ fontWeight: 600, color: '#1E293B' }}>{u.name}</div>
                  <div style={{ fontSize: '0.72rem', color: '#64748B' }}>{u.role}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Notifications Shortcut */}
        <button
          onClick={() => navigate('/notifications')}
          title="Notifications"
          style={{
            position: 'relative',
            background: 'none',
            border: '1px solid var(--border-color)',
            borderRadius: '8px',
            width: '38px',
            height: '38px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#475569',
            cursor: 'pointer',
          }}
        >
          <Bell size={18} />
        </button>
      </div>
    </header>
  );
};

export default Navbar;
