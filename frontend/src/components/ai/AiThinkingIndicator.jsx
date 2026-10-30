import React, { useEffect, useState } from 'react';
import { Sparkles, Brain, Cpu, CheckCircle } from 'lucide-react';

const DEFAULT_STEPS = [
  'Ingesting meeting transcript and notes...',
  'Extracting key discussion points and context...',
  'Identifying explicit commitments and action items...',
  'Resolving suggested task owners and deadlines...',
  'Structuring decisions and risk analysis...',
  'Formulating structured JSON output...'
];

export default function AiThinkingIndicator({
  title = 'AI Intelligence Processing',
  steps = DEFAULT_STEPS,
  provider = 'HandoffIQ AI Engine'
}) {
  const [currentStepIndex, setCurrentStepIndex] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentStepIndex((prev) => (prev < steps.length - 1 ? prev + 1 : prev));
    }, 1800);
    return () => clearInterval(interval);
  }, [steps.length]);

  return (
    <div style={{
      background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(37, 99, 235, 0.08) 100%)',
      border: '1px solid rgba(79, 70, 229, 0.2)',
      borderRadius: 'var(--radius-lg)',
      padding: '28px',
      textAlign: 'center',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: '16px',
      boxShadow: '0 4px 20px rgba(79, 70, 229, 0.08)'
    }}>
      <div style={{
        position: 'relative',
        width: '64px',
        height: '64px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div style={{
          position: 'absolute',
          inset: 0,
          borderRadius: '50%',
          border: '3px solid rgba(79, 70, 229, 0.2)',
          borderTopColor: 'var(--primary-color)',
          animation: 'spin 1.2s cubic-bezier(0.68, -0.55, 0.27, 1.55) infinite'
        }} />
        <div style={{
          width: '46px',
          height: '46px',
          borderRadius: '50%',
          background: 'linear-gradient(135deg, #4f46e5 0%, #2563eb 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          boxShadow: '0 4px 12px rgba(79, 70, 229, 0.4)'
        }}>
          <Sparkles size={22} />
        </div>
      </div>

      <div>
        <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)', marginBottom: '4px' }}>
          {title}
        </h4>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
          Powered by <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>{provider}</span>
        </p>
      </div>

      <div style={{
        width: '100%',
        maxWidth: '420px',
        background: 'var(--surface-color)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        padding: '12px 16px',
        textAlign: 'left'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: 500, color: 'var(--primary-color)' }}>
          <Cpu size={16} />
          <span>{steps[currentStepIndex]}</span>
        </div>
        <div style={{
          marginTop: '10px',
          height: '4px',
          background: 'var(--bg-secondary)',
          borderRadius: '999px',
          overflow: 'hidden'
        }}>
          <div style={{
            height: '100%',
            background: 'linear-gradient(90deg, #4f46e5, #2563eb)',
            width: `${((currentStepIndex + 1) / steps.length) * 100}%`,
            transition: 'width 0.4s ease'
          }} />
        </div>
      </div>
    </div>
  );
}
