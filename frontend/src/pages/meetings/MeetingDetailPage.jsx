import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { meetingApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  Calendar,
  Clock,
  Users,
  Sparkles,
  ArrowLeft,
  CheckCircle2,
  FileText,
  ListTodo,
  ShieldCheck,
  AlertCircle
} from 'lucide-react';

export default function MeetingDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [meeting, setMeeting] = useState(null);
  const [decisions, setDecisions] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updatingStatus, setUpdatingStatus] = useState(false);

  useEffect(() => {
    fetchMeetingData();
  }, [id]);

  const fetchMeetingData = async () => {
    setLoading(true);
    try {
      const [meetingRes, decisionsRes, tasksRes] = await Promise.all([
        meetingApi.getMeeting(id),
        meetingApi.getMeetingDecisions(id),
        meetingApi.getMeetingTasks(id)
      ]);
      setMeeting(meetingRes.data);
      setDecisions(decisionsRes.data || []);
      setTasks(tasksRes.data || []);
    } catch (err) {
      console.error(err);
      setError('Could not load meeting details.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    setUpdatingStatus(true);
    try {
      await meetingApi.updateMeetingStatus(id, newStatus);
      fetchMeetingData();
    } catch (err) {
      alert('Failed to update status');
    } finally {
      setUpdatingStatus(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading meeting details..." />;
  if (!meeting) return <AlertBanner type="error" message="Meeting not found" />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Top Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <Button
          variant="outline"
          size="sm"
          icon={ArrowLeft}
          onClick={() => navigate('/meetings')}
        >
          Back to Meetings
        </Button>

        <div style={{ display: 'flex', gap: '10px' }}>
          <Button
            variant="primary"
            icon={Sparkles}
            onClick={() => navigate(`/meetings/ai-intelligence?meetingId=${meeting.id}&projectId=${meeting.projectId}`)}
          >
            Run AI Intelligence on Notes
          </Button>
        </div>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Meeting Header Card */}
      <div style={{
        background: 'var(--surface-color)',
        border: '1px solid var(--border-color)',
        borderRadius: 'var(--radius-lg)',
        padding: '24px',
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', marginBottom: '12px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                {meeting.projectName || 'Project Meeting'}
              </span>
              <Badge variant={meeting.status === 'COMPLETED' ? 'success' : meeting.status === 'SCHEDULED' ? 'primary' : 'warning'}>
                {meeting.status}
              </Badge>
            </div>
            <h1 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
              {meeting.title}
            </h1>
          </div>

          {/* Status Progression Controls */}
          <div style={{ display: 'flex', gap: '8px' }}>
            {meeting.status === 'SCHEDULED' && (
              <Button
                size="sm"
                variant="outline"
                loading={updatingStatus}
                onClick={() => handleStatusChange('IN_PROGRESS')}
              >
                Start Meeting
              </Button>
            )}
            {meeting.status === 'IN_PROGRESS' && (
              <Button
                size="sm"
                variant="primary"
                loading={updatingStatus}
                onClick={() => handleStatusChange('COMPLETED')}
              >
                Mark Completed
              </Button>
            )}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', borderTop: '1px solid var(--border-color)', paddingTop: '16px', color: 'var(--text-secondary)', fontSize: '13px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Calendar size={16} color="var(--primary-color)" />
            <span>Date: <strong>{meeting.meetingDate}</strong></span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Clock size={16} color="var(--primary-color)" />
            <span>Time: <strong>{meeting.startTime || '10:00'} - {meeting.endTime || '11:00'}</strong></span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Users size={16} color="var(--primary-color)" />
            <span>Organizer: <strong>{meeting.organizerName || 'Manager'}</strong></span>
          </div>
        </div>
      </div>

      {/* Agenda & Notes */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '20px' }}>
        <Card title="📋 Meeting Agenda">
          <p style={{ fontSize: '14px', lineHeight: 1.6, color: 'var(--text-secondary)', whiteSpace: 'pre-wrap' }}>
            {meeting.agenda || 'No agenda recorded.'}
          </p>
        </Card>

        <Card title="📝 Discussion Notes">
          <p style={{ fontSize: '14px', lineHeight: 1.6, color: 'var(--text-secondary)', whiteSpace: 'pre-wrap' }}>
            {meeting.meetingNotes || 'No notes taken yet. Use AI Meeting Intelligence to transcribe and summarize.'}
          </p>
        </Card>
      </div>

      {/* Decisions Log */}
      <Card
        title={`🛡️ Recorded Decisions (${decisions.length})`}
        subtitle="Architectural, product, and timeline decisions formalized in this meeting"
      >
        {decisions.length === 0 ? (
          <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            No formal decisions logged yet. Run AI Meeting Intelligence to automatically capture decisions.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {decisions.map((dec) => (
              <div
                key={dec.id}
                style={{
                  padding: '14px',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-color)',
                  background: 'var(--bg-secondary)',
                  borderLeft: '4px solid var(--success-color)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '6px' }}>
                  <p style={{ fontWeight: 600, fontSize: '14px', color: 'var(--text-primary)', margin: 0 }}>
                    {dec.decisionText}
                  </p>
                  <span style={{ fontSize: '11px', padding: '2px 8px', borderRadius: '999px', background: '#ecfdf5', color: '#065f46', fontWeight: 600 }}>
                    {dec.status}
                  </span>
                </div>
                {dec.rationale && (
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', margin: '4px 0 0', fontStyle: 'italic' }}>
                    <strong>Rationale:</strong> {dec.rationale}
                  </p>
                )}
                {dec.impactedArea && (
                  <span style={{ display: 'inline-block', marginTop: '6px', fontSize: '11px', color: 'var(--text-muted)' }}>
                    Impacted Area: {dec.impactedArea}
                  </span>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Action Items / Associated Tasks */}
      <Card
        title={`✅ Action Items & Tasks (${tasks.length})`}
        subtitle="Work assignments derived from this meeting's discussions"
      >
        {tasks.length === 0 ? (
          <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            No tasks created from this meeting yet.
          </p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {tasks.map((t) => (
              <div
                key={t.id}
                onClick={() => navigate(`/tasks/${t.id}`)}
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
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-muted)' }}>{t.taskCode || `#${t.id}`}</span>
                    <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>{t.title}</span>
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                    Assignee: <strong>{t.assignedEmployeeName || 'Unassigned'}</strong> | Due: {t.dueDate || 'Unscheduled'}
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Badge variant={t.priority === 'CRITICAL' ? 'danger' : t.priority === 'HIGH' ? 'warning' : 'primary'}>
                    {t.priority}
                  </Badge>
                  <Badge variant={t.status === 'COMPLETED' ? 'success' : t.status === 'IN_PROGRESS' ? 'info' : 'default'}>
                    {t.status}
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
