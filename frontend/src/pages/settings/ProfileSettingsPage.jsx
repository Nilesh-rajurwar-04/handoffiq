import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import AlertBanner from '../../components/common/AlertBanner';
import {
  User,
  Shield,
  Key,
  Cpu,
  Sparkles,
  Server,
  CheckCircle2,
  Lock
} from 'lucide-react';

export default function ProfileSettingsPage() {
  const { user } = useAuth();
  const [success, setSuccess] = useState(null);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '800px', margin: '0 auto', width: '100%' }}>
      <div>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          Profile & System Settings
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
          Inspect your active role permissions, organizational credentials, and configured AI engine.
        </p>
      </div>

      {success && <AlertBanner type="success" message={success} />}

      {/* User Information Card */}
      <Card title="👤 User Profile">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '56px',
              height: '56px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #4f46e5 0%, #2563eb 100%)',
              color: 'white',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '22px',
              fontWeight: 800
            }}>
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div>
              <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)' }}>
                {user?.fullName || 'Active User'}
              </h3>
              <p style={{ margin: '2px 0 0', fontSize: '13px', color: 'var(--text-secondary)' }}>
                {user?.jobTitle || 'Team Member'} • {user?.departmentName || 'Engineering'}
              </p>
            </div>
          </div>

          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '14px',
            borderTop: '1px solid var(--border-color)',
            paddingTop: '14px',
            fontSize: '13px'
          }}>
            <div>
              <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Username</span>
              <strong style={{ color: 'var(--text-primary)' }}>{user?.username || 'user'}</strong>
            </div>
            <div>
              <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Email</span>
              <strong style={{ color: 'var(--text-primary)' }}>{user?.email || 'user@digiplus.com'}</strong>
            </div>
            <div>
              <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Active Role</span>
              <Badge variant={user?.role === 'ROLE_ADMIN' ? 'danger' : user?.role === 'ROLE_MANAGER' ? 'primary' : 'success'}>
                {user?.role?.replace('ROLE_', '') || 'EMPLOYEE'}
              </Badge>
            </div>
          </div>
        </div>
      </Card>

      {/* AI Provider Configuration Card */}
      <Card title="🤖 Configured AI Service Engine">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '14px',
            borderRadius: '8px',
            background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(37, 99, 235, 0.05) 100%)',
            border: '1px solid rgba(79, 70, 229, 0.2)'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ width: '40px', height: '40px', borderRadius: '10px', background: '#eef2ff', color: '#4f46e5', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Cpu size={22} />
              </div>
              <div>
                <h4 style={{ margin: 0, fontSize: '14.5px', fontWeight: 700, color: 'var(--text-primary)' }}>
                  Configurable AI Provider Engine
                </h4>
                <p style={{ margin: '2px 0 0', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                  Auto-detects Google Gemini or OpenAI; switches seamlessly to Rule-Based Fallback if keys are unconfigured.
                </p>
              </div>
            </div>
            <Badge variant="success">Active</Badge>
          </div>

          <div style={{ fontSize: '12.5px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
            <p style={{ margin: '0 0 6px' }}><strong>Supported Environment Variables:</strong></p>
            <ul style={{ margin: 0, paddingLeft: '18px' }}>
              <li><code>AI_PROVIDER</code> = <code>GEMINI</code> | <code>OPENAI</code> | <code>FALLBACK</code></li>
              <li><code>GEMINI_API_KEY</code> = Your Google Gemini API Key</li>
              <li><code>OPENAI_API_KEY</code> = Your OpenAI API Key</li>
            </ul>
          </div>
        </div>
      </Card>

      {/* Security & Token Lifecycle */}
      <Card title="🛡️ Security & Authentication">
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px', color: 'var(--text-secondary)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--success-color)', fontWeight: 600 }}>
            <CheckCircle2 size={16} />
            <span>Stateless JJWT 0.12.5 with BCrypt (Strength 12) Encryption</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--success-color)', fontWeight: 600 }}>
            <CheckCircle2 size={16} />
            <span>MDC Request Tracing with X-Correlation-Id on every REST invocation</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--success-color)', fontWeight: 600 }}>
            <CheckCircle2 size={16} />
            <span>Role-Based Access Control enforced at method level via @PreAuthorize</span>
          </div>
        </div>
      </Card>
    </div>
  );
}
