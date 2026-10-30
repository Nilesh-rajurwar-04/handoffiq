import React from 'react';
import { CheckCircle2, XCircle, AlertCircle, ArrowRight } from 'lucide-react';

const ReadinessChecklistView = ({ checklist = [], missingRequirements = [], recommendedActions = [] }) => {
  return (
    <div>
      {/* Missing Requirements Warning Banner if any */}
      {missingRequirements && missingRequirements.length > 0 && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #FECACA', borderRadius: 'var(--radius-md)', padding: '16px', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#991B1B', fontWeight: 700, fontSize: '0.9rem', marginBottom: '6px' }}>
            <AlertCircle size={18} />
            <span>Missing Mandatory Criteria ({missingRequirements.length})</span>
          </div>
          <ul style={{ paddingLeft: '24px', fontSize: '0.825rem', color: '#7F1D1D', display: 'flex', flexDirection: 'column', gap: '4px' }}>
            {missingRequirements.map((item, idx) => (
              <li key={idx}>{item}</li>
            ))}
          </ul>
        </div>
      )}

      {/* 7-Criteria Checklist Items */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {checklist.map((item, index) => {
          const isPassed = item.isPassed;
          const isMandatory = item.isMandatory;

          return (
            <div
              key={index}
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                justifyContent: 'space-between',
                padding: '16px',
                borderRadius: 'var(--radius-md)',
                backgroundColor: isPassed ? '#F8FAFC' : '#FFFDF5',
                border: isPassed ? '1px solid #E2E8F0' : '1px solid #FDE68A',
                transition: 'all 0.2s ease',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '14px', flex: 1 }}>
                <div style={{ marginTop: '2px' }}>
                  {isPassed ? (
                    <CheckCircle2 size={22} style={{ color: '#10B981' }} />
                  ) : (
                    <XCircle size={22} style={{ color: isMandatory ? '#EF4444' : '#F59E0B' }} />
                  )}
                </div>

                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                    <span style={{ fontWeight: 700, fontSize: '0.95rem', color: 'var(--text-main)' }}>
                      {item.criterion}
                    </span>
                    {isMandatory && (
                      <span style={{ fontSize: '0.7rem', fontWeight: 700, padding: '2px 6px', borderRadius: '4px', backgroundColor: '#FEE2E2', color: '#991B1B' }}>
                        MANDATORY
                      </span>
                    )}
                  </div>

                  <p style={{ fontSize: '0.825rem', color: '#64748B', marginTop: '2px' }}>
                    {item.description}
                  </p>

                  <div style={{ fontSize: '0.8rem', color: isPassed ? '#059669' : '#B45309', marginTop: '6px', fontWeight: 500 }}>
                    Status: {item.statusDetails}
                  </div>

                  {!isPassed && item.correctiveAction && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.775rem', color: '#4F46E5', marginTop: '4px', fontWeight: 600 }}>
                      <ArrowRight size={14} />
                      <span>Recommended action: {item.correctiveAction}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Points Earned */}
              <div style={{ textAlign: 'right', minWidth: '70px', paddingLeft: '12px' }}>
                <div style={{ fontSize: '1rem', fontWeight: 800, color: isPassed ? '#10B981' : '#64748B' }}>
                  {item.earnedScore}/{item.maxScore}
                </div>
                <div style={{ fontSize: '0.7rem', color: '#94A3B8', fontWeight: 600 }}>
                  POINTS
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Recommended Corrective Actions Section */}
      {recommendedActions && recommendedActions.length > 0 && (
        <div style={{ marginTop: '24px', padding: '16px', backgroundColor: '#EEF2FF', borderRadius: 'var(--radius-md)', border: '1px solid #C7D2FE' }}>
          <h5 style={{ fontSize: '0.875rem', fontWeight: 700, color: '#312E81', marginBottom: '8px' }}>
            📋 Next Actions to Achieve 100% Handover Readiness
          </h5>
          <ol style={{ paddingLeft: '20px', fontSize: '0.825rem', color: '#3730A3', display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {recommendedActions.map((action, i) => (
              <li key={i}>{action}</li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
};

export default ReadinessChecklistView;
