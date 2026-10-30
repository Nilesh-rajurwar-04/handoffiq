import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Calendar,
  Sparkles,
  CheckSquare,
  ListTodo,
  ArrowRightLeft,
  BookOpen,
  Bot,
  Bell,
  BarChart3,
  Users,
  GitBranch,
  Settings,
  LogOut,
  ChevronRight,
  ShieldCheck,
  Compass
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { notificationsApi } from '../../services/api';

const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const fetchUnread = async () => {
      try {
        const res = await notificationsApi.getUnreadCount();
        setUnreadCount(res.data.unreadCount || 0);
      } catch (err) {
        // Silently fail if not logged in
      }
    };
    fetchUnread();
    const interval = setInterval(fetchUnread, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navSections = [
    {
      title: 'CORE PLATFORM',
      items: [
        { label: 'Dashboard', path: '/', icon: LayoutDashboard },
        { label: 'Meetings', path: '/meetings', icon: Calendar },
        { label: 'AI Meeting Intelligence', path: '/meetings/ai-intelligence', icon: Sparkles, badge: 'AI' },
        { label: 'My Tasks', path: '/my-tasks', icon: CheckSquare },
        { label: 'All Tasks', path: '/tasks', icon: ListTodo },
        { label: 'Employee Handovers', path: '/handovers', icon: ArrowRightLeft, highlight: true },
      ],
    },
    {
      title: 'KNOWLEDGE & CONTINUITY',
      items: [
        { label: 'AI Catch-up Brief', path: '/catch-up-brief', icon: Compass, badge: 'AI' },
        { label: 'Knowledge Base', path: '/knowledge-base', icon: BookOpen },
        { label: 'Handoff Assistant', path: '/assistant', icon: Bot, badge: 'Live' },
      ],
    },
    {
      title: 'MANAGEMENT & INSIGHTS',
      items: [
        { label: 'Notifications', path: '/notifications', icon: Bell, count: unreadCount },
        { label: 'Analytics', path: '/analytics', icon: BarChart3 },
        { label: 'Employees & Workload', path: '/employees', icon: Users },
        { label: 'Project Continuity View', path: '/projects/continuity', icon: GitBranch },
      ],
    },
  ];

  return (
    <aside
      style={{
        width: '260px',
        backgroundColor: '#0F172A',
        color: '#F8FAFC',
        display: 'flex',
        flexDirection: 'column',
        height: '100vh',
        position: 'sticky',
        top: 0,
        zIndex: 40,
        borderRight: '1px solid #1E293B',
      }}
    >
      {/* Brand Header */}
      <div
        style={{
          padding: '22px 20px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          borderBottom: '1px solid #1E293B',
        }}
      >
        <div
          style={{
            width: '38px',
            height: '38px',
            borderRadius: '10px',
            background: 'linear-gradient(135deg, #4F46E5 0%, #06B6D4 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#FFFFFF',
            boxShadow: '0 4px 10px rgba(79, 70, 229, 0.4)',
          }}
        >
          <ShieldCheck size={22} />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '1.2rem', fontWeight: 800, letterSpacing: '-0.03em', color: '#FFFFFF' }}>
              Handoff<span style={{ color: '#06B6D4' }}>IQ</span>
            </span>
            <span style={{ fontSize: '0.65rem', fontWeight: 700, padding: '1px 5px', borderRadius: '4px', backgroundColor: '#312E81', color: '#A5B4FC' }}>
              v1.0
            </span>
          </div>
          <p style={{ fontSize: '0.7rem', color: '#94A3B8', fontWeight: 500 }}>
            Knowledge & Task Continuity
          </p>
        </div>
      </div>

      {/* Navigation Links */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '16px 12px' }}>
        {navSections.map((sec, sIdx) => (
          <div key={sIdx} style={{ marginBottom: '22px' }}>
            <div
              style={{
                fontSize: '0.68rem',
                fontWeight: 700,
                color: '#64748B',
                letterSpacing: '0.08em',
                padding: '0 10px',
                marginBottom: '8px',
              }}
            >
              {sec.title}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '3px' }}>
              {sec.items.map((item, iIdx) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={iIdx}
                    to={item.path}
                    end={item.path === '/'}
                    style={({ isActive }) => ({
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '9px 12px',
                      borderRadius: '8px',
                      fontSize: '0.86rem',
                      fontWeight: isActive ? 600 : 500,
                      color: isActive ? '#FFFFFF' : '#94A3B8',
                      backgroundColor: isActive ? '#4F46E5' : 'transparent',
                      transition: 'all 0.15s ease',
                      textDecoration: 'none',
                    })}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <Icon size={18} />
                      <span>{item.label}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      {item.badge && (
                        <span
                          style={{
                            fontSize: '0.65rem',
                            fontWeight: 700,
                            padding: '1px 5px',
                            borderRadius: '4px',
                            backgroundColor: item.badge === 'AI' ? 'rgba(6, 182, 212, 0.2)' : 'rgba(16, 185, 129, 0.2)',
                            color: item.badge === 'AI' ? '#38BDF8' : '#34D399',
                          }}
                        >
                          {item.badge}
                        </span>
                      )}

                      {item.count > 0 && (
                        <span
                          style={{
                            fontSize: '0.7rem',
                            fontWeight: 700,
                            padding: '1px 6px',
                            borderRadius: '9999px',
                            backgroundColor: '#EF4444',
                            color: '#FFFFFF',
                          }}
                        >
                          {item.count}
                        </span>
                      )}
                    </div>
                  </NavLink>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* User Footer & Logout */}
      <div
        style={{
          padding: '14px 16px',
          borderTop: '1px solid #1E293B',
          backgroundColor: '#090D1A',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
            <img
              src={user?.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'}
              alt={user?.fullName || 'User'}
              style={{ width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover', border: '1px solid #334155' }}
            />
            <div style={{ minWidth: 0 }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: '#FFFFFF', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {user?.fullName || 'Active Employee'}
              </div>
              <div style={{ fontSize: '0.7rem', color: '#94A3B8', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {user?.role === 'ROLE_ADMIN' ? 'Admin' : user?.role === 'ROLE_MANAGER' ? 'Manager' : 'Employee'}
              </div>
            </div>
          </div>

          <button
            onClick={handleLogout}
            title="Log Out"
            style={{
              background: 'none',
              border: 'none',
              color: '#94A3B8',
              cursor: 'pointer',
              padding: '6px',
              borderRadius: '6px',
              display: 'flex',
              alignItems: 'center',
            }}
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
