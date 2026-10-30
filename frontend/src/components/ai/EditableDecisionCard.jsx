import React, { useState } from 'react';
import { CheckCircle2, Trash2, Edit3, ShieldCheck } from 'lucide-react';
import Button from '../common/Button';

export default function EditableDecisionCard({ decision, onUpdate, onDelete }) {
  const [isEditing, setIsEditing] = useState(false);
  const [data, setData] = useState({
    decisionText: decision.decisionText || decision.decision || '',
    rationale: decision.rationale || '',
    impactedArea: decision.impactedArea || 'General',
    status: decision.status || 'Agreed'
  });

  const handleChange = (field, value) => {
    const updated = { ...data, [field]: value };
    setData(updated);
    if (onUpdate) onUpdate(updated);
  };

  return (
    <div style={{
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-md)',
      padding: '16px',
      background: 'var(--surface-color)',
      boxShadow: 'var(--shadow-sm)',
      position: 'relative',
      borderLeft: '4px solid var(--success-color)'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ShieldCheck size={18} color="var(--success-color)" />
          <span style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', fontWeight: 600 }}>
            Decision
          </span>
        </div>
        <div style={{ display: 'flex', gap: '6px' }}>
          <button
            onClick={() => setIsEditing(!isEditing)}
            title="Edit Decision"
            style={{
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--text-muted)',
              padding: '4px',
              borderRadius: '4px'
            }}
          >
            <Edit3 size={15} />
          </button>
          <button
            onClick={onDelete}
            title="Remove Decision"
            style={{
              background: 'transparent',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--danger-color)',
              padding: '4px',
              borderRadius: '4px'
            }}
          >
            <Trash2 size={15} />
          </button>
        </div>
      </div>

      {isEditing ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div>
            <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)' }}>Decision Statement</label>
            <textarea
              value={data.decisionText}
              onChange={(e) => handleChange('decisionText', e.target.value)}
              rows={2}
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                fontSize: '13px'
              }}
            />
          </div>
          <div>
            <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)' }}>Rationale / Context</label>
            <input
              type="text"
              value={data.rationale}
              onChange={(e) => handleChange('rationale', e.target.value)}
              placeholder="Why was this decided?"
              style={{
                width: '100%',
                padding: '8px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                fontSize: '13px'
              }}
            />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)' }}>Impacted Area</label>
              <input
                type="text"
                value={data.impactedArea}
                onChange={(e) => handleChange('impactedArea', e.target.value)}
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid var(--border-color)',
                  fontSize: '13px'
                }}
              />
            </div>
            <div>
              <label style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-muted)' }}>Status</label>
              <select
                value={data.status}
                onChange={(e) => handleChange('status', e.target.value)}
                style={{
                  width: '100%',
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid var(--border-color)',
                  fontSize: '13px'
                }}
              >
                <option value="Agreed">Agreed</option>
                <option value="Pending Review">Pending Review</option>
                <option value="Superceded">Superceded</option>
              </select>
            </div>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '4px' }}>
            <Button size="sm" variant="outline" onClick={() => setIsEditing(false)}>
              Done Editing
            </Button>
          </div>
        </div>
      ) : (
        <div>
          <p style={{ fontWeight: 600, fontSize: '14px', color: 'var(--text-primary)', marginBottom: '6px' }}>
            {data.decisionText || 'No decision text provided'}
          </p>
          {data.rationale && (
            <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '8px', fontStyle: 'italic' }}>
              <strong>Rationale:</strong> {data.rationale}
            </p>
          )}
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', alignItems: 'center' }}>
            <span style={{
              fontSize: '11px',
              padding: '2px 8px',
              borderRadius: '999px',
              background: 'var(--bg-secondary)',
              color: 'var(--text-secondary)'
            }}>
              Area: {data.impactedArea}
            </span>
            <span style={{
              fontSize: '11px',
              padding: '2px 8px',
              borderRadius: '999px',
              background: '#ecfdf5',
              color: '#065f46',
              fontWeight: 500
            }}>
              {data.status}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}
