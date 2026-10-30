import React, { useEffect, useState } from 'react';
import { projectApi, meetingApi, taskApi, handoverApi } from '../../services/api';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import AlertBanner from '../../components/common/AlertBanner';
import {
  FolderGit2,
  Calendar,
  CheckCircle2,
  Clock,
  ArrowRightLeft,
  Users,
  ShieldCheck,
  History
} from 'lucide-react';

export default function ProjectContinuityPage() {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState('');
  const [projectDetails, setProjectDetails] = useState(null);
  const [meetings, setMeetings] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [handovers, setHandovers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProjects();
  }, []);

  useEffect(() => {
    if (selectedProjectId) {
      loadProjectContinuity(selectedProjectId);
    }
  }, [selectedProjectId]);

  const fetchProjects = async () => {
    setLoading(true);
    try {
      const res = await projectApi.getProjects();
      setProjects(res.data || []);
      if (res.data?.length > 0) {
        setSelectedProjectId(res.data[0].id);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadProjectContinuity = async (pId) => {
    try {
      const [projRes, meetRes, taskRes, handRes] = await Promise.all([
        projectApi.getProject(pId),
        meetingApi.getMeetings({ projectId: pId }),
        taskApi.getTasks({ projectId: pId }),
        handoverApi.getHandovers({ projectId: pId })
      ]);
      setProjectDetails(projRes.data);
      setMeetings(meetRes.data?.content || meetRes.data || []);
      setTasks(taskRes.data?.content || taskRes.data || []);
      setHandovers(handRes.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <LoadingSpinner text="Loading projects..." />;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '24px', fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>
          Project Continuity & Historical Lineage
        </h2>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: '4px 0 0' }}>
          Unified historical audit of decisions, meetings, and responsibility transitions for long-term project longevity.
        </p>
      </div>

      {/* Project Selector Bar */}
      <Card>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <label style={{ fontSize: '13px', fontWeight: 700, color: 'var(--text-primary)', whiteSpace: 'nowrap' }}>
            Active Project:
          </label>
          <select
            value={selectedProjectId}
            onChange={(e) => setSelectedProjectId(e.target.value)}
            style={{ flex: 1, padding: '8px 12px', borderRadius: '6px', border: '1px solid var(--border-color)', fontSize: '13.5px', fontWeight: 600 }}
          >
            {projects.map((p) => (
              <option key={p.id} value={p.id}>{p.name} ({p.departmentName || 'Engineering'})</option>
            ))}
          </select>
        </div>
      </Card>

      {projectDetails && (
        <>
          {/* Project Summary Card */}
          <div style={{
            background: 'var(--surface-color)',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-lg)',
            padding: '20px',
            boxShadow: 'var(--shadow-sm)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--primary-color)', textTransform: 'uppercase' }}>
                  {projectDetails.departmentName}
                </span>
                <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--text-primary)', margin: '2px 0 6px' }}>
                  {projectDetails.name}
                </h3>
              </div>
              <Badge variant="primary">{projectDetails.status || 'ACTIVE'}</Badge>
            </div>
            <p style={{ fontSize: '13.5px', color: 'var(--text-secondary)', margin: 0, lineHeight: 1.6 }}>
              {projectDetails.description}
            </p>
          </div>

          {/* Continuity Metrics Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px' }}>
            <div style={{ background: 'var(--surface-color)', border: '1px solid var(--border-color)', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Total Meetings</span>
              <h3 style={{ margin: '4px 0 0', fontSize: '22px', fontWeight: 800, color: 'var(--primary-color)' }}>{meetings.length}</h3>
            </div>
            <div style={{ background: 'var(--surface-color)', border: '1px solid var(--border-color)', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Total Tasks</span>
              <h3 style={{ margin: '4px 0 0', fontSize: '22px', fontWeight: 800, color: '#0284c7' }}>{tasks.length}</h3>
            </div>
            <div style={{ background: 'var(--surface-color)', border: '1px solid var(--border-color)', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Completed Tasks</span>
              <h3 style={{ margin: '4px 0 0', fontSize: '22px', fontWeight: 800, color: 'var(--success-color)' }}>
                {tasks.filter((t) => t.status === 'COMPLETED').length}
              </h3>
            </div>
            <div style={{ background: 'var(--surface-color)', border: '1px solid var(--border-color)', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600, textTransform: 'uppercase' }}>Handovers Executed</span>
              <h3 style={{ margin: '4px 0 0', fontSize: '22px', fontWeight: 800, color: '#7c3aed' }}>{handovers.length}</h3>
            </div>
          </div>

          {/* Chronological Continuity Timeline */}
          <Card title="📜 Continuity Timeline & Knowledge Lineage">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', position: 'relative' }}>
              {/* Combine Meetings and Handovers into timeline */}
              {meetings.map((m) => (
                <div
                  key={`meet-${m.id}`}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '12px',
                    padding: '12px 14px',
                    borderRadius: '8px',
                    background: 'var(--bg-secondary)',
                    border: '1px solid var(--border-color)'
                  }}
                >
                  <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: '#eff6ff', color: '#2563eb', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <Calendar size={16} />
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <strong style={{ fontSize: '13.5px', color: 'var(--text-primary)' }}>Meeting: {m.title}</strong>
                      <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>{m.meetingDate}</span>
                    </div>
                    <p style={{ margin: '4px 0 0', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                      {m.agenda || 'Regular synchronization session'}
                    </p>
                  </div>
                </div>
              ))}

              {handovers.map((h) => (
                <div
                  key={`hand-${h.id}`}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '12px',
                    padding: '12px 14px',
                    borderRadius: '8px',
                    background: '#f5f3ff',
                    border: '1px solid #ddd6fe'
                  }}
                >
                  <div style={{ width: '32px', height: '32px', borderRadius: '8px', background: '#e9d5ff', color: '#7c3aed', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <ArrowRightLeft size={16} />
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <strong style={{ fontSize: '13.5px', color: '#581c87' }}>Handover: {h.title}</strong>
                      <Badge variant="success">{h.readinessScore}% Ready</Badge>
                    </div>
                    <p style={{ margin: '4px 0 0', fontSize: '12.5px', color: '#6b21a8' }}>
                      Transferred from <strong>{h.departingEmployeeName}</strong> to <strong>{h.receivingEmployeeName}</strong> ({h.handoverReason})
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </Card>
        </>
      )}
    </div>
  );
}
