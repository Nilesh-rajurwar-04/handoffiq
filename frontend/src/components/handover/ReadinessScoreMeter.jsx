import React from 'react';
import { Award, CheckCircle2, AlertTriangle, ShieldCheck } from 'lucide-react';

const ReadinessScoreMeter = ({ score = 0, isReadyForCompletion = false, size = 160 }) => {
  const strokeWidth = 14;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const progress = Math.min(Math.max(score, 0), 100);
  const strokeDashoffset = circumference - (progress / 100) * circumference;

  const getColor = () => {
    if (score >= 90) return '#10B981'; // Green
    if (score >= 75) return '#3B82F6'; // Blue
    if (score >= 50) return '#F59E0B'; // Amber
    return '#EF4444'; // Red
  };

  const getLabel = () => {
    if (score >= 90) return 'Ready for Final Sign-Off';
    if (score >= 75) return 'Good Continuity';
    if (score >= 50) return 'Needs Attention';
    return 'Critical Information Missing';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: '16px' }}>
      <div style={{ position: 'relative', width: size, height: size }}>
        <svg width={size} height={size}>
          {/* Background Ring */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#E2E8F0"
            strokeWidth={strokeWidth}
            fill="transparent"
          />
          {/* Progress Ring */}
          <circle
            className="readiness-meter-ring"
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={getColor()}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            style={{ transition: 'stroke-dashoffset 0.8s ease, stroke 0.4s ease' }}
          />
        </svg>

        {/* Center Text */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <span style={{ fontSize: '2.2rem', fontWeight: 800, color: '#0F172A', lineHeight: 1 }}>
            {score}
          </span>
          <span style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748B', marginTop: '2px' }}>
            OUT OF 100
          </span>
        </div>
      </div>

      <div style={{ marginTop: '16px' }}>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '0.875rem', fontWeight: 700, color: getColor() }}>
          {score >= 90 ? <ShieldCheck size={18} /> : score >= 75 ? <Award size={18} /> : <AlertTriangle size={18} />}
          <span>{getLabel()}</span>
        </div>
        <p style={{ fontSize: '0.8rem', color: '#64748B', marginTop: '4px' }}>
          {isReadyForCompletion ? (
            <span style={{ color: '#059669', fontWeight: 600 }}>✓ All mandatory completion criteria fulfilled</span>
          ) : (
            <span style={{ color: '#DC2626', fontWeight: 600 }}>⚠️ Incomplete: Review missing requirements below</span>
          )}
        </p>
      </div>
    </div>
  );
};

export default ReadinessScoreMeter;
