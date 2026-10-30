import React from 'react';
import Badge from '../common/Badge';

const STATUS_CONFIG = {
  DRAFT: { label: 'Draft', variant: 'default' },
  AWAITING_REVIEW: { label: 'Awaiting Review', variant: 'warning' },
  AWAITING_ACCEPTANCE: { label: 'Awaiting Acceptance', variant: 'info' },
  ACCEPTED: { label: 'Accepted', variant: 'primary' },
  COMPLETED: { label: 'Completed', variant: 'success' },
  REJECTED: { label: 'Needs Revision', variant: 'danger' }
};

export default function HandoverStatusBadge({ status }) {
  const config = STATUS_CONFIG[status] || { label: status || 'Unknown', variant: 'default' };
  return (
    <Badge variant={config.variant}>
      {config.label}
    </Badge>
  );
}
