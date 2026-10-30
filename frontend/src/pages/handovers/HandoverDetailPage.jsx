import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { handoverApi, aiApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import HandoverStatusBadge from '../../components/handover/HandoverStatusBadge';
import ReadinessScoreMeter from '../../components/handover/ReadinessScoreMeter';
import ReadinessChecklistView from '../../components/handover/ReadinessChecklistView';
import AiThinkingIndicator from '../../components/ai/AiThinkingIndicator';
import {
  ArrowLeft,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
  Sparkles,
  AlertTriangle,
  RefreshCw,
  FileText,
  UserCheck,
  Calendar,
  Lock,
  ListTodo
} from 'lucide-react';

export default function HandoverDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [handover, setHandover] = useState(null);
  const [readiness, setReadiness] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionSuccess, setActionSuccess] = useState(null);

  // AI Generation State
  const [generatingAi, setGeneratingAi] = useState(false);
  const [recalculating, setRecalculating] = useState(false);
  const [acknowledging, setAcknowledging] = useState(false);
  const [completing, setCompleting] = useState(false);

  useEffect(() => {
    fetchHandover();
  }, [id]);

  const fetchHandover = async () => {
    setLoading(true);
    try {
      const res = await handoverApi.getHandover(id);
      setHandover(res.data);
      // If readiness report is returned, set it
      if (res.data?.readinessReport) {
        setReadiness(res.data.readinessReport);
      } else {
        // Fetch explicit readiness score
        const scoreRes = await handoverApi.getReadinessScore(id);
        setReadiness(scoreRes.data);
      }
    } catch (err) {
      console.error(err);
      setError('Could not load handover package details.');
    } finally {
      setLoading(false);
    }
  };

  const handleRecalculateScore = async () => {
    setRecalculating(true);
    setActionSuccess(null);
    try {
      const res = await handoverApi.getReadinessScore(id);
      setReadiness(res.data);
      setActionSuccess(`Readiness score refreshed: ${res.data.score}%`);
    } catch (err) {
      setError('Failed to recalculate readiness score.');
    } finally {
      setRecalculating(false);
    }
  };

  const handleGenerateAiReport = async () => {
    setGeneratingAi(true);
    setActionSuccess(null);
    setError(null);
    try {
      const res = await aiApi.generateHandoverReport({ handoverId: Number(id) });
      setActionSuccess('AI Handover Report generated and persisted successfully!');
      fetchHandover();
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Failed to generate AI handover report.');
    } finally {
      setGeneratingAi(false);
    }
  };

  const handleAcknowledge = async () => {
    setAcknowledging(true);
    setActionSuccess(null);
    setError(null);
    try {
      await handoverApi.acknowledgeHandover(id, {
        notes: 'I have reviewed the handover materials, tasks, and historical decisions.'
      });
      setActionSuccess('Handover acknowledged successfully! Readiness score has increased.');
      fetchHandover();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to acknowledge handover.');
    } finally {
      setAcknowledging(false);
    }
  };

  const handleCompleteHandover = async () => {
    setCompleting(true);
    setActionSuccess(null);
    setError(null);
    try {
      await handoverApi.completeHandover(id);
      setActionSuccess('🎉 Handover finalized and marked completed! All responsibilities cleanly transferred.');
      fetchHandover();
    } catch (err) {
      console.error(err);
      const errMsg = err?.response?.data?.message || err?.message || 'Handover cannot be completed yet.';
      setError(`Cannot complete handover: ${errMsg}`);
    } finally {
      setCompleting(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading handover package & calculating readiness..." />;
  if (!handover) return <AlertBanner type="error" message="Handover not found." />;

  const currentScore = readiness?.score ?? handover?.readinessScore ?? 0;
  const isEligibleToComplete = currentScore >= 75 && readiness?.isReadyForCompletion !== false;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <Button
          variant="outline"
          size="sm"
          icon={ArrowLeft}
          onClick={() => navigate('/handovers')}
        >
          Back to Handovers
        </Button>

        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <Button
            size="sm"
            variant="outline"
            icon={RefreshCw}
            loading={recalculating}
            onClick={handleRecalculateScore}
          >
            Recalculate Score
          </Button>

          {handover.status !== 'COMPLETED' && (
            <Button
              size="sm"
              variant="outline"
              icon={UserCheck}
              loading={acknowledging}
              onClick={handleAcknowledge}
            >
              Acknowledge Receipt
            </Button>
          )}

          {handover.status !== 'COMPLETED' && (
            <Button
              size="sm"
              variant={isEligibleToComplete ? 'success' : 'outline'}
              icon={isEligibleToComplete ? CheckCircle2 : Lock}
              loading={completing}
              onClick={handleCompleteHandover}
            >
              {isEligibleToComplete ? 'Finalize & Complete Handover' : 'Finalize (Requires 75%+ Readiness)'}
            </Button>
          )}
        </div>
      </div>

      {actionSuccess && <AlertBanner type="success" message={actionSuccess} />}
      {error && <AlertBanner type="error" message={error} />}

      {/* Main Handover Overview Card */}
      <div style={{
        background: 'var(--surface-color)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '24px',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '14px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
              <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                {handover.projectName || 'Project Continuity'}
              </span>
              <HandoverStatusBadge status={handover.status} />
            </div>
            <h1 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
              {handover.title}
            </h1>
          </div>
        </div>

        {/* Transfer Route */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '16px',
          padding: '16px',
          borderRadius: '8px',
          background: 'var(--bg-secondary)',
          margin: '12px 0'
        }}>
          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Transferor (Departing)</span>
            <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>{handover.departingEmployeeName}</strong>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Receiver (Incoming)</span>
            <strong style={{ fontSize: '14px', color: 'var(--text-primary)' }}>{handover.receivingEmployeeName}</strong>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Reason</span>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>{handover.handoverReason}</span>
          </div>

          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Target Date</span>
            <span style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>{handover.targetDate}</span>
          </div>
        </div>

        {handover.specialInstructions && (
          <div style={{ marginTop: '12px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <strong>Special Instructions:</strong> {handover.specialInstructions}
          </div>
        )}
      </div>

      {/* STANDOUT INNOVATION: Handover Readiness Score & 7-Criteria Checklist */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
        {/* Readiness Gauge Meter */}
        <ReadinessScoreMeter
          score={currentScore}
          level={readiness?.level}
          summary={readiness?.summary}
          canFinalize={isEligibleToComplete}
        />

        {/* Detailed 7-Criteria Breakdown */}
        <ReadinessChecklistView
          criteria={readiness?.criteria || []}
          missingRequirements={readiness?.missingRequirements || []}
        />
      </div>

      {/* AI Handover Report Section */}
      <Card
        title="✨ AI-Generated Comprehensive Handover Package"
        subtitle="Automatic synthesis of project status, completed milestones, blockers, and next steps"
        actions={
          <Button
            size="sm"
            variant="primary"
            icon={Sparkles}
            loading={generatingAi}
            onClick={handleGenerateAiReport}
          >
            {handover.aiSummary ? 'Regenerate AI Report' : 'Generate AI Handover Report'}
          </Button>
        }
      >
        {generatingAi ? (
          <AiThinkingIndicator title="Synthesizing Knowledge & Task History into Handover Package..." />
        ) : handover.aiSummary ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{
              background: '#f8fafc',
              border: '1px solid var(--border-color)',
              borderRadius: '8px',
              padding: '16px',
              fontSize: '13.5px',
              lineHeight: 1.6,
              color: 'var(--text-primary)',
              whiteSpace: 'pre-wrap'
            }}>
              {handover.aiSummary}
            </div>

            {handover.recommendedNextSteps && (
              <div style={{
                background: '#eff6ff',
                border: '1px solid #bfdbfe',
                borderRadius: '8px',
                padding: '14px 18px'
              }}>
                <h4 style={{ margin: '0 0 6px', fontSize: '13.5px', fontWeight: 700, color: '#1e40af' }}>
                  💡 AI Recommended Next Steps for {handover.receivingEmployeeName}
                </h4>
                <p style={{ margin: 0, fontSize: '13px', color: '#1e3a8a', lineHeight: 1.5, whiteSpace: 'pre-wrap' }}>
                  {handover.recommendedNextSteps}
                </p>
              </div>
            )}
          </div>
        ) : (
          <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)' }}>
            <Sparkles size={32} style={{ margin: '0 auto 8px', color: 'var(--primary-color)' }} />
            <p style={{ fontWeight: 600, color: 'var(--text-primary)', margin: 0 }}>
              No AI Handover Report generated yet.
            </p>
            <p style={{ fontSize: '13px', margin: '4px 0 14px' }}>
              Click the button above to have the AI compile a 9-point continuity report from project records.
            </p>
            <Button
              variant="primary"
              size="sm"
              icon={Sparkles}
              loading={generatingAi}
              onClick={handleGenerateAiReport}
            >
              Generate AI Report Now
            </Button>
          </div>
        )}
      </Card>

      {/* Included Tasks */}
      <Card
        title={`📋 Associated Deliverables & Tasks (${handover.tasks?.length || 0})`}
        subtitle="Responsibilities being transitioned to the receiving employee"
      >
        {(!handover.tasks || handover.tasks.length === 0) ? (
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
            No specific tasks attached to this handover.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {handover.tasks.map((ht) => (
              <div
                key={ht.id}
                onClick={() => navigate(`/tasks/${ht.taskId}`)}
                style={{
                  padding: '12px 14px',
                  borderRadius: 'var(--radius-sm)',
                  border: '1px solid var(--border-color)',
                  background: 'var(--surface-color)',
                  cursor: 'pointer',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}
              >
                <div>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)' }}>
                    {ht.taskCode || `#${ht.taskId}`}
                  </span>
                  <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginLeft: '8px' }}>
                    {ht.taskTitle}
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Badge variant={ht.status === 'COMPLETED' ? 'success' : 'warning'}>
                    {ht.status}
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
