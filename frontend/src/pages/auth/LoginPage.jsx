import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { LogIn, ArrowRight, ShieldCheck, UserCheck, Briefcase } from 'lucide-react';
import Button from '../../components/common/Button';
import AlertBanner from '../../components/common/AlertBanner';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e?.preventDefault();
    if (!username || !password) {
      setError('Please provide both username and password.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      const serverErrors = err?.response?.data?.errors;
      const detail = serverErrors ? Object.values(serverErrors).join(', ') : null;
      setError(detail || err?.response?.data?.message || err.message || 'Login failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (demoUser, demoPass) => {
    setUsername(demoUser);
    setPassword(demoPass);
    setError(null);
    setLoading(true);
    try {
      await login(demoUser, demoPass);
      navigate('/dashboard');
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Quick login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%)',
      padding: '24px'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '460px',
        background: '#ffffff',
        borderRadius: '16px',
        boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.35)',
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          background: 'linear-gradient(135deg, #4f46e5 0%, #2563eb 100%)',
          padding: '32px 28px',
          color: '#ffffff',
          textAlign: 'center'
        }}>
          <div style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: '54px',
            height: '54px',
            borderRadius: '12px',
            background: 'rgba(255, 255, 255, 0.18)',
            marginBottom: '14px',
            backdropFilter: 'blur(8px)'
          }}>
            <LogIn size={28} color="#ffffff" />
          </div>
          <h1 style={{ fontSize: '24px', fontWeight: 800, margin: 0, letterSpacing: '-0.02em' }}>
            HandoffIQ
          </h1>
          <p style={{ fontSize: '13px', opacity: 0.9, marginTop: '6px', marginBottom: 0 }}>
            AI-Powered Knowledge & Task Management Platform
          </p>
        </div>

        {/* Form Body */}
        <div style={{ padding: '28px' }}>
          {error && <AlertBanner type="error" message={error} style={{ marginBottom: '16px' }} />}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Username or Email
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. rahul.sharma or manager"
                required
                style={{
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  fontSize: '14px',
                  outline: 'none',
                  transition: 'border-color 0.15s'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••••••"
                required
                style={{
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  fontSize: '14px',
                  outline: 'none',
                  transition: 'border-color 0.15s'
                }}
              />
            </div>

            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              icon={ArrowRight}
              style={{ width: '100%', marginTop: '4px' }}
            >
              Sign In to HandoffIQ
            </Button>
          </form>

          {/* 1-Click Demo Credentials */}
          <div style={{
            marginTop: '24px',
            paddingTop: '20px',
            borderTop: '1px solid #e2e8f0'
          }}>
            <p style={{ fontSize: '12px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '10px', textAlign: 'center' }}>
              Instant Demo Sign In (1-Click)
            </p>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px' }}>
              <button
                type="button"
                onClick={() => handleQuickLogin('admin', 'password123')}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '10px 6px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  background: '#f8fafc',
                  cursor: 'pointer',
                  transition: 'all 0.15s'
                }}
              >
                <ShieldCheck size={18} color="#4f46e5" />
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#1e293b' }}>Admin</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Full Access</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('manager', 'password123')}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '10px 6px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  background: '#f8fafc',
                  cursor: 'pointer',
                  transition: 'all 0.15s'
                }}
              >
                <Briefcase size={18} color="#0284c7" />
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#1e293b' }}>Manager</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Team Lead</span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('rahul.sharma', 'password123')}
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '4px',
                  padding: '10px 6px',
                  borderRadius: '8px',
                  border: '1px solid #cbd5e1',
                  background: '#f8fafc',
                  cursor: 'pointer',
                  transition: 'all 0.15s'
                }}
              >
                <UserCheck size={18} color="#10b981" />
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#1e293b' }}>Employee</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Developer</span>
              </button>
            </div>
          </div>

          <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <span style={{ fontSize: '13px', color: '#64748b' }}>
              Don't have an account?{' '}
              <Link to="/register" style={{ color: '#4f46e5', fontWeight: 600, textDecoration: 'none' }}>
                Register here
              </Link>
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
