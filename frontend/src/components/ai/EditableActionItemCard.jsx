import React, { useState } from 'react';
import { User, Calendar, AlertCircle, Check, Trash2 } from 'lucide-react';
import Badge from '../common/Badge';

const EditableActionItemCard = ({ item, index, users = [], onUpdate, onDelete }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(item.title);
  const [description, setDescription] = useState(item.description || '');
  const [ownerId, setOwnerId] = useState(item.suggestedOwnerId || '');
  const [deadline, setDeadline] = useState(item.suggestedDeadline || '');
  const [priority, setPriority] = useState(item.priority || 'MEDIUM');
  const [isConfirmed, setIsConfirmed] = useState(item.isConfirmed || false);

  const handleSave = () => {
    const selectedUser = users.find((u) => String(u.id) === String(ownerId));
    onUpdate(index, {
      ...item,
      title,
      description,
      suggestedOwnerId: ownerId ? Number(ownerId) : null,
      suggestedOwnerName: selectedUser ? selectedUser.fullName : 'Unassigned',
      suggestedDeadline: deadline,
      priority,
      isConfirmed,
    });
    setIsEditing(false);
  };

  const handleToggleConfirm = (checked) => {
    setIsConfirmed(checked);
    onUpdate(index, { ...item, isConfirmed: checked });
  };

  return (
    <div
      style={{
        padding: '16px',
        borderRadius: 'var(--radius-md)',
        backgroundColor: isConfirmed ? '#F0FDF4' : '#FFFFFF',
        border: isConfirmed ? '1.5px solid #10B981' : '1px solid var(--border-color)',
        boxShadow: 'var(--shadow-sm)',
        marginBottom: '12px',
        transition: 'all 0.2s ease',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px', flex: 1 }}>
          <input
            type="checkbox"
            checked={isConfirmed}
            onChange={(e) => handleToggleConfirm(e.target.checked)}
            style={{ width: '18px', height: '18px', marginTop: '3px', cursor: 'pointer', accentColor: '#10B981' }}
          />

          <div style={{ flex: 1 }}>
            {isEditing ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                <input
                  type="text"
                  className="form-input"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Task title"
                />
                <textarea
                  className="form-textarea"
                  rows={2}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Action item description"
                />
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
                  <div>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748B' }}>Assigned Owner</label>
                    <select
                      className="form-select"
                      value={ownerId}
                      onChange={(e) => setOwnerId(e.target.value)}
                    >
                      <option value="">Unassigned</option>
                      {users.map((u) => (
                        <option key={u.id} value={u.id}>{u.fullName} ({u.designation || 'Engineer'})</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748B' }}>Target Deadline</label>
                    <input
                      type="date"
                      className="form-input"
                      value={deadline}
                      onChange={(e) => setDeadline(e.target.value)}
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748B' }}>Priority</label>
                    <select
                      className="form-select"
                      value={priority}
                      onChange={(e) => setPriority(e.target.value)}
                    >
                      <option value="LOW">LOW</option>
                      <option value="MEDIUM">MEDIUM</option>
                      <option value="HIGH">HIGH</option>
                      <option value="CRITICAL">CRITICAL</option>
                    </select>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
                  <button className="btn btn-primary btn-sm" onClick={handleSave}>Save Changes</button>
                  <button className="btn btn-secondary btn-sm" onClick={() => setIsEditing(false)}>Cancel</button>
                </div>
              </div>
            ) : (
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                  <span style={{ fontWeight: 700, fontSize: '0.925rem', color: '#0F172A' }}>{title}</span>
                  <Badge variant={priority.toLowerCase()}>{priority}</Badge>
                </div>
                {description && (
                  <p style={{ fontSize: '0.825rem', color: '#64748B', marginTop: '4px' }}>{description}</p>
                )}
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginTop: '8px', fontSize: '0.775rem', color: '#475569' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                    <User size={14} style={{ color: '#4F46E5' }} />
                    <span>Owner: <strong>{item.suggestedOwnerName || 'Unassigned'}</strong></span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                    <Calendar size={14} style={{ color: '#06B6D4' }} />
                    <span>Due: <strong>{deadline || 'Not set'}</strong></span>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {!isEditing && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <button
              onClick={() => setIsEditing(true)}
              style={{ fontSize: '0.75rem', fontWeight: 600, padding: '4px 8px', borderRadius: '4px', border: '1px solid #CBD5E1', background: '#F8FAFC', cursor: 'pointer', color: '#334155' }}
            >
              Edit
            </button>
            <button
              onClick={() => onDelete(index)}
              title="Remove Action Item"
              style={{ background: 'none', border: 'none', color: '#94A3B8', cursor: 'pointer', padding: '4px' }}
            >
              <Trash2 size={15} />
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default EditableActionItemCard;
