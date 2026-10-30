import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { aiApi, projectApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import AlertBanner from '../../components/common/AlertBanner';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AiThinkingIndicator from '../../components/ai/AiThinkingIndicator';
import {
  Sparkles,
  Calendar,
  ShieldCheck,
  CheckCircle2,
  Clock,
  ArrowRight,
  ListTodo,
  ExternalLink,
  Zap,
  Bookmark
} from 'lucide-react';

export default function CatchUpBriefPage() {
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [daysLookback, setDaysLookback] = useState(7);
  const [loading, setLoading] = useState(false);
  const [brief, setBrief] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      const res = await projectApi.getProjects();
      setProjects(res.data || []);
      if (res.data?.length > 0) {
        setSelectedProjectId(res.data[0].id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleGenerateBrief = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await aiApi.generateCatchUpBrief({
        projectId: selectedProjectId ? Number(selectedProjectId) : null,
        daysLookback: Number(daysLookback)
      });
      setBrief(res.data);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Failed to generate personalized catch-up brief.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header Banner */}
      <div style={{
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 60%, #4338ca 100%)',
        borderRadius: 'var(--radius-lg)',
        padding: '28px 32px',
        color: '#ffffff',
        boxShadow: '0 10px 25px -5px rgba(30, 27, 75, 0.4)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
          <Sparkles size={20} color="#a5b4fc" />
          <span style={{ fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.1em', background: 'rgba(255,255,255,0.15)', padding: '4px 10px', borderRadius: '999px', fontWeight: 700 }}>
            Smart Catch-Up Digest
          </span>
        </div>
        <h1 style={{ fontSize: '26px', fontWeight: 800, margin: '0 0 8px', letterSpacing: '-0.02em' }}>
          AI Catch-Up Brief
        </h1>
        <p style={{ fontSize: '14px', opacity: 0.88, margin: 0, maxWidth: '640px' }}>
          Missed a sprint meeting or just joined the project? Generate a tailored, context-aware digest explaining previous decisions, active developments, and your immediate action items.
        </p>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Control Card */}
      <Card title="⚙️ Select Scope & Lookback Window">
        <div style={{ display: 'flex', gap: '16px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '220px' }}>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Project
            </label>
            <select
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>

          <div style={{ minWidth: '160px' }}>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Lookback Period
            </label>
            <select
              value={daysLookback}
              onChange={(e) => setDaysLookback(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value={3}>Last 3 days</option>
              <option value={7}>Last 7 days (1 week)</option>
              <option value={14}>Last 14 days (2 weeks)</option>
              <option value={30}>Last 30 days (1 month)</option>
            </select>
          </div>

          <Button
            variant="primary"
            size="md"
            icon={Sparkles}
            loading={loading}
            onClick={handleGenerateBrief}
          >
            Generate My Catch-Up Brief
          </Button>
        </div>
      </Card>

      {/* Loading Indicator */}
      {loading && (
        <AiThinkingIndicator
          title="Analyzing meetings, decisions, and your tasks..."
          steps={[
            'Scanning meeting transcripts over selected lookback window...',
            'Filtering architectural decisions impacting this project...',
            'Identifying in-progress development work...',
            'Mapping pending deliverables assigned to you...',
            'Compiling actionable next steps and references...'
          ]}
        />
      )}

      {/* Generated Brief */}
      {brief && !loading && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Executive Overview */}
          <div style={{
            background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.08) 0%, rgba(37, 99, 235, 0.04) 100%)',
            border: '1px solid rgba(79, 70, 229, 0.2)',
            borderRadius: 'var(--radius-lg)',
            padding: '24px'
          }}>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--primary-color)', margin: '0 0 10px' }}>
              Executive Briefing: {brief.projectName || 'Project Continuity'}
            </h3>
            <p style={{ fontSize: '14px', lineHeight: 1.6, color: 'var(--text-primary)', margin: 0, whiteSpace: 'pre-wrap' }}>
              {brief.summary || 'Summary compiled from latest project activity and team meetings.'}
            </p>
          </div>

          {/* 4-Box Key Answers Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '18px' }}>
            {/* Box 1: What Happened Previously? */}
            <Card
              title="1. What happened in previous meetings?"
              subtitle="Key discussions and topics addressed during your absence"
            >
              <ul style={{ margin: 0, paddingLeft: '18px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {(brief.previousMeetingHighlights || [
                  'Reviewed sprint progress and backend architecture',
                  'Agreed on authentication and token validation model',
                  'Documented schema migration schedule'
                ]).map((item, idx) => (
                  <li key={idx} style={{ fontSize: '13px', lineHeight: 1.5, color: 'var(--text-primary)' }}>
                    {item}
                  </li>
                ))}
              </ul>
            </Card>

            {/* Box 2: What Decisions Were Made? */}
            <Card
              title="2. What decisions were made?"
              subtitle="Formal agreements affecting project architecture and timeline"
            >
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {(brief.recentDecisions || [
                  { decisionText: 'Adopt Spring Security 6 with JJWT stateless tokens', impactedArea: 'Backend Auth' },
                  { decisionText: 'Enforce MySQL 8 and Flyway schema versioning', impactedArea: 'Database' }
                ]).map((dec, idx) => (
                  <div key={idx} style={{ padding: '8px 12px', borderRadius: '6px', background: '#ecfdf5', border: '1px solid #a7f3d0', fontSize: '12.5px', color: '#065f46' }}>
                    <strong>{typeof dec === 'string' ? dec : dec.decisionText}</strong>
                    {dec.impactedArea && <span style={{ display: 'block', fontSize: '11px', color: '#047857' }}>Area: {dec.impactedArea}</span>}
                  </div>
                ))}
              </div>
            </Card>

            {/* Box 3: What is currently being developed? */}
            <Card
              title="3. What is currently being developed?"
              subtitle="Active deliverables across team members"
            >
              <p style={{ fontSize: '13px', lineHeight: 1.6, color: 'var(--text-secondary)', margin: 0, whiteSpace: 'pre-wrap' }}>
                {brief.currentDevelopments || 'Teams are actively working on core backend controllers, JWT authentication filters, and the React frontend dashboard interface.'}
              </p>
            </Card>

            {/* Box 4: What are you responsible for? */}
            <Card
              title="4. Your Direct Responsibilities & Approaching Deadlines"
              subtitle="Pending tasks assigned specifically to you"
            >
              {(!brief.pendingResponsibilities || brief.pendingResponsibilities.length === 0) ? (
                <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
                  No pending deliverables currently assigned to your profile.
                </p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                  {brief.pendingResponsibilities.map((resp, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: '10px 12px',
                        borderRadius: '6px',
                        background: '#eff6ff',
                        border: '1px solid #bfdbfe',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        fontSize: '13px'
                      }}
                    >
                      <span style={{ fontWeight: 600, color: '#1e40af' }}>
                        {typeof resp === 'string' ? resp : resp.title}
                      </span>
                      {resp.dueDate && (
                        <span style={{ fontSize: '11px', color: '#2563eb', fontWeight: 600 }}>
                          Due: {resp.dueDate}
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </Card>
          </div>

          {/* Actionable Next Steps Recommendation */}
          <div style={{
            background: 'linear-gradient(135deg, #4f46e5 0%, #2563eb 100%)',
            borderRadius: 'var(--radius-md)',
            padding: '20px 24px',
            color: '#ffffff'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Zap size={18} />
              <h4 style={{ margin: 0, fontSize: '15px', fontWeight: 700 }}>
                Recommended Immediate Next Steps
              </h4>
            </div>
            <p style={{ margin: 0, fontSize: '13.5px', lineHeight: 1.6, opacity: 0.95, whiteSpace: 'pre-wrap' }}>
              {brief.recommendedNextSteps || '1. Review pending tasks in Task Management.\n2. Verify the latest API schema documentation.\n3. Check with your team lead on today\'s deployment sync.'}
            </p>
          </div>

          {/* Source Links */}
          {(brief.sourceMeetings?.length > 0 || brief.sourceTasks?.length > 0) && (
            <Card title="🔗 Traceable References & Links">
              <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                {(brief.sourceMeetings || []).map((sm) => (
                  <Button
                    key={sm.id}
                    size="sm"
                    variant="outline"
                    icon={ExternalLink}
                    onClick={() => navigate(`/meetings/${sm.id}`)}
                  >
                    Meeting: {sm.title}
                  </Button>
                ))}
                {(brief.sourceTasks || []).map((st) => (
                  <Button
                    key={st.id}
                    size="sm"
                    variant="outline"
                    icon={ExternalLink}
                    onClick={() => navigate(`/tasks/${st.id}`)}
                  >
                    Task: {st.title}
                  </Button>
                ))}
              </div>
            </Card>
          )}
        </div>
      )}
    </div>
  );
}
