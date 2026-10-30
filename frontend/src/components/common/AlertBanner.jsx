import React from 'react';
import { AlertCircle, CheckCircle2, Info, XCircle } from 'lucide-react';

const AlertBanner = ({ type = 'error', message, title, onClose }) => {
  if (!message) return null;

  const styles = {
    error: { bg: '#fef2f2', border: '#fecaca', text: '#991b1b', icon: XCircle },
    success: { bg: '#ecfdf5', border: '#a7f3d0', text: '#065f46', icon: CheckCircle2 },
    warning: { bg: '#fffbeb', border: '#fde68a', text: '#92400e', icon: AlertCircle },
    info: { bg: '#eff6ff', border: '#bfdbfe', text: '#1e40af', icon: Info },
  };

  const current = styles[type] || styles.info;
  const Icon = current.icon;

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: '12px',
        padding: '14px 18px',
        borderRadius: 'var(--radius-md)',
        backgroundColor: current.bg,
        border: `1px solid ${current.border}`,
        color: current.text,
        marginBottom: '18px',
        fontSize: '0.875rem',
      }}
    >
      <Icon size={20} style={{ flexShrink: 0, marginTop: '1px' }} />
      <div style={{ flex: 1 }}>
        {title && <strong style={{ display: 'block', marginBottom: '2px' }}>{title}</strong>}
        <span>{message}</span>
      </div>
    </div>
  );
};

export default AlertBanner;
