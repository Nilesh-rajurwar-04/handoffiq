import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { taskApi, userApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  ArrowLeft,
  Calendar,
  Clock,
  User,
  CheckCircle2,
  AlertTriangle,
  ArrowRightLeft,
  MessageSquare,
  History,
  FileCheck,
  Send,
  Sliders
} from 'lucide-react';

export default function TaskDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [task, setTask] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // New Comment
  const [newComment, setNewComment] = useState('');
  const [postingComment, setPostingComment] = useState(false);

  // Reassign Modal
  const [isReassignModalOpen, setIsReassignModalOpen] = useState(false);
  const [selectedNewAssignee, setSelectedNewAssignee] = useState('');
  const [reassigning, setReassigning] = useState(false);

  // Outcome Modal
  const [isOutcomeModalOpen, setIsOutcomeModalOpen] = useState(false);
  const [outcomeDescription, setOutcomeDescription] = useState('Completed successfully.');
  const [completing, setCompleting] = useState(false);

  // Carry Forward Modal
  const [isCarryModalOpen, setIsCarryModalOpen] = useState(false);
  const [carryReason, setCarryReason] = useState('Carried forward to next milestone.');
  const [carrying, setCarrying] = useState(false);

  useEffect(() => {
    fetchTask();
  }, [id]);

  const fetchTask = async () => {
    setLoading(true);
    try {
      const [taskRes, usersRes] = await Promise.all([
        taskApi.getTask(id),
        userApi.getUsers()
      ]);
      setTask(taskRes.data);
      setUsers(usersRes.data || []);
      setSelectedNewAssignee(taskRes.data?.assignedEmployeeId || '');
    } catch (err) {
      console.error(err);
      setError('Could not load task details.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus) => {
    try {
      await taskApi.updateTaskStatus(id, newStatus, `Status updated to ${newStatus}`);
      fetchTask();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to update task status.');
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    setPostingComment(true);
    try {
      await taskApi.addTaskComment(id, { commentText: newComment });
      setNewComment('');
      fetchTask();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to post comment.');
    } finally {
      setPostingComment(false);
    }
  };

  const handleReassign = async () => {
    if (!selectedNewAssignee) return;
    setReassigning(true);
    try {
      await taskApi.reassignTask(id, selectedNewAssignee, 'Reassigned via Task Details');
      setIsReassignModalOpen(false);
      fetchTask();
    } catch (err) {
      alert('Failed to reassign task.');
    } finally {
      setReassigning(false);
    }
  };

  const handleComplete = async () => {
    setCompleting(true);
    try {
      await taskApi.completeTask(id, {
        outcomeDescription: outcomeDescription,
        completionNotes: 'Verified and signed off.'
      });
      setIsOutcomeModalOpen(false);
      fetchTask();
    } catch (err) {
      alert('Failed to complete task.');
    } finally {
      setCompleting(false);
    }
  };

  const handleCarryForward = async () => {
    setCarrying(true);
    try {
      await taskApi.carryForwardTask(id, { reason: carryReason });
      setIsCarryModalOpen(false);
      fetchTask();
    } catch (err) {
      alert('Failed to carry forward.');
    } finally {
      setCarrying(false);
    }
  };

  if (loading) return <LoadingSpinner text="Loading task..." />;
  if (!task) return <AlertBanner type="error" message="Task not found." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Top Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <Button
          variant="outline"
          size="sm"
          icon={ArrowLeft}
          onClick={() => navigate('/tasks')}
        >
          Back to Tasks
        </Button>

        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            size="sm"
            variant="outline"
            icon={User}
            onClick={() => setIsReassignModalOpen(true)}
          >
            Reassign
          </Button>
          {task.status !== 'COMPLETED' && (
            <>
              <Button
                size="sm"
                variant="outline"
                icon={ArrowRightLeft}
                onClick={() => setIsCarryModalOpen(true)}
              >
                Carry Forward
              </Button>
              <Button
                size="sm"
                variant="success"
                icon={CheckCircle2}
                onClick={() => setIsOutcomeModalOpen(true)}
              >
                Record Outcome & Complete
              </Button>
            </>
          )}
        </div>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Task Header Card */}
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
              <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--primary-color)' }}>
                {task.taskCode || `#${task.id}`}
              </span>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>•</span>
              <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                {task.projectName || 'General Project'}
              </span>
            </div>
            <h1 style={{ fontSize: '22px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
              {task.title}
            </h1>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Badge variant={task.priority === 'CRITICAL' ? 'danger' : task.priority === 'HIGH' ? 'warning' : 'primary'}>
              {task.priority}
            </Badge>
            <Badge variant={task.status === 'COMPLETED' ? 'success' : task.status === 'IN_PROGRESS' ? 'info' : task.status === 'BLOCKED' ? 'danger' : 'default'}>
              {task.status?.replace('_', ' ')}
            </Badge>
          </div>
        </div>

        {/* Quick Status Buttons */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 0', borderTop: '1px solid var(--border-color)', borderBottom: '1px solid var(--border-color)' }}>
          <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-muted)' }}>Update Status:</span>
          {['TO_DO', 'IN_PROGRESS', 'BLOCKED'].map((st) => (
            <button
              key={st}
              disabled={task.status === st}
              onClick={() => handleStatusChange(st)}
              style={{
                fontSize: '12px',
                padding: '4px 10px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                background: task.status === st ? 'var(--primary-color)' : 'var(--bg-secondary)',
                color: task.status === st ? '#ffffff' : 'var(--text-primary)',
                fontWeight: 600,
                cursor: task.status === st ? 'default' : 'pointer'
              }}
            >
              {st.replace('_', ' ')}
            </button>
          ))}
        </div>

        {/* Key Metadata Row */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', marginTop: '16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Assignee</span>
            <strong style={{ color: 'var(--text-primary)' }}>{task.assignedEmployeeName || 'Unassigned'}</strong>
          </div>
          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Created By</span>
            <strong style={{ color: 'var(--text-primary)' }}>{task.createdByName || 'System'}</strong>
          </div>
          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Start Date</span>
            <strong style={{ color: 'var(--text-primary)' }}>{task.startDate || 'Not set'}</strong>
          </div>
          <div>
            <span style={{ display: 'block', fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>Due Date</span>
            <strong style={{ color: 'var(--text-primary)' }}>{task.dueDate || 'Not set'}</strong>
          </div>
        </div>
      </div>

      {/* Description & Recorded Outcome */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
        <Card title="📄 Description & Acceptance Criteria">
          <p style={{ fontSize: '13.5px', lineHeight: 1.6, color: 'var(--text-secondary)', whiteSpace: 'pre-wrap', margin: 0 }}>
            {task.description || 'No detailed description provided.'}
          </p>
        </Card>

        <Card title="🎯 Recorded Task Outcome">
          {task.outcome ? (
            <div style={{
              background: '#ecfdf5',
              border: '1px solid #a7f3d0',
              borderRadius: 'var(--radius-md)',
              padding: '14px',
              display: 'flex',
              flexDirection: 'column',
              gap: '6px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#065f46', fontWeight: 700, fontSize: '13px' }}>
                <CheckCircle2 size={16} />
                <span>Outcome Verified</span>
              </div>
              <p style={{ fontSize: '13px', color: '#064e3b', margin: 0 }}>
                {task.outcome.outcomeDescription}
              </p>
              {task.outcome.completionNotes && (
                <p style={{ fontSize: '12px', color: '#047857', margin: 0, fontStyle: 'italic' }}>
                  Notes: {task.outcome.completionNotes}
                </p>
              )}
              <span style={{ fontSize: '11px', color: '#059669', marginTop: '4px' }}>
                Completed by {task.outcome.completedByName || 'Assignee'} on {task.outcome.completedAt?.substring(0, 10)}
              </span>
            </div>
          ) : (
            <div style={{ padding: '16px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
              <Clock size={28} style={{ margin: '0 auto 6px', color: 'var(--text-muted)' }} />
              <p style={{ margin: 0, fontWeight: 500 }}>No outcome recorded yet.</p>
              <p style={{ margin: '4px 0 0', fontSize: '12px' }}>When this task is completed, an outcome description will be documented here.</p>
            </div>
          )}
        </Card>
      </div>

      {/* Audit Trails: Carry-Forward History & Status Timeline */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
        {/* Carry-Forward Audit Trail */}
        <Card
          title={`🔄 Carry-Forward History (${task.carryForwardHistories?.length || 0})`}
          subtitle="Full lineage of deferred action items preserved across meetings"
        >
          {(!task.carryForwardHistories || task.carryForwardHistories.length === 0) ? (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
              This task was not carried forward; it originated in its current iteration.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {task.carryForwardHistories.map((cf, idx) => (
                <div
                  key={idx}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '6px',
                    border: '1px solid #fed7aa',
                    background: '#fff7ed',
                    fontSize: '12.5px'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#9a3412', fontWeight: 600 }}>
                    <span>Carried forward by {cf.carriedByName || 'Manager'}</span>
                    <span>{cf.carriedAt?.substring(0, 10)}</span>
                  </div>
                  <p style={{ margin: '4px 0 0', color: '#7c2d12' }}>
                    <strong>Reason:</strong> {cf.reason}
                  </p>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* Status History Timeline */}
        <Card
          title={`⏱️ Status Transitions (${task.statusHistories?.length || 0})`}
          subtitle="Audit log of state changes"
        >
          {(!task.statusHistories || task.statusHistories.length === 0) ? (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
              No status transitions logged.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {task.statusHistories.map((sh, idx) => (
                <div
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    fontSize: '12.5px',
                    padding: '6px 0',
                    borderBottom: '1px solid var(--border-color)'
                  }}
                >
                  <div>
                    <span style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                      {sh.previousStatus || 'CREATED'} → {sh.newStatus}
                    </span>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block' }}>
                      by {sh.changedByName || 'User'} {sh.changeReason ? `(${sh.changeReason})` : ''}
                    </span>
                  </div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    {sh.changedAt?.substring(0, 16).replace('T', ' ')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* Discussion & Comments */}
      <Card
        title={`💬 Team Discussion & Updates (${task.comments?.length || 0})`}
        subtitle="Collaborative commentary and blockers"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {(!task.comments || task.comments.length === 0) ? (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', margin: 0 }}>
              No comments yet. Post an update below.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {task.comments.map((c) => (
                <div
                  key={c.id}
                  style={{
                    padding: '12px 14px',
                    borderRadius: 'var(--radius-sm)',
                    background: 'var(--bg-secondary)',
                    border: '1px solid var(--border-color)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                    <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-primary)' }}>
                      {c.authorName || 'Team Member'}
                    </span>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                      {c.createdAt?.substring(0, 16).replace('T', ' ')}
                    </span>
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0, whiteSpace: 'pre-wrap' }}>
                    {c.commentText}
                  </p>
                </div>
              ))}
            </div>
          )}

          {/* Add Comment Input */}
          <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Write a comment or post an update..."
              style={{
                flex: 1,
                padding: '9px 12px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                fontSize: '13px'
              }}
            />
            <Button
              type="submit"
              variant="primary"
              icon={Send}
              loading={postingComment}
            >
              Post
            </Button>
          </form>
        </div>
      </Card>

      {/* Modal: Reassign */}
      <Modal
        isOpen={isReassignModalOpen}
        onClose={() => setIsReassignModalOpen(false)}
        title="Reassign Task Responsibility"
        maxWidth="440px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Select New Assignee
            </label>
            <select
              value={selectedNewAssignee}
              onChange={(e) => setSelectedNewAssignee(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">-- Unassigned --</option>
              {users.map((u) => (
                <option key={u.id} value={u.id}>{u.fullName} ({u.jobTitle || 'Team'})</option>
              ))}
            </select>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <Button variant="outline" onClick={() => setIsReassignModalOpen(false)}>Cancel</Button>
            <Button variant="primary" loading={reassigning} onClick={handleReassign}>Confirm Reassignment</Button>
          </div>
        </div>
      </Modal>

      {/* Modal: Record Outcome */}
      <Modal
        isOpen={isOutcomeModalOpen}
        onClose={() => setIsOutcomeModalOpen(false)}
        title="Sign Off Task & Document Outcome"
        maxWidth="480px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Outcome Description *
            </label>
            <textarea
              rows={3}
              value={outcomeDescription}
              onChange={(e) => setOutcomeDescription(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <Button variant="outline" onClick={() => setIsOutcomeModalOpen(false)}>Cancel</Button>
            <Button variant="success" icon={CheckCircle2} loading={completing} onClick={handleComplete}>
              Sign Off & Complete
            </Button>
          </div>
        </div>
      </Modal>

      {/* Modal: Carry Forward */}
      <Modal
        isOpen={isCarryModalOpen}
        onClose={() => setIsCarryModalOpen(false)}
        title="Carry Forward Task"
        maxWidth="480px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Carry-Forward Reason *
            </label>
            <textarea
              rows={3}
              value={carryReason}
              onChange={(e) => setCarryReason(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <Button variant="outline" onClick={() => setIsCarryModalOpen(false)}>Cancel</Button>
            <Button variant="primary" icon={ArrowRightLeft} loading={carrying} onClick={handleCarryForward}>
              Carry Forward
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
