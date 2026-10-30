import React from 'react';
import Button from './Button';

const EmptyState = ({ icon: Icon, title, message, actionLabel, onAction }) => {
  return (
    <div style={{ textAlign: 'center', padding: '48px 24px', backgroundColor: '#ffffff', borderRadius: 'var(--radius-lg)', border: '1px dashed var(--border-color)' }}>
      {Icon && (
        <div style={{ display: 'inline-flex', padding: '16px', borderRadius: '50%', backgroundColor: 'var(--primary-light)', color: 'var(--primary)', marginBottom: '16px' }}>
          <Icon size={32} />
        </div>
      )}
      <h4 style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)', marginBottom: '6px' }}>{title}</h4>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem', maxWidth: '420px', margin: '0 auto 20px auto' }}>{message}</p>
      {actionLabel && onAction && (
        <Button variant="primary" onClick={onAction}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
};

export default EmptyState;
