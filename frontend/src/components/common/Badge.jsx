import React from 'react';

const Badge = ({ variant = 'gray', children, icon: Icon, size = 'sm' }) => {
  const getVariantClass = () => {
    switch (variant.toLowerCase()) {
      case 'success':
      case 'completed':
      case 'approved':
      case 'optimal':
        return 'badge-success';
      case 'warning':
      case 'in_progress':
      case 'awaiting_review':
      case 'awaiting_acceptance':
      case 'high':
        return 'badge-warning';
      case 'danger':
      case 'blocked':
      case 'cancelled':
      case 'critical':
      case 'overloaded':
        return 'badge-danger';
      case 'info':
      case 'scheduled':
      case 'accepted':
      case 'medium':
        return 'badge-info';
      case 'purple':
      case 'draft':
      case 'todo':
        return 'badge-purple';
      default:
        return 'badge-gray';
    }
  };

  return (
    <span className={`badge ${getVariantClass()}`} style={{ fontSize: size === 'xs' ? '0.7rem' : '0.75rem' }}>
      {Icon && <Icon size={12} />}
      {children}
    </span>
  );
};

export default Badge;
