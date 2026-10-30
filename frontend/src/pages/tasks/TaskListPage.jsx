import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { taskApi, projectApi, userApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import EmptyState from '../../components/common/EmptyState';
import {
  ListTodo,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  Clock,
  AlertTriangle,
  ArrowRight,
  ArrowRightLeft,
  Calendar,
  User,
  SlidersHorizontal
} from 'lucide-react';

export default function TaskListPage() {
  const navigate = useNavigate();

  const [tasks, setTasks] = useState([]);
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');
  const [projectFilter, setProjectFilter] = useState('');
  const [myTasksOnly, setMyTasksOnly] = useState(false);

  // Create Task Modal
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createFormData, setCreateFormData] = useState({
    title: '',
    description: '',
    projectId: '',
    assignedEmployeeId: '',
    priority: 'MEDIUM',
    startDate: '',
    dueDate: ''
  });
  const [creating, setCreating] = useState(false);

  // Complete / Record Outcome Modal
  const [selectedTaskForOutcome, setSelectedTaskForOutcome] = useState(null);
  const [outcomeDescription, setOutcomeDescription] = useState('Completed successfully.');
  const [recordingOutcome, setRecordingOutcome] = useState(false);

  // Carry Forward Modal
  const [selectedTaskForCarry, setSelectedTaskForCarry] = useState(null);
  const [carryReason, setCarryReason] = useState('Sprint capacity constraint; carry forward to next iteration.');
  const [carrying, setCarrying] = useState(false);

  useEffect(() => {
    fetchInitialData();
  }, [statusFilter, priorityFilter, projectFilter, myTasksOnly]);

  const fetchInitialData = async () => {
    setLoading(true);
    try {
      const params = {
        status: statusFilter || undefined,
        priority: priorityFilter || undefined,
        projectId: projectFilter || undefined,
        myTasksOnly: myTasksOnly || undefined
      };
      const [tasksRes, projectsRes, usersRes] = await Promise.allSettled([
        taskApi.getTasks(params),
        projectApi.getProjects(),
        userApi.getUsers()
      ]);
      if (tasksRes.status === 'fulfilled') {
        setTasks(tasksRes.value.data?.content || tasksRes.value.data || []);
      } else {
        console.error('Failed to fetch tasks:', tasksRes.reason);
        setError('Failed to fetch tasks.');
      }
      if (projectsRes.status === 'fulfilled') {
        setProjects(projectsRes.value.data || []);
      }
      if (usersRes.status === 'fulfilled') {
        setUsers(usersRes.value.data || []);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch tasks.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    setCreating(true);
    try {
      await taskApi.createTask({
        ...createFormData,
        projectId: Number(createFormData.projectId),
        assignedEmployeeId: createFormData.assignedEmployeeId ? Number(createFormData.assignedEmployeeId) : null
      });
      setIsCreateModalOpen(false);
      setCreateFormData({
        title: '',
        description: '',
        projectId: '',
        assignedEmployeeId: '',
        priority: 'MEDIUM',
        startDate: '',
        dueDate: ''
      });
      fetchInitialData();
    } catch (err) {
      alert(err?.response?.data?.message || 'Error creating task');
    } finally {
      setCreating(false);
    }
  };

  const handleRecordOutcome = async () => {
    if (!selectedTaskForOutcome) return;
    setRecordingOutcome(true);
    try {
      await taskApi.completeTask(selectedTaskForOutcome.id, {
        outcomeDescription: outcomeDescription,
        completionNotes: 'Task verified and signed off.'
      });
      setSelectedTaskForOutcome(null);
      fetchInitialData();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to record outcome.');
    } finally {
      setRecordingOutcome(false);
    }
  };

  const handleCarryForward = async () => {
    if (!selectedTaskForCarry) return;
    setCarrying(true);
    try {
      await taskApi.carryForwardTask(selectedTaskForCarry.id, {
        reason: carryReason,
        toMeetingId: null
      });
      setSelectedTaskForCarry(null);
      fetchInitialData();
    } catch (err) {
      alert(err?.response?.data?.message || 'Failed to carry forward task.');
    } finally {
      setCarrying(false);
    }
  };

  const filteredTasks = tasks.filter((t) => {
    if (!search) return true;
    const term = search.toLowerCase();
    return (
      t.title?.toLowerCase().includes(term) ||
      t.taskCode?.toLowerCase().includes(term) ||
      t.assignedEmployeeName?.toLowerCase().includes(term)
    );
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
            Task Management & Accountability
          </h2>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
            Assign, track, carry forward, and record validated outcomes for every project deliverable.
          </p>
        </div>
        <Button
          variant="primary"
          icon={Plus}
          onClick={() => setIsCreateModalOpen(true)}
        >
          Create Task
        </Button>
      </div>

      {error && <AlertBanner type="error" message={error} />}

      {/* Filter Bar */}
      <Card>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ flex: 1, minWidth: '220px', position: 'relative' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Search by title, code (#TSK-01), or owner..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{
                width: '100%',
                padding: '8px 12px 8px 34px',
                borderRadius: '6px',
                border: '1px solid var(--border-color)',
                fontSize: '13px'
              }}
            />
          </div>

          <div style={{ minWidth: '150px' }}>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">All Statuses</option>
              <option value="TO_DO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="BLOCKED">Blocked</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>

          <div style={{ minWidth: '140px' }}>
            <select
              value={priorityFilter}
              onChange={(e) => setPriorityFilter(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">All Priorities</option>
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>

          <div style={{ minWidth: '160px' }}>
            <select
              value={projectFilter}
              onChange={(e) => setProjectFilter(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">All Projects</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>

          <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', cursor: 'pointer' }}>
            <input
              type="checkbox"
              checked={myTasksOnly}
              onChange={(e) => setMyTasksOnly(e.target.checked)}
            />
            My Tasks Only
          </label>
        </div>
      </Card>

      {/* Task List / Table */}
      {loading ? (
        <LoadingSpinner text="Filtering tasks..." />
      ) : filteredTasks.length === 0 ? (
        <EmptyState
          title="No tasks match the criteria"
          description="Try relaxing your filters or create a new task."
          icon={ListTodo}
          actionLabel="Create Task"
          onAction={() => setIsCreateModalOpen(true)}
        />
      ) : (
        <div style={{
          background: 'var(--surface-color)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          boxShadow: 'var(--shadow-sm)'
        }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
            <thead>
              <tr style={{ background: 'var(--bg-secondary)', borderBottom: '1px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '11px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <th style={{ padding: '12px 16px' }}>Task Code & Title</th>
                <th style={{ padding: '12px 16px' }}>Project</th>
                <th style={{ padding: '12px 16px' }}>Assignee</th>
                <th style={{ padding: '12px 16px' }}>Priority</th>
                <th style={{ padding: '12px 16px' }}>Due Date</th>
                <th style={{ padding: '12px 16px' }}>Status</th>
                <th style={{ padding: '12px 16px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredTasks.map((t) => (
                <tr
                  key={t.id}
                  style={{
                    borderBottom: '1px solid var(--border-color)',
                    cursor: 'pointer',
                    transition: 'background 0.15s ease'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.background = 'var(--bg-secondary)'}
                  onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}
                >
                  <td style={{ padding: '14px 16px' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                      <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)' }}>{t.taskCode || `#${t.id}`}</span>
                      <span style={{ fontWeight: 600, color: 'var(--text-primary)', marginTop: '2px' }}>{t.title}</span>
                      {t.carryForwardCount > 0 && (
                        <span style={{ fontSize: '10.5px', color: '#d97706', display: 'flex', alignItems: 'center', gap: '3px', marginTop: '2px' }}>
                          <ArrowRightLeft size={10} /> Carried forward {t.carryForwardCount} time(s)
                        </span>
                      )}
                    </div>
                  </td>

                  <td style={{ padding: '14px 16px', color: 'var(--text-secondary)' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    {t.projectName || 'General'}
                  </td>

                  <td style={{ padding: '14px 16px' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <div style={{ width: '24px', height: '24px', borderRadius: '50%', background: '#e0e7ff', color: '#4338ca', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', fontWeight: 700 }}>
                        {t.assignedEmployeeName ? t.assignedEmployeeName.charAt(0) : '?'}
                      </div>
                      <span style={{ color: 'var(--text-primary)' }}>{t.assignedEmployeeName || 'Unassigned'}</span>
                    </div>
                  </td>

                  <td style={{ padding: '14px 16px' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    <Badge variant={t.priority === 'CRITICAL' ? 'danger' : t.priority === 'HIGH' ? 'warning' : 'primary'}>
                      {t.priority}
                    </Badge>
                  </td>

                  <td style={{ padding: '14px 16px', color: 'var(--text-secondary)' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    {t.dueDate || 'No due date'}
                  </td>

                  <td style={{ padding: '14px 16px' }} onClick={() => navigate(`/tasks/${t.id}`)}>
                    <Badge variant={t.status === 'COMPLETED' ? 'success' : t.status === 'IN_PROGRESS' ? 'info' : t.status === 'BLOCKED' ? 'danger' : 'default'}>
                      {t.status?.replace('_', ' ')}
                    </Badge>
                  </td>

                  <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '6px' }}>
                      {t.status !== 'COMPLETED' && (
                        <Button
                          size="sm"
                          variant="success"
                          icon={CheckCircle2}
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedTaskForOutcome(t);
                          }}
                        >
                          Complete
                        </Button>
                      )}
                      {t.status !== 'COMPLETED' && (
                        <Button
                          size="sm"
                          variant="outline"
                          icon={ArrowRightLeft}
                          title="Carry Forward"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedTaskForCarry(t);
                          }}
                        />
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal: Create Task */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        title="Create New Task"
        maxWidth="540px"
      >
        <form onSubmit={handleCreateTask} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Task Title *
            </label>
            <input
              type="text"
              required
              value={createFormData.title}
              onChange={(e) => setCreateFormData({ ...createFormData, title: e.target.value })}
              placeholder="e.g. Implement OAuth2 Refresh Token Rotation"
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Associated Project *
            </label>
            <select
              required
              value={createFormData.projectId}
              onChange={(e) => setCreateFormData({ ...createFormData, projectId: e.target.value })}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            >
              <option value="">Select project</option>
              {projects.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Assign Employee
              </label>
              <select
                value={createFormData.assignedEmployeeId}
                onChange={(e) => setCreateFormData({ ...createFormData, assignedEmployeeId: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              >
                <option value="">-- Unassigned --</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>{u.fullName} ({u.jobTitle || 'Team'})</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Priority
              </label>
              <select
                value={createFormData.priority}
                onChange={(e) => setCreateFormData({ ...createFormData, priority: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Start Date
              </label>
              <input
                type="date"
                value={createFormData.startDate}
                onChange={(e) => setCreateFormData({ ...createFormData, startDate: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
                Due Date
              </label>
              <input
                type="date"
                value={createFormData.dueDate}
                onChange={(e) => setCreateFormData({ ...createFormData, dueDate: e.target.value })}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Description
            </label>
            <textarea
              rows={3}
              value={createFormData.description}
              onChange={(e) => setCreateFormData({ ...createFormData, description: e.target.value })}
              placeholder="Detailed acceptance criteria and technical background..."
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
            <Button variant="outline" type="button" onClick={() => setIsCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" loading={creating}>
              Create Task
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal: Complete Task & Record Outcome */}
      <Modal
        isOpen={Boolean(selectedTaskForOutcome)}
        onClose={() => setSelectedTaskForOutcome(null)}
        title="Complete Task & Record Outcome"
        maxWidth="500px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <p style={{ fontSize: '13.5px', color: 'var(--text-primary)', margin: 0 }}>
            Task: <strong>{selectedTaskForOutcome?.title}</strong>
          </p>
          <div style={{
            background: '#ecfdf5',
            border: '1px solid #a7f3d0',
            borderRadius: '6px',
            padding: '10px 14px',
            fontSize: '12.5px',
            color: '#065f46'
          }}>
            HandoffIQ requires every completed deliverable to record a verifiable outcome description for future employee context and handover traceability.
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Outcome Description *
            </label>
            <textarea
              rows={3}
              value={outcomeDescription}
              onChange={(e) => setOutcomeDescription(e.target.value)}
              placeholder="e.g. Completed successfully. Documentation prepared and merged into main."
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <Button variant="outline" onClick={() => setSelectedTaskForOutcome(null)}>
              Cancel
            </Button>
            <Button variant="success" icon={CheckCircle2} loading={recordingOutcome} onClick={handleRecordOutcome}>
              Sign Off & Complete
            </Button>
          </div>
        </div>
      </Modal>

      {/* Modal: Carry Forward Task */}
      <Modal
        isOpen={Boolean(selectedTaskForCarry)}
        onClose={() => setSelectedTaskForCarry(null)}
        title="Carry Forward Unresolved Task"
        maxWidth="500px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <p style={{ fontSize: '13.5px', color: 'var(--text-primary)', margin: 0 }}>
            Carry Forward: <strong>{selectedTaskForCarry?.title}</strong>
          </p>
          <div style={{
            background: '#fffbeb',
            border: '1px solid #fde68a',
            borderRadius: '6px',
            padding: '10px 14px',
            fontSize: '12.5px',
            color: '#92400e'
          }}>
            This preserves the original task ID, status history, and historical owner while logging an explicit carry-forward audit entry.
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '4px' }}>
              Carry-Forward Reason *
            </label>
            <textarea
              rows={3}
              value={carryReason}
              onChange={(e) => setCarryReason(e.target.value)}
              placeholder="e.g. Blocked by external API key dependency; carry forward to Sprint 23."
              style={{ width: '100%', padding: '9px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13px' }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <Button variant="outline" onClick={() => setSelectedTaskForCarry(null)}>
              Cancel
            </Button>
            <Button variant="primary" icon={ArrowRightLeft} loading={carrying} onClick={handleCarryForward}>
              Carry Forward
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
