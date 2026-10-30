import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { handoverApi, projectApi, userApi, taskApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import AlertBanner from '../../components/common/AlertBanner';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import {
  ArrowRightLeft,
  ArrowLeft,
  CheckCircle2,
  Calendar,
  Users,
  FolderGit2,
  ListTodo,
  FileText
} from 'lucide-react';

export default function CreateHandoverPage() {
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    title: '',
    projectId: '',
    departingEmployeeId: '',
    receivingEmployeeId: '',
    handoverReason: 'Project Reassignment',
    targetDate: '',
    specialInstructions: '',
    selectedTaskIds: []
  });

  useEffect(() => {
    loadPrerequisites();
  }, []);

  useEffect(() => {
    if (formData.projectId) {
      loadProjectTasks(formData.projectId);
    }
  }, [formData.projectId]);

  const loadPrerequisites = async () => {
    setLoading(true);
    try {
      const [projRes, usersRes] = await Promise.all([
        projectApi.getProjects(),
        userApi.getUsers()
      ]);
      setProjects(projRes.data || []);
      setUsers(usersRes.data || []);
      if (projRes.data?.length > 0) {
        setFormData((prev) => ({ ...prev, projectId: projRes.data[0].id }));
      }
    } catch (err) {
      console.error(err);
      setError('Failed to load project and user dependencies.');
    } finally {
      setLoading(false);
    }
  };

  const loadProjectTasks = async (pId) => {
    try {
      const res = await taskApi.getTasks({ projectId: pId });
      setTasks(res.data?.content || res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const toggleTaskSelection = (taskId) => {
    setFormData((prev) => {
      const exists = prev.selectedTaskIds.includes(taskId);
      return {
        ...prev,
        selectedTaskIds: exists
          ? prev.selectedTaskIds.filter((id) => id !== taskId)
          : [...prev.selectedTaskIds, taskId]
      };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.departingEmployeeId || !formData.receivingEmployeeId) {
      setError('Please select both departing and receiving employees.');
      return;
    }
    if (formData.departingEmployeeId === formData.receivingEmployeeId) {
      setError('Departing employee and receiving employee must be different individuals.');
      return;
    }
    setError(null);
    setSubmitting(true);
    try {
      const payload = {
        title: formData.title || `Handover: ${projects.find((p) => String(p.id) === String(formData.projectId))?.name || 'Project'} Transfer`,
        projectId: Number(formData.projectId),
        departingEmployeeId: Number(formData.departingEmployeeId),
        receivingEmployeeId: Number(formData.receivingEmployeeId),
        handoverReason: formData.handoverReason,
        targetDate: formData.targetDate || new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
        specialInstructions: formData.specialInstructions,
        taskIds: formData.selectedTaskIds
      };
      const res = await handoverApi.createHandover(payload);
      navigate(`/handovers/${res.data.id}`);
    } catch (err) {
      console.error(err);
      setError(err?.response?.data?.message || 'Failed to initiate handover package.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <LoadingSpinner text="Preparing handover wizard..." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '860px', margin: '0 auto' }}>
      {/* Top Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <Button
          variant="outline"
          size="sm"
          icon={ArrowLeft}
          onClick={() => navigate('/handovers')}
        >
          Cancel
        </Button>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
            Initiate Employee Handover
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '2px 0 0' }}>
            Set up responsibility transfers, identify deliverables, and trigger the Handover Readiness Scorer.
          </p>
        </div>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        {/* Core Transfer Details */}
        <Card title="🔄 Transfer Scope & Employees">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Handover Package Title *
              </label>
              <input
                type="text"
                required
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                placeholder="e.g. Rahul Sharma to Priya Patel: Auth Module & DB Schema Transfer"
                style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                  Project *
                </label>
                <select
                  required
                  value={formData.projectId}
                  onChange={(e) => setFormData({ ...formData, projectId: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
                >
                  {projects.map((p) => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                  Handover Reason *
                </label>
                <select
                  value={formData.handoverReason}
                  onChange={(e) => setFormData({ ...formData, handoverReason: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
                >
                  <option value="Project Reassignment">Project Reassignment</option>
                  <option value="Parental or Medical Leave">Parental or Medical Leave</option>
                  <option value="Department Transfer">Department Transfer</option>
                  <option value="New Hire Ramp-Up">New Hire Ramp-Up</option>
                  <option value="Employee Exit">Employee Exit / Separation</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                  Departing / Transferring Employee *
                </label>
                <select
                  required
                  value={formData.departingEmployeeId}
                  onChange={(e) => setFormData({ ...formData, departingEmployeeId: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
                >
                  <option value="">Select transferring employee</option>
                  {users.map((u) => (
                    <option key={u.id} value={u.id}>{u.fullName} ({u.jobTitle || 'Team'})</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                  Receiving Employee *
                </label>
                <select
                  required
                  value={formData.receivingEmployeeId}
                  onChange={(e) => setFormData({ ...formData, receivingEmployeeId: e.target.value })}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
                >
                  <option value="">Select receiving employee</option>
                  {users.map((u) => (
                    <option key={u.id} value={u.id}>{u.fullName} ({u.jobTitle || 'Team'})</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Target Completion Date *
              </label>
              <input
                type="date"
                required
                value={formData.targetDate}
                onChange={(e) => setFormData({ ...formData, targetDate: e.target.value })}
                style={{ width: '100%', maxWidth: '280px', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
            </div>
          </div>
        </Card>

        {/* Task Selection */}
        <Card
          title={`📋 Select Tasks to Transfer (${formData.selectedTaskIds.length} selected)`}
          subtitle="Check all responsibilities and deliverables to attach to this handover package"
        >
          {tasks.length === 0 ? (
            <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              No tasks found for this project.
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', maxHeight: '300px', overflowY: 'auto' }}>
              {tasks.map((t) => {
                const isSelected = formData.selectedTaskIds.includes(t.id);
                return (
                  <div
                    key={t.id}
                    onClick={() => toggleTaskSelection(t.id)}
                    style={{
                      padding: '10px 14px',
                      borderRadius: '6px',
                      border: isSelected ? '1px solid var(--primary-color)' : '1px solid var(--border-color)',
                      background: isSelected ? 'rgba(79, 70, 229, 0.05)' : 'var(--surface-color)',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      transition: 'all 0.15s'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                      <input
                        type="checkbox"
                        checked={isSelected}
                        onChange={() => {}}
                        style={{ cursor: 'pointer' }}
                      />
                      <div>
                        <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)' }}>{t.taskCode || `#${t.id}`}</span>
                        <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', marginLeft: '6px' }}>{t.title}</span>
                      </div>
                    </div>
                    <span style={{ fontSize: '11px', padding: '2px 8px', borderRadius: '999px', background: 'var(--bg-secondary)', color: 'var(--text-secondary)' }}>
                      Current: {t.assignedEmployeeName || 'Unassigned'}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </Card>

        {/* Special Instructions & Operational Context */}
        <Card title="📝 Special Instructions & Transition Notes">
          <textarea
            rows={4}
            value={formData.specialInstructions}
            onChange={(e) => setFormData({ ...formData, specialInstructions: e.target.value })}
            placeholder="Document credentials location, secret manager keys, staging environments, on-call schedules, or specific caveats..."
            style={{ width: '100%', padding: '12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px', lineHeight: 1.6 }}
          />
        </Card>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <Button
            variant="outline"
            type="button"
            onClick={() => navigate('/handovers')}
          >
            Cancel
          </Button>
          <Button
            variant="primary"
            size="lg"
            icon={ArrowRightLeft}
            loading={submitting}
            type="submit"
          >
            Create Handover & Calculate Readiness
          </Button>
        </div>
      </form>
    </div>
  );
}
