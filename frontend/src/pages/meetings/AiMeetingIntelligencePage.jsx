import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { aiApi, meetingApi, projectApi, userApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import AlertBanner from '../../components/common/AlertBanner';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AiThinkingIndicator from '../../components/ai/AiThinkingIndicator';
import EditableActionItemCard from '../../components/ai/EditableActionItemCard';
import EditableDecisionCard from '../../components/ai/EditableDecisionCard';
import {
  Sparkles,
  Save,
  CheckCircle,
  FileText,
  AlertTriangle,
  Lightbulb,
  ShieldCheck,
  ListTodo,
  Plus,
  RefreshCw
} from 'lucide-react';

const SAMPLE_1 = `We discussed the new employee onboarding portal. Rahul will complete the login module by Friday. Priya will prepare the database documentation. The manager approved the new authentication design. The API integration is still pending.`;

const SAMPLE_2 = `Discussed AWS multi-region cloud migration. Amit committed to finalizing Terraform modules by Tuesday. Neha will review security group rules and IAM policies. Decision: adopt ECS Fargate over EKS for simpler maintenance and cost predictability. Risk: CloudWatch billing might exceed initial budget if log retention is left indefinite. Unresolved: need client approval for VPC peering window.`;

export default function AiMeetingIntelligencePage() {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const initialMeetingId = queryParams.get('meetingId') || '';
  const initialProjectId = queryParams.get('projectId') || '';

  const [projects, setProjects] = useState([]);
  const [meetings, setMeetings] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(initialProjectId);
  const [selectedMeetingId, setSelectedMeetingId] = useState(initialMeetingId);
  const [rawNotes, setRawNotes] = useState(SAMPLE_1);

  const [analyzing, setAnalyzing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [aiResult, setAiResult] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [projRes, meetRes] = await Promise.all([
        projectApi.getProjects(),
        meetingApi.getMeetings()
      ]);
      setProjects(projRes.data || []);
      setMeetings(meetRes.data?.content || meetRes.data || []);
      if (!selectedProjectId && projRes.data?.length > 0) {
        setSelectedProjectId(projRes.data[0].id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleAnalyze = async () => {
    if (!rawNotes.trim()) {
      setErrorMessage('Please provide meeting notes or transcripts to analyze.');
      return;
    }
    setErrorMessage(null);
    setSuccessMessage(null);
    setAnalyzing(true);
    try {
      const res = await aiApi.analyzeMeetingNotes({
        rawNotes,
        projectId: selectedProjectId ? Number(selectedProjectId) : null,
        meetingId: selectedMeetingId ? Number(selectedMeetingId) : null
      });
      setAiResult(res.data);
    } catch (err) {
      console.error('AI Analysis failed:', err);
      setErrorMessage(err?.response?.data?.message || 'AI analysis failed. Using fallback processor.');
    } finally {
      setAnalyzing(false);
    }
  };

  // State update handlers for editable cards
  const handleUpdateActionItem = (index, updatedItem) => {
    if (!aiResult) return;
    const newItems = [...(aiResult.actionItems || [])];
    newItems[index] = updatedItem;
    setAiResult({ ...aiResult, actionItems: newItems });
  };

  const handleDeleteActionItem = (index) => {
    if (!aiResult) return;
    const newItems = (aiResult.actionItems || []).filter((_, i) => i !== index);
    setAiResult({ ...aiResult, actionItems: newItems });
  };

  const handleAddActionItem = () => {
    if (!aiResult) return;
    const newItems = [
      ...(aiResult.actionItems || []),
      {
        title: 'New action item',
        suggestedOwner: '',
        priority: 'MEDIUM',
        suggestedDeadline: '',
        confirmed: true
      }
    ];
    setAiResult({ ...aiResult, actionItems: newItems });
  };

  const handleUpdateDecision = (index, updatedDec) => {
    if (!aiResult) return;
    const newDecs = [...(aiResult.decisions || [])];
    newDecs[index] = updatedDec;
    setAiResult({ ...aiResult, decisions: newDecs });
  };

  const handleDeleteDecision = (index) => {
    if (!aiResult) return;
    const newDecs = (aiResult.decisions || []).filter((_, i) => i !== index);
    setAiResult({ ...aiResult, decisions: newDecs });
  };

  const handleAddDecision = () => {
    if (!aiResult) return;
    const newDecs = [
      ...(aiResult.decisions || []),
      {
        decisionText: 'New key decision',
        rationale: 'Discussed and agreed in team sync',
        impactedArea: 'Architecture',
        status: 'Agreed'
      }
    ];
    setAiResult({ ...aiResult, decisions: newDecs });
  };

  const handleApproveAndSave = async () => {
    if (!aiResult) return;
    setSaving(true);
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      const payload = {
        meetingId: selectedMeetingId ? Number(selectedMeetingId) : null,
        projectId: selectedProjectId ? Number(selectedProjectId) : (projects[0]?.id || 1),
        summary: aiResult.summary,
        keyDiscussionPoints: aiResult.keyDiscussionPoints,
        decisions: aiResult.decisions,
        actionItems: aiResult.actionItems,
        risksAndQuestions: aiResult.risksAndQuestions
      };
      const res = await aiApi.approveMeetingIntelligence(payload);
      setSuccessMessage(
        `Successfully saved! Created ${res.data?.createdTasksCount ?? aiResult.actionItems?.length} tasks and ${res.data?.createdDecisionsCount ?? aiResult.decisions?.length} decisions in MySQL.`
      );
    } catch (err) {
      console.error(err);
      setErrorMessage(err?.response?.data?.message || 'Failed to save approved intelligence to database.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
          <Sparkles size={20} color="var(--primary-color)" />
          <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Core AI Intelligence Engine
          </span>
        </div>
        <h2 style={{ fontSize: '26px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          AI Meeting Intelligence
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
          Transform unstructured meeting notes and discussions into verified decisions, tasks, and risk assessments.
        </p>
      </div>

      {successMessage && <AlertBanner type="success" message={successMessage} />}
      {errorMessage && <AlertBanner type="error" message={errorMessage} />}

      {/* Input Configuration Card */}
      <Card
        title="📝 Raw Meeting Notes & Discussion Transcript"
        subtitle="Paste notes or choose a realistic sample to inspect AI reasoning"
        actions={
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setRawNotes(SAMPLE_1)}
            >
              Load Sample 1 (Onboarding)
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setRawNotes(SAMPLE_2)}
            >
              Load Sample 2 (Cloud Migration)
            </Button>
          </div>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Associate with Project
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

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Associate with Existing Meeting (Optional)
              </label>
              <select
                value={selectedMeetingId}
                onChange={(e) => setSelectedMeetingId(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              >
                <option value="">-- Standalone Meeting Notes --</option>
                {meetings.map((m) => (
                  <option key={m.id} value={m.id}>{m.title} ({m.meetingDate})</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Meeting Notes / Transcript *
            </label>
            <textarea
              rows={6}
              value={rawNotes}
              onChange={(e) => setRawNotes(e.target.value)}
              placeholder="Paste raw discussion notes, meeting bullet points, or conversation transcript..."
              style={{
                width: '100%',
                padding: '12px',
                borderRadius: '8px',
                border: '1px solid var(--border-color)',
                fontSize: '13.5px',
                lineHeight: 1.6,
                fontFamily: 'inherit'
              }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              variant="primary"
              size="lg"
              icon={Sparkles}
              loading={analyzing}
              onClick={handleAnalyze}
            >
              Analyze with AI Intelligence
            </Button>
          </div>
        </div>
      </Card>

      {/* Analyzing Animated State */}
      {analyzing && <AiThinkingIndicator title="Extracting Intelligence from Meeting Notes..." />}

      {/* AI Structured Results */}
      {aiResult && !analyzing && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Top Approval Banner */}
          <div style={{
            background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            borderRadius: 'var(--radius-md)',
            padding: '16px 20px',
            color: 'white',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '12px'
          }}>
            <div>
              <h4 style={{ margin: 0, fontSize: '16px', fontWeight: 700 }}>
                Structured Knowledge Extracted
              </h4>
              <p style={{ margin: '4px 0 0', fontSize: '12.5px', opacity: 0.9 }}>
                Review and modify action items, assignees, and decisions below before saving into MySQL.
              </p>
            </div>
            <Button
              variant="light"
              icon={Save}
              loading={saving}
              onClick={handleApproveAndSave}
            >
              Approve & Save to Database
            </Button>
          </div>

          {/* Summary & Discussion Points */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
            <Card title="📑 Executive Summary">
              <textarea
                value={aiResult.summary || ''}
                onChange={(e) => setAiResult({ ...aiResult, summary: e.target.value })}
                rows={4}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '6px',
                  border: '1px solid var(--border-color)',
                  fontSize: '13.5px',
                  lineHeight: 1.6
                }}
              />
            </Card>

            <Card title="💡 Key Discussion Points">
              <ul style={{ margin: 0, paddingLeft: '18px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {(aiResult.keyDiscussionPoints || []).map((pt, idx) => (
                  <li key={idx} style={{ fontSize: '13.5px', color: 'var(--text-primary)', lineHeight: 1.5 }}>
                    {pt}
                  </li>
                ))}
              </ul>
            </Card>
          </div>

          {/* Formal Decisions */}
          <Card
            title={`🛡️ Decisions (${aiResult.decisions?.length || 0})`}
            subtitle="Agreed architectural, scope, and process decisions"
            actions={
              <Button size="sm" variant="outline" icon={Plus} onClick={handleAddDecision}>
                Add Decision
              </Button>
            }
          >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '14px' }}>
              {(aiResult.decisions || []).map((dec, idx) => (
                <EditableDecisionCard
                  key={idx}
                  decision={dec}
                  onUpdate={(updated) => handleUpdateDecision(idx, updated)}
                  onDelete={() => handleDeleteDecision(idx)}
                />
              ))}
            </div>
          </Card>

          {/* Action Items & Suggested Tasks */}
          <Card
            title={`✅ Action Items & Task Assignments (${aiResult.actionItems?.length || 0})`}
            subtitle="Editable cards with suggested owner, deadline, and priority (Confirmation required)"
            actions={
              <Button size="sm" variant="outline" icon={Plus} onClick={handleAddActionItem}>
                Add Action Item
              </Button>
            }
          >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '14px' }}>
              {(aiResult.actionItems || []).map((item, idx) => (
                <EditableActionItemCard
                  key={idx}
                  item={item}
                  onUpdate={(updated) => handleUpdateActionItem(idx, updated)}
                  onDelete={() => handleDeleteActionItem(idx)}
                />
              ))}
            </div>
          </Card>

          {/* Risks & Unresolved Questions */}
          <Card title="⚠️ Risks & Unresolved Questions">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {(aiResult.risksAndQuestions || []).map((risk, idx) => (
                <div
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    padding: '10px 14px',
                    borderRadius: '6px',
                    background: '#fffbeb',
                    border: '1px solid #fde68a',
                    fontSize: '13px',
                    color: '#92400e'
                  }}
                >
                  <AlertTriangle size={16} color="#d97706" />
                  <span>{risk}</span>
                </div>
              ))}
            </div>
          </Card>

          {/* Bottom Action Footer */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', paddingBottom: '24px' }}>
            <Button
              variant="outline"
              onClick={() => navigate('/meetings')}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="lg"
              icon={Save}
              loading={saving}
              onClick={handleApproveAndSave}
            >
              Approve & Save All to Database
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
